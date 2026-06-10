import { Kafka, Errors, Logger, Utils } from 'tradex-common';
import config from '../config';
import IPhoneNumberReq from '../models/request/IPhoneNumberReq';
import { Inject, Service } from 'typedi';
import { ILotteAccountInfoRequest } from '../models/request/lotte/ILotteAccountInfoRequest';
import { IAccountInfoResponse } from '../models/response/IAccountInfoResponse';
import { ILotteAccountInfoResponse, ILotteAccountInfoData } from '../models/response/lotte/ILotteAccountInfoResponse';
import { parseMessages, getElementAtIndex } from '../utils/lotte';
import { LotteAccountDao } from '../daos/LotteAccountDao';
import { IContext } from '../models/IContext';
import * as crypt3 from 'apache-crypt';

@Service()
export class CommonService {
  @Inject()
  private lotteAccountDao: LotteAccountDao;

  async getPhoneNumberFromTuxedo(phoneNumberRequest: IPhoneNumberReq, transactionId: string): Promise<string> {
    // query tuxedo to get phoneNumber
    const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      transactionId,
      config.tuxedo.topic,
      config.tuxedo.apis.accountMobile,
      phoneNumberRequest,
      config.timeouts.otpService
    );
    if (msg.data.status) {
      throw new Errors.ForwardError(msg.data.status);
    }
    return msg.data.data.phoneNumber;
  }

  async getAccountInfo(accountNumber: string, ctx: IContext): Promise<IAccountInfoResponse> {
    const lotteRequest: ILotteAccountInfoRequest = {
      acnt_no: accountNumber.toUpperCase(),
    };
    const lotteRes: ILotteAccountInfoResponse = await this.lotteAccountDao.getAccountInfo(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    const lotteResDataList: ILotteAccountInfoData = getElementAtIndex<ILotteAccountInfoData>(lotteRes.data_list);
    if (codes === null || codes === '0011') {
      const response: IAccountInfoResponse = {
        username: accountNumber.toUpperCase(),
        email: lotteResDataList.email,
        address: lotteResDataList.address,
        phoneNumber: lotteResDataList.phone,
        identifierNumber: lotteResDataList.identity_card,
        customerName: lotteResDataList.customer_name,
        agencyName: lotteResDataList.manager,
      };
      return response;
    } else {
      return {};
    }
  }

  decodeOtp(checksum: string, prefixLog?: string): string {
    Logger.info(`${prefixLog} -- START decode OTP: ${checksum}`);
    for (let i = 0; i < 9999; i++) {
      const plainText = Utils.leftPad(`${i}`, 4, '0');
      if (crypt3(plainText, checksum) === checksum) {
        Logger.info(`${prefixLog} -- END decode OTP: ${checksum}, result = ${plainText}`);
        return plainText;
      }
    }
    Logger.error(`cannot decode otp: ${checksum}`);
    return null;
  }

  convertPhoneNumber(phoneNumber: string): string {
    return phoneNumber.startsWith('0') ? '84'.concat(phoneNumber.substring(1)) : phoneNumber;
  }
}
