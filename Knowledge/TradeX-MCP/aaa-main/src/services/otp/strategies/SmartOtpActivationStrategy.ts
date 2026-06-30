import { Service } from 'typedi';
import { BaseOtpStrategy } from '../BaseOtpStrategy';
import { Errors, Kafka, Logger, TradexNotification, Utils } from 'tradex-common';
import SmartOtpActivationSms from '../../../models/notification/SmartOtpActivationSms';
import ISendOtpStrategyReq from '../../../models/request/ISendOtpStrategyReq';
import IVerifyOtpStrategyReq from '../../../models/request/IVerifyOtpStrategyReq';
import conf from '../../../conf';
import { PHONE_NUMBER_NOT_FOUND } from '../../../constants/errors';

import * as uuid from 'uuid';
import IVerifyOtpStrategyRes from '../../../models/response/IVerifyOtpStrategyRes';
import * as redis from '../../redis/Redis';
import { Category } from '../../redis/Redis';

@Service()
export class SmartOtpActivationStrategy extends BaseOtpStrategy {

  // --- Config (loaded from conf.otpStrategy.SMART_OTP) ---
  readonly txType = 'SMART_OTP';
  readonly otpLength = conf.otpStrategy.SMART_OTP.otpLength;
  readonly ttlSeconds = conf.otpStrategy.SMART_OTP.ttlSeconds;
  readonly maxResendPerSession = conf.otpStrategy.SMART_OTP.maxResendPerSession;
  readonly maxFailAttempts = conf.otpStrategy.SMART_OTP.maxFailAttempts;
  readonly lockDurationSeconds = conf.otpStrategy.SMART_OTP.lockDurationSeconds;
  readonly minIntervalSeconds = conf.otpStrategy.SMART_OTP.minIntervalSeconds;

  // --- Abstract method implementations ---

  protected getAccountNumber(request: ISendOtpStrategyReq): string {
    return request.headers.token.userData.accountNumbers[0];
  }

  protected async resolvePhoneNumber(
    request: ISendOtpStrategyReq,
    transactionId: string,
  ): Promise<string> {
    const accountNumber = this.getAccountNumber(request);
    Logger.info(`[SmartOtpActivation] txId: ${transactionId} -- calling lotte-bridge get:/api/v1/lotte/equity/account/info for acc: ${accountNumber}`);
    const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      transactionId,
      'lotte-bridge',
      'get:/api/v1/lotte/equity/account/info',
      { accountNumber, headers: request.headers },
      conf.timeouts.otpService,
    );
    if (msg.data.status) {
      Logger.error(`[SmartOtpActivation] txId: ${transactionId} -- lotte-bridge returned error: ${JSON.stringify(msg.data.status)}`);
      throw new Errors.ForwardError(msg.data.status);
    }
    Logger.info(`[SmartOtpActivation] txId: ${transactionId} -- response from lotte-bridge: ${JSON.stringify(msg.data)}`);
    const phoneNumber: string = msg.data.data?.phoneNumber;
    if (!phoneNumber) {
      Logger.error(`[SmartOtpActivation] txId: ${transactionId} -- phoneNumber not found in account info for acc: ${accountNumber}`);
      throw new Errors.GeneralError(PHONE_NUMBER_NOT_FOUND);
    }

    Logger.info(`[SmartOtpActivation] txId: ${transactionId} -- lotte-bridge response OK, phoneNumber resolved`);
    return phoneNumber;
  }

  protected createSmsTemplateData(otpCode: string): TradexNotification.ITemplateData {
    const sms = new SmartOtpActivationSms();
    sms.otp = otpCode;
    return sms;
  }

  protected validateSendRequest(request: ISendOtpStrategyReq): void {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.txType, 'txType')
      .setRequire()
      .throwValid(invalidParams);
    if (!request.headers?.token?.userData?.accountNumbers?.[0]) {
      invalidParams.add('REQUIRED', 'accountNumber', null);
    }
    invalidParams.throwErr();
  }

  protected validateVerifyRequest(request: IVerifyOtpStrategyReq): void {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.otpId, 'otpId')
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.otpValue, 'otpValue')
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();
  }

  async verifyOtp(
    request: IVerifyOtpStrategyReq,
    msg: Kafka.IMessage,
  ): Promise<IVerifyOtpStrategyRes> {
    const transactionId = `${msg.transactionId}`;
    const result = await super.verifyOtp(request, msg);
    if (result.success) {
      // Sinh otpKey + store Redis để /smartOtp/register validate sau này
      const username = request.headers?.token?.userData?.username;
      const accountNumber = request.headers?.token?.userData?.accountNumbers?.[0];
      const otpKey = uuid.v4();
      await redis.set(
        Category.SOTP_REGISTER_TOKEN,
        otpKey,
        { username, accountNumber },
        300,   // TTL 5 phút - đủ để user qua màn /register
        true,  // isNotSetCluster=true: bỏ prefix "aaa_" để lotte-bridge đọc được
      );
      Logger.info(`[SmartOtpActivation] verifyOtp -- txId: ${transactionId}, generated otpKey, stored in Redis (catSotpRegisterToken_${otpKey}) with TTL 300s for acc: ${accountNumber}`);
      return { success: true, otpKey };
    }
    return result;
  }

}
