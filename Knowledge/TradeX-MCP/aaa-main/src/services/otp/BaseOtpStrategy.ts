import { IOtpStrategy } from './IOtpStrategy';
import * as redis from '../redis/Redis';
import { Category } from '../redis/Redis';
import { sendSms } from '../NotificationService';
import { Kafka, Logger, Errors, TradexNotification } from 'tradex-common';
import conf from '../../conf';
import * as uuid from 'uuid';
import * as moment from 'moment';
import { IOtpData } from '../../models/redis/IOtpData';
import { IOtpValidation } from '../../models/redis/IOtpValidation';
import ISendOtpStrategyReq from '../../models/request/ISendOtpStrategyReq';
import IVerifyOtpStrategyReq from '../../models/request/IVerifyOtpStrategyReq';
import ISendOtpStrategyRes from '../../models/response/ISendOtpStrategyRes';
import IVerifyOtpStrategyRes from '../../models/response/IVerifyOtpStrategyRes';
import {
  OTP_EXPIRED,
  OTP_LOCKED,
  OTP_MAX_RESEND,
  OTP_INCORRECT,
  OTP_INCORRECT_MAX,
  OTP_GENERATE_TOO_FAST,
} from '../../constants/errors';

export abstract class BaseOtpStrategy implements IOtpStrategy {

  abstract readonly txType: string;
  abstract readonly otpLength: number;
  abstract readonly ttlSeconds: number;
  abstract readonly maxResendPerSession: number;
  abstract readonly maxFailAttempts: number;
  abstract readonly lockDurationSeconds: number;
  abstract readonly minIntervalSeconds: number;

  protected abstract resolvePhoneNumber(request: ISendOtpStrategyReq, transactionId: string): Promise<string>;
  protected abstract createSmsTemplateData(otpCode: string): TradexNotification.ITemplateData;
  protected abstract getAccountNumber(request: ISendOtpStrategyReq): string;
  protected abstract validateSendRequest(request: ISendOtpStrategyReq): void;
  protected abstract validateVerifyRequest(request: IVerifyOtpStrategyReq): void;


  async sendOtp(request: ISendOtpStrategyReq, msg: Kafka.IMessage): Promise<ISendOtpStrategyRes> {
    const transactionId = `${msg.transactionId}`;
    Logger.info(`[OtpStrategy] sendOtp START -- txId: ${transactionId}, txType: ${this.txType}`);

    this.validateSendRequest(request);

    const accountNumber = this.getAccountNumber(request);
    const pfx = `[OtpStrategy] txId: ${transactionId}, txType: ${this.txType}, acc: ${accountNumber}`;
    Logger.info(`${pfx} -- sendOtp validated, accountNumber resolved`);

    await this.checkLock(accountNumber, pfx);

    const validation = await this.getRateLimit(accountNumber, pfx);
    if (validation.count >= this.maxResendPerSession) {
      await this.setLock(accountNumber);
      validation.count = 0;
      await this.updateRateLimit(validation);
      Logger.warn(`${pfx} -- max resend reached, account LOCKED for ${this.lockDurationSeconds}s (auto-unlock)`);
      throw new Errors.GeneralError(OTP_MAX_RESEND);
    }
    this.checkRateLimitRules(validation, pfx);

    Logger.info(`${pfx} -- resolving phone number via Kafka`);
    const phoneNumber = await this.resolvePhoneNumber(request, transactionId);
    const smsPhoneNumber = this.formatPhoneNumberForSms(phoneNumber);
    Logger.info(`${pfx} -- phone number resolved: ${phoneNumber ? phoneNumber.replace(/.(?=.{4})/g, '*') : 'N/A'}`);

    const otpCode = this.generateOtpCode(this.otpLength);
    const otpId = uuid.v4();
    Logger.info(`${pfx} -- OTP generated, otpId: ${otpId}, ttl: ${this.ttlSeconds}s`);

    const otpData: IOtpData = {
      phoneNumber,
      otp: otpCode,
      txType: this.txType,
      accountNumber,
    };
    await this.storeOtp(otpId, otpData);
    Logger.info(`${pfx} -- OTP stored in Redis, otpId: ${otpId} - ${otpCode}`);

    validation.count += 1;
    validation.latestRequest = Date.now();
    await this.updateRateLimit(validation);
    Logger.info(`${pfx} -- rate limit updated, count: ${validation.count}/${this.maxResendPerSession}`);

    const smsData = this.createSmsTemplateData(otpCode);
    const locale = request.headers?.['accept-language'] || 'vi';
    this.sendSmsByPhoneNumber(smsData, smsPhoneNumber, locale);
    Logger.info(`${pfx} -- SMS sent, locale: ${locale}`);

    const expiredTime = new Date(Date.now() + this.ttlSeconds * 1000);
    const resendRemaining = this.maxResendPerSession - validation.count;
    Logger.info(`${pfx} -- sendOtp SUCCESS, otpId: ${otpId}, resendRemaining: ${resendRemaining}`);
    return {
      otpId,
      expiredTime: expiredTime.toISOString(),
      resendRemaining,
      phoneNumber,
    };
  }

