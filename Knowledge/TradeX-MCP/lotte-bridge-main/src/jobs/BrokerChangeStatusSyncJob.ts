import { Service, Inject } from 'typedi';
import { InjectRepository } from 'typeorm-typedi-extensions';
import { Kafka, Logger, Utils } from 'tradex-common';
import { scheduleJob } from 'node-schedule';
import config from '../config';
import { IContext } from '../models/IContext';
import { LotteAccountDao } from '../daos/LotteAccountDao';
import { AccountChangeBrokerRequestRepository } from '../repositories/AccountChangeBrokerRequestRepository';
import { AccountChangeBrokerRequest, ChangeBrokerStatus } from '../models/db/AccountChangeBrokerRequest';
import { ILotteBrokerHistoryRequest } from '../models/request/lotte/ILotteBrokerHistoryRequest';
import { ILotteBrokerHistoryResponse, ILotteBrokerHistoryData } from '../models/response/lotte/ILotteBrokerHistoryResponse';
import RedisService from '../init/RedisService';
import { formatDateVietnam, mapBosStatusToTradeX, sendEmailNotification, sendSmsNotification } from '../utils/brokerUtils';
import { ILotteAccountInfoRequest } from '../models/request/lotte/ILotteAccountInfoRequest';
import { ILotteEmployeeInfoRequest } from '../models/request/lotte/ILotteEmployeeInfoRequest';

const { formatDateToDisplay, DATE_DISPLAY_FORMAT } = Utils;

const LOCK_KEY = 'broker_change_sync_job_lock';
const LOCK_DURATION = 300;

@Service()
export class BrokerChangeStatusSyncJob {
  @Inject()
  private lotteAccountDao: LotteAccountDao;

  @Inject()
  private redisService: RedisService;

  @InjectRepository()
  private accountChangeBrokerRequestRepository: AccountChangeBrokerRequestRepository;

  async init(): Promise<void> {
    scheduleJob(config.schedule.syncBrokerStatus, async () => {
      await this.execute();
    });
    Logger.info('BrokerChangeStatusSyncJob scheduled with cron:', config.schedule.syncBrokerStatus);
  }

  private async execute(): Promise<void> {
    const jobId = `sync_broker_${Date.now()}`;
    Logger.info(jobId, 'BrokerChangeStatusSyncJob starting at', new Date().toLocaleString());

    const lockAcquired = await this.acquireLock();
    if (!lockAcquired) {
      Logger.info(jobId, 'BrokerChangeStatusSyncJob skipped - another instance is running');
      return;
    }

    try {
      const pendingRequests = await this.accountChangeBrokerRequestRepository.findAllPending();
      Logger.info(jobId, `Found ${pendingRequests.length} pending change broker requests`);

      if (pendingRequests.length === 0) {
        Logger.info(jobId, 'No pending requests to sync');
        return;
      }

      const ctx: IContext = {
        id: jobId,
        txId: jobId,
        orgMsg: {
          data: {
            headers: {
              'accept-language': 'vi',
            },
          },
        } as unknown as Kafka.IMessage,
      };

      const today = new Date();
      const toDate = formatDateToDisplay(today, DATE_DISPLAY_FORMAT);

      const requestsToUpdate: AccountChangeBrokerRequest[] = [];
      const notificationsToSend: { request: AccountChangeBrokerRequest; oldStatus: ChangeBrokerStatus; newStatus: ChangeBrokerStatus }[] = [];

      for (const request of pendingRequests) {
        if (!request.coreSeqNo) continue;

        const fromDate = formatDateToDisplay(request.createdAt || new Date(), DATE_DISPLAY_FORMAT);

        const lotteRequest: ILotteBrokerHistoryRequest = {
          account_number: request.accountNo,
          from_date: fromDate,
          to_date: toDate,
          previous_broker: '%',
          new_broker: '%',
          broker_status: '%',
          status: '%',
          hts_user_id: '%',
        };

        try {
          const lotteRes: ILotteBrokerHistoryResponse = await this.lotteAccountDao.getBrokerHistory(lotteRequest, ctx);

          if (lotteRes.error_code !== '0000' || !lotteRes.data_list) {
            Logger.warn(jobId, `API error for account ${request.accountNo}: ${lotteRes.error_code}`);
            continue;
          }

          const coreData = lotteRes.data_list.find(item => item.seq_no === request.coreSeqNo);
          if (!coreData) continue;

          const updateResult = this.prepareStatusUpdate(request, coreData);
          if (!updateResult) continue;

          requestsToUpdate.push(updateResult.request);
          if (updateResult.shouldNotify) {
            notificationsToSend.push({
              request: updateResult.request,
              oldStatus: updateResult.oldStatus,
              newStatus: updateResult.newStatus,
            });
          }
        } catch (error) {
          Logger.error(jobId, `Error querying status for account ${request.accountNo}:`, error);
        }
      }

      if (requestsToUpdate.length > 0) {
        await this.accountChangeBrokerRequestRepository.save(requestsToUpdate);
        Logger.info(jobId, `Batch saved ${requestsToUpdate.length} updated requests`);

        for (const notification of notificationsToSend) {
          await this.sendStatusChangeNotification(notification.request, notification.oldStatus, notification.newStatus, jobId, ctx);
        }
      }

      Logger.info(jobId, `Updated ${requestsToUpdate.length} out of ${pendingRequests.length} pending requests`);

      Logger.info(jobId, 'BrokerChangeStatusSyncJob completed at', new Date().toLocaleString());
    } finally {
      await this.releaseLock();
    }
  }

