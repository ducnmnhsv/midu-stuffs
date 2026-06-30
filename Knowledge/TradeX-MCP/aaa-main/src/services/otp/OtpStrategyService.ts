import { Service } from 'typedi';
import { IOtpStrategy } from './IOtpStrategy';
import { RegisterBankAccountOtpStrategy } from './strategies/RegisterBankAccountOtpStrategy';
import { SmartOtpActivationStrategy } from './strategies/SmartOtpActivationStrategy';
import * as redis from '../redis/Redis';
import { Category } from '../redis/Redis';
import { Kafka, Errors, Logger } from 'tradex-common';
import ISendOtpStrategyReq from '../../models/request/ISendOtpStrategyReq';
import IVerifyOtpStrategyReq from '../../models/request/IVerifyOtpStrategyReq';
import ISendOtpStrategyRes from '../../models/response/ISendOtpStrategyRes';
import IVerifyOtpStrategyRes from '../../models/response/IVerifyOtpStrategyRes';
import { IOtpData } from '../../models/redis/IOtpData';
import { OTP_TX_TYPE_NOT_FOUND, OTP_EXPIRED } from '../../constants/errors';

@Service()
export class OtpStrategyService {

  private readonly strategies: Map<string, IOtpStrategy> = new Map();

  constructor(
    registerBankAccount: RegisterBankAccountOtpStrategy,
    smartOtpActivation: SmartOtpActivationStrategy,
  ) {
    this.register(registerBankAccount);
    this.register(smartOtpActivation);
  }

  private register(strategy: IOtpStrategy): void {
    this.strategies.set(strategy.txType, strategy);
    Logger.info(`OTP Strategy registered: ${strategy.txType}`);
  }

  private getStrategy(txType: string): IOtpStrategy {
    const strategy = this.strategies.get(txType);
    if (!strategy) {
      Logger.warn(`[OtpStrategyService] -- strategy not found for txType: ${txType}, available: [${Array.from(this.strategies.keys()).join(', ')}]`);
      throw new Errors.GeneralError(OTP_TX_TYPE_NOT_FOUND);
    }
    return strategy;
  }

  async sendOtp(
    request: ISendOtpStrategyReq,
    msg: Kafka.IMessage,
  ): Promise<ISendOtpStrategyRes> {
    Logger.info(`[OtpStrategyService] sendOtp -- txId: ${msg.transactionId}, txType: ${request.txType}`);
    return this.getStrategy(request.txType).sendOtp(request, msg);
  }

  async verifyOtp(
    request: IVerifyOtpStrategyReq,
    msg: Kafka.IMessage,
    expectedTxType?: string,
  ): Promise<IVerifyOtpStrategyRes> {
    Logger.info(`[OtpStrategyService] verifyOtp -- txId: ${msg.transactionId}, otpId: ${request.otpId}`);

    let otpData: IOtpData;
    try {
      otpData = await redis.get<IOtpData>(Category.OTP_STRATEGY, request.otpId);
    } catch {
      Logger.warn(`[OtpStrategyService] verifyOtp -- OTP not found in Redis, otpId: ${request.otpId}`);
      throw new Errors.GeneralError(OTP_EXPIRED);
    }
    if (!otpData) {
      Logger.warn(`[OtpStrategyService] verifyOtp -- OTP data is null, otpId: ${request.otpId}`);
      throw new Errors.GeneralError(OTP_EXPIRED);
    }

    // Check otpId thuộc đúng endpoint
    if (expectedTxType && otpData.txType !== expectedTxType) {
      Logger.warn(`[OtpStrategyService] verifyOtp -- txType mismatch: expected=${expectedTxType}, actual=${otpData.txType}, otpId=${request.otpId}`);
      throw new Errors.GeneralError(OTP_EXPIRED);
    }

    Logger.info(`[OtpStrategyService] verifyOtp -- resolved txType: ${otpData.txType} for otpId: ${request.otpId}`);

    return this.getStrategy(otpData.txType).verifyOtp(request, msg);
  }
}
