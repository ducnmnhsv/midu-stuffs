import { Inject, Service } from 'typedi';
import { IContext } from '../models/IContext';
import { Errors, Utils } from 'tradex-common';
import { INotificationRequest, IMaintenanceNotficationRequest } from '../models/request/INotificationRequest';
import {
  IMaintenanceNotficationResponse,
  INotificationResponse,
  toMaintenanceNotficationResponse,
  toNotificationResponse,
} from '../models/response/INotificationResponse';
import LotteCommonDao from '../daos/LotteCommonDao';
import {
  ILotteMaintenanceNotificationRequest,
  ILotteNotificationRequest,
} from '../models/request/lotte/ILotteNotificationRequest';
import config from '../config';
import { ILotteotificationResponse } from '../models/response/lotte/ILotteotificationResponse';
import { IParam } from '../models/request/lotte/ILotteRequest';
import { parseMessages, setDefault } from '../utils/lotte';
import { GeneralError } from 'tradex-common/build/src/modules/errors';

const { InvalidParameterError } = Errors;
const { validate } = Utils;

@Service()
export class NotificationService {
  @Inject()
  private lotteCommon: LotteCommonDao;

  async getNotification(request: INotificationRequest, ctx: IContext): Promise<INotificationResponse[]> {
    const error = new InvalidParameterError();
    validate(request.subAccount, 'subAccount')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const accountNumber = request.headers.token.userData.username.toUpperCase();
    const notificationRequest: ILotteNotificationRequest = {
      acnt_no: accountNumber ? accountNumber : '',
      sub_no: request.subAccount ? request.subAccount : '',
      from_dt: request.fromDate ? request.fromDate : '',
      to_dt: request.toDate ? request.toDate : '',
      next_key: request.nextKey ? request.nextKey : '',
      lang_code: 'V',
    };
    const headers: IParam = {
      'Content-Type': 'application/json',
      apiKey: config.lotte.apiKey,
    };
    const notificationResponse: ILotteotificationResponse = await this.lotteCommon.get(
      config.lotte.apis.getNotification,
      headers,
      notificationRequest,
      null,
      null,
      ctx
    );
    const response: INotificationResponse[] = toNotificationResponse(notificationResponse);
    return response;
  }

  async getMaintenanceNotfication(
    request: IMaintenanceNotficationRequest,
    ctx: IContext
  ): Promise<IMaintenanceNotficationResponse[]> {
    const notificationRequest: ILotteMaintenanceNotificationRequest = {
      from_date: request.fromDate ? request.fromDate : '',
      to_date: request.toDate ? request.toDate : '',
      next_key: request.nextKey ? request.nextKey : '',
      row_count: setDefault<number>(request.rowCount, config.defaultFetchCount).toString(),
    };
    const headers: IParam = {
      'Content-Type': 'application/json',
      apiKey: config.lotte.apiKey,
    };
    const notificationResponse: ILotteotificationResponse = await this.lotteCommon.get(
      config.lotte.apis.getMaintenanceNotification,
      headers,
      notificationRequest,
      null,
      null,
      ctx
    );
    const { codes, messages } = parseMessages(notificationResponse.error_desc, notificationResponse.error_code);
    if (codes === null || codes === '0011') {
      const response: IMaintenanceNotficationResponse[] = toMaintenanceNotficationResponse(notificationResponse);
      return response;
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(messages);
    }
  }
}