  private prepareStatusUpdate(
    request: AccountChangeBrokerRequest,
    coreData: ILotteBrokerHistoryData
  ): { request: AccountChangeBrokerRequest; oldStatus: ChangeBrokerStatus; newStatus: ChangeBrokerStatus; shouldNotify: boolean } | null {
    const newStatus = mapBosStatusToTradeX(coreData.bos_status);

    if (request.status !== newStatus) {
      const oldStatus = request.status;
      request.status = newStatus;
      request.updatedAt = new Date();
      if (coreData.broker_update_date) {
        try {
          const updateDate = new Date(coreData.broker_update_date);
          if (!isNaN(updateDate.getTime())) {
            request.updatedAt = updateDate;
          }
        } catch (e) {
          // Keep current date if parsing fails
        }
      }

      const shouldNotify = newStatus === ChangeBrokerStatus.APPROVED || newStatus === ChangeBrokerStatus.REJECTED;
      return { request, oldStatus, newStatus, shouldNotify };
    }
    return null;
  }

  private async sendStatusChangeNotification(
    request: AccountChangeBrokerRequest,
    oldStatus: ChangeBrokerStatus,
    newStatus: ChangeBrokerStatus,
    jobId: string,
    ctx: IContext
  ): Promise<void> {
    try {
      let customerEmail = '';
      let customerPhone = '';
      let newBrokerEmail = '';
      let previousBrokerName = '';
      let newBrokerName = '';

      const accountInfoRequest: ILotteAccountInfoRequest = {
        acnt_no: request.accountNo,
      };

      try {
        const accountInfoRes = await this.lotteAccountDao.getAccountInfo(accountInfoRequest, ctx);
        if (accountInfoRes.error_code === '0000' && accountInfoRes.data_list && accountInfoRes.data_list.length > 0) {
          const accountInfo = accountInfoRes.data_list[0];
          customerEmail = accountInfo.email || '';
          customerPhone = accountInfo.phone || '';

          if (request.oldBrokerId) {
            const prevEmpInfoRequest: ILotteEmployeeInfoRequest = {
              employee_id: request.oldBrokerId,
            };

            try {
              const prevEmpInfoRes = await this.lotteAccountDao.getEmployeeInfo(prevEmpInfoRequest, ctx);
              if (prevEmpInfoRes.error_code === '0000' && prevEmpInfoRes.data_list && prevEmpInfoRes.data_list.length > 0) {
                previousBrokerName = prevEmpInfoRes.data_list[0].os_user_nm || '';
              }
            } catch (empError) {
              Logger.warn(jobId, `Failed to get employee info for old broker ${request.oldBrokerId}:`, empError);
            }
          }
        }
      } catch (accountError) {
        Logger.warn(jobId, `Failed to get account info for ${request.accountNo}:`, accountError);
      }

      if (request.newBrokerId) {
        const newEmpInfoRequest: ILotteEmployeeInfoRequest = {
          employee_id: request.newBrokerId,
        };

        try {
          const newEmpInfoRes = await this.lotteAccountDao.getEmployeeInfo(newEmpInfoRequest, ctx);
          if (newEmpInfoRes.error_code === '0000' && newEmpInfoRes.data_list && newEmpInfoRes.data_list.length > 0) {
            const newBrokerInfo = newEmpInfoRes.data_list[0];
            newBrokerName = newBrokerInfo.os_user_nm || '';
            newBrokerEmail = newBrokerInfo.os_email || '';
          }
        } catch (empError) {
          Logger.warn(jobId, `Failed to get employee info for new broker ${request.newBrokerId}:`, empError);
        }
      }

      const approvedAt = formatDateVietnam(new Date());

      const baseTemplateData = {
        accountNumber: request.accountNo,
        accountName: request.customerName || '',
        previousBrokerName,
        newBrokerName,
        approvedAt,
        sequence: request.coreSeqNo,
      };

      if (newStatus === ChangeBrokerStatus.APPROVED) {
        this.sendApprovedNotifications(jobId, request.accountNo, newBrokerEmail, customerEmail, customerPhone, baseTemplateData);
      } else if (newStatus === ChangeBrokerStatus.REJECTED) {
        this.sendRejectedNotifications(jobId, request.accountNo, newBrokerEmail, customerEmail, customerPhone, baseTemplateData);
      }

      Logger.info(jobId, 'Change broker status update notifications sent', {
        accountNumber: request.accountNo,
        coreSeqNo: request.coreSeqNo,
        oldStatus,
        newStatus,
        customerEmail: customerEmail || 'N/A',
        customerPhone: customerPhone || 'N/A',
        newBrokerEmail: newBrokerEmail || 'N/A',
      });
    } catch (error) {
      Logger.error(jobId, `Failed to send notification for request ${request.coreSeqNo}:`, error);
    }
  }

