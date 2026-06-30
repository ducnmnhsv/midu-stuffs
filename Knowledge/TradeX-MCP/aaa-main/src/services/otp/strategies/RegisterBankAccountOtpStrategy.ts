import { Service } from 'typedi';
import { BaseOtpStrategy } from '../BaseOtpStrategy';
import { Errors, Kafka, Logger, TradexNotification, Utils } from 'tradex-common';
import RegisterBankAccountOtpSms from '../../../models/notification/RegisterBankAccountOtpSms';
import ISendOtpStrategyReq from '../../../models/request/ISendOtpStrategyReq';
import IVerifyOtpStrategyReq from '../../../models/request/IVerifyOtpStrategyReq';
import conf from '../../../conf';
import { PHONE_NUMBER_NOT_FOUND } from '../../../constants/errors';

@Service()
export class RegisterBankAccountOtpStrategy extends BaseOtpStrategy {

  // --- Config ---
  readonly txType = 'REGISTER_BANK_ACCOUNT';
  readonly otpLength = 6;
  readonly ttlSeconds = 120;
  readonly maxResendPerSession = 3;
  readonly maxFailAttempts = 5;
  readonly lockDurationSeconds = 1800;
  readonly minIntervalSeconds = 5;

  // --- Abstract method implementations ---

  protected getAccountNumber(request: ISendOtpStrategyReq): string {
    return request.headers.token.userData.accountNumbers[0];
  }

  protected async resolvePhoneNumber(
    request: ISendOtpStrategyReq,
    transactionId: string,
  ): Promise<string> {
    const accountNumber = this.getAccountNumber(request);
    Logger.info(`[RegisterBankAccountOtp] txId: ${transactionId} -- calling lotte-bridge get:/api/v1/lotte/equity/account/info for acc: ${accountNumber}`);
    const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      transactionId,
      'lotte-bridge',
      'get:/api/v1/lotte/equity/account/info',
      { accountNumber, headers: request.headers },
      conf.timeouts.otpService,
    );
    if (msg.data.status) {
      Logger.error(`[RegisterBankAccountOtp] txId: ${transactionId} -- lotte-bridge returned error: ${JSON.stringify(msg.data.status)}`);
      throw new Errors.ForwardError(msg.data.status);
    }
    Logger.info(`[RegisterBankAccountOtp] txId: ${transactionId} -- response from lotte-bridge: ${JSON.stringify(msg.data)}`);
    const phoneNumber: string = msg.data.data?.phoneNumber;
    if (!phoneNumber) {
      Logger.error(`[RegisterBankAccountOtp] txId: ${transactionId} -- phoneNumber not found in account info for acc: ${accountNumber}`);
      throw new Errors.GeneralError(PHONE_NUMBER_NOT_FOUND);
    }

    Logger.info(`[RegisterBankAccountOtp] txId: ${transactionId} -- lotte-bridge response OK, phoneNumber resolved`);
    return phoneNumber;
  }

  protected createSmsTemplateData(otpCode: string): TradexNotification.ITemplateData {
    const sms = new RegisterBankAccountOtpSms();
    sms.otp = otpCode;
    return sms;
  }

  protected validateSendRequest(request: ISendOtpStrategyReq): void {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.txType, "txType")
      .setRequire()
      .throwValid(invalidParams);
    if (!request.headers?.token?.userData?.accountNumbers?.[0]) {
      invalidParams.add("REQUIRED", "accountNumber", null);
    }
    invalidParams.throwErr();
  }

  protected validateVerifyRequest(request: IVerifyOtpStrategyReq): void {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.otpId, "otpId")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.otpValue, "otpValue")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();
  }
}