  async verifyOtp(request: IVerifyOtpStrategyReq, msg: Kafka.IMessage): Promise<IVerifyOtpStrategyRes> {
    const transactionId = `${msg.transactionId}`;
    Logger.info(`[OtpStrategy] verifyOtp START -- txId: ${transactionId}, otpId: ${request.otpId}`);

    this.validateVerifyRequest(request);

    const otpData = await this.findOtp(request.otpId);
    const pfx = `[OtpStrategy] txId: ${transactionId}, txType: ${this.txType}, acc: ${otpData.accountNumber}`;
    Logger.info(`${pfx} -- OTP data found for otpId: ${request.otpId}`);

    await this.checkLock(otpData.accountNumber, pfx);

    const validation = await this.getRateLimit(otpData.accountNumber, pfx);

    if (otpData.otp !== request.otpValue) {
      validation.failedCount += 1;
      await this.updateRateLimit(validation);
      Logger.warn(`${pfx} -- OTP mismatch, failedCount: ${validation.failedCount}/${this.maxFailAttempts}`);

      if (validation.failedCount >= this.maxFailAttempts) {
        await this.setLock(otpData.accountNumber);
        Logger.warn(`${pfx} -- account LOCKED for ${this.lockDurationSeconds}s due to max failed attempts`);
        throw new Errors.GeneralError(OTP_INCORRECT_MAX);
      }
      throw new Errors.GeneralError(OTP_INCORRECT);
    }

    validation.failedCount = 0;
    await this.updateRateLimit(validation);
    await redis.del(Category.OTP_STRATEGY, request.otpId);

    Logger.info(`${pfx} -- verifyOtp SUCCESS, otpId: ${request.otpId} cleaned up`);
    return { success: true };
  }

  protected async checkLock(accountNumber: string, pfx?: string): Promise<void> {
    let isLocked = false;
    try {
      isLocked = await redis.get<boolean>(Category.OTP_STRATEGY, `${accountNumber}_LOCKED`);
    } catch {
      // not found = not locked
    }
    if (isLocked) {
      Logger.warn(`${pfx || '[OtpStrategy]'} -- account is LOCKED, rejecting request`);
      throw new Errors.GeneralError(OTP_LOCKED);
    }
  }

  protected async getRateLimit(accountNumber: string, pfx?: string): Promise<IOtpValidation> {
    const key = `${accountNumber}_${moment().format('YYYYMMDD')}`;
    try {
      const validation = await redis.get<IOtpValidation>(Category.OTP_STRATEGY, key);
      Logger.info(`${pfx || '[OtpStrategy]'} -- rateLimit loaded: count=${validation.count}, failedCount=${validation.failedCount}`);
      return validation;
    } catch {
      Logger.info(`${pfx || '[OtpStrategy]'} -- no rateLimit record found, initializing new one`);
      return { accountNumber, count: 0, failedCount: 0, latestRequest: null };
    }
  }

  protected checkRateLimitRules(validation: IOtpValidation, pfx?: string): void {
    if (validation.latestRequest) {
      const elapsed = (Date.now() - validation.latestRequest) / 1000;
      if (elapsed < this.minIntervalSeconds) {
        Logger.warn(`${pfx || '[OtpStrategy]'} -- OTP requested too fast, elapsed: ${elapsed.toFixed(1)}s < min: ${this.minIntervalSeconds}s`);
        throw new Errors.GeneralError(OTP_GENERATE_TOO_FAST);
      }
    }
    if (validation.count >= this.maxResendPerSession) {
      Logger.warn(`${pfx || '[OtpStrategy]'} -- max resend reached: ${validation.count}/${this.maxResendPerSession}`);
      throw new Errors.GeneralError(OTP_MAX_RESEND);
    }
    if (validation.failedCount >= this.maxFailAttempts) {
      Logger.warn(`${pfx || '[OtpStrategy]'} -- max fail attempts reached: ${validation.failedCount}/${this.maxFailAttempts}, account locked`);
      throw new Errors.GeneralError(OTP_LOCKED);
    }
  }

  protected async updateRateLimit(validation: IOtpValidation): Promise<void> {
    const key = `${validation.accountNumber}_${moment().format('YYYYMMDD')}`;
    await redis.set(Category.OTP_STRATEGY, key, validation, 86400);
  }

  protected async storeOtp(otpId: string, otpData: IOtpData): Promise<void> {
    await redis.set(Category.OTP_STRATEGY, otpId, otpData, this.ttlSeconds);
  }

  protected async findOtp(otpId: string): Promise<IOtpData> {
    try {
      const data = await redis.get<IOtpData>(Category.OTP_STRATEGY, otpId);
      if (!data) {
        Logger.warn(`[OtpStrategy] -- OTP not found (expired or invalid), otpId: ${otpId}`);
        throw new Errors.GeneralError(OTP_EXPIRED);
      }
      return data;
    } catch (e) {
      if (e instanceof Errors.GeneralError) {
        throw e;
      }
      Logger.warn(`[OtpStrategy] -- error finding OTP, otpId: ${otpId}`, e);
      throw new Errors.GeneralError(OTP_EXPIRED);
    }
  }

  protected async setLock(accountNumber: string): Promise<void> {
    Logger.warn(`[OtpStrategy] -- setting lock for accountNumber: ${accountNumber}, duration: ${this.lockDurationSeconds}s`);
    await redis.set(Category.OTP_STRATEGY, `${accountNumber}_LOCKED`, true, this.lockDurationSeconds);
  }

  protected generateOtpCode(length: number): string {
    let code = '';
    for (let i = 0; i < length; i++) {
      code += Math.floor(Math.random() * 10).toString();
    }
    return code;
  }

  protected formatPhoneNumberForSms(phoneNumber: string): string {
    let formatted = phoneNumber;
    if (formatted.startsWith('0')) {
      formatted = `84${formatted.substring(1)}`;
    }
    if (formatted.startsWith('+')) {
      formatted = formatted.substring(1);
    }
    return formatted;
  }

  protected sendSmsByPhoneNumber(
    smsData: TradexNotification.ITemplateData,
    phoneNumber: string,
    locale: string,
  ): void {
    sendSms(smsData, phoneNumber, conf.domain, locale);
  }
}