  private sendApprovedNotifications(
    jobId: string,
    accountNo: string,
    newBrokerEmail: string,
    customerEmail: string,
    customerPhone: string,
    templateData: Record<string, unknown>
  ): void {
    if (newBrokerEmail) {
      sendEmailNotification({
        txId: jobId,
        logId: jobId,
        toList: [newBrokerEmail],
        subject: `[NHSV] Xác nhận phê duyệt yêu cầu đổi người chăm sóc tài khoản – Tài khoản ${accountNo}`,
        templateName: 'nhsv_change_broker_approved',
        templateData,
        logMessage: 'APPROVED notification sent to new broker',
      });
    }

    if (customerEmail) {
      sendEmailNotification({
        txId: jobId,
        logId: jobId,
        toList: [customerEmail],
        subject: '[NHSV] Yêu cầu đổi người chăm sóc tài khoản được chấp thuận',
        templateName: 'nhsv_change_broker_customer_approved',
        templateData,
        logMessage: 'APPROVED notification sent to customer',
      });
    }

    if (customerPhone) {
      sendSmsNotification({
        txId: jobId,
        logId: jobId,
        phoneNumber: customerPhone,
        templateName: 'nhsv_change_broker_approved_sms',
        templateData: { accountNumber: accountNo },
        logMessage: 'APPROVED SMS sent to customer',
      });
    } else {
      Logger.warn(jobId, `No phone found for customer ${accountNo}, skipping APPROVED SMS notification`);
    }
  }

  private sendRejectedNotifications(
    jobId: string,
    accountNo: string,
    newBrokerEmail: string,
    customerEmail: string,
    customerPhone: string,
    templateData: Record<string, unknown>
  ): void {
    if (newBrokerEmail) {
      sendEmailNotification({
        txId: jobId,
        logId: jobId,
        toList: [newBrokerEmail],
        subject: `[NHSV] Xác nhận từ chối yêu cầu đổi người chăm sóc tài khoản – Tài khoản ${accountNo}`,
        templateName: 'nhsv_change_broker_rejected',
        templateData,
        logMessage: 'REJECTED notification sent to new broker',
      });
    }

    if (customerEmail) {
      sendEmailNotification({
        txId: jobId,
        logId: jobId,
        toList: [customerEmail],
        subject: '[NHSV] Yêu cầu đổi người chăm sóc tài khoản chưa được chấp thuận',
        templateName: 'nhsv_change_broker_customer_rejected',
        templateData,
        logMessage: 'REJECTED notification sent to customer',
      });
    }

    if (customerPhone) {
      sendSmsNotification({
        txId: jobId,
        logId: jobId,
        phoneNumber: customerPhone,
        templateName: 'nhsv_change_broker_rejected_sms',
        templateData: { accountNumber: accountNo },
        logMessage: 'REJECTED SMS sent to customer',
      });
    } else {
      Logger.warn(jobId, `No phone found for customer ${accountNo}, skipping REJECTED SMS notification`);
    }
  }

  private async acquireLock(): Promise<boolean> {
    try {
      const lockKey = this.redisService.getRedisKey('lock', LOCK_KEY);
      const exists = await this.redisService.exists(lockKey);
      if (exists) {
        return false;
      }
      await this.redisService.set('lock', LOCK_KEY, Date.now().toString(), false, LOCK_DURATION);
      return true;
    } catch (error) {
      Logger.error('BrokerChangeStatusSyncJob', 'Error acquiring lock:', error);
      return false;
    }
  }

  private async releaseLock(): Promise<void> {
    try {
      await this.redisService.del('lock', LOCK_KEY);
    } catch (error) {
      Logger.error('BrokerChangeStatusSyncJob', 'Error releasing lock:', error);
    }
  }
}

