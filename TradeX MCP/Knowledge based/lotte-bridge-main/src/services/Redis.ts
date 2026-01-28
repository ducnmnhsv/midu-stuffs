import { Inject, Service } from 'typedi';
import RedisService from '../init/RedisService';
import { Logger, Utils } from 'tradex-common';
import { IContext } from '../models/IContext';
import { IOtpData } from '../models/IOtpData';
import { IOtpValidation } from '../models/IOtpValidation';

const { DATE_DISPLAY_FORMAT, formatDateToDisplay } = Utils;

export const Category = {
  USER_ACC_INFO: 'catUserAccInfo',
  PAGING_STORE: 'pagingStore',
  VERIFY_USER_DATA: 'catVerifyUserData',
  AUTH_OTP_DATA: 'catAuthOtpData',
  BANK_ACCOUNT: 'catBankAccount',
  RESET_PASSWORD_DATA: 'catResetPasswordData',
  OTP_VALIDATE: 'catOtpValidate',
  OTP: 'catOtp',
  SYMBOL_INFO: 'realtime_mapSymbolInfo',
  SYMBOL_INFO_ODD_LOT: 'realtime_mapSymbolInfoOddLot',
};

@Service()
export default class Redis {
  @Inject()
  private readonly core: RedisService;

  async set<T>(
    category: string,
    key: string,
    value: T,
    durationInSeconds?: number,
    isNotSetCluster?: boolean
  ): Promise<void> {
    return this.core.set(category, key, value, isNotSetCluster, durationInSeconds);
  }

  async get<T>(category: string, key: string, isNotSetCluster?: boolean): Promise<T> {
    return this.core.get(category, key, isNotSetCluster);
  }

  async del(category: string, key: string): Promise<boolean> {
    return this.core.del(category, key);
  }

  getRedisKey(category: string, key: string): string {
    return this.core.getRedisKey(category, key);
  }

  async exists(key: string): Promise<boolean> {
    return this.core.exists(key);
  }

  async getOtpValidate(id: string, ctx: IContext) {
    const realKey = `${id}_${formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT)}`;
    try {
      const otpValidate = await this.get<IOtpValidation>(Category.OTP_VALIDATE, realKey);
      return otpValidate;
    } catch (err) {
      Logger.error(ctx.id, 'get otp validate error ', err);
      return null;
    }
  }

  async setOtpValidate(id: string, otpValidate: IOtpValidation) {
    const realKey = `${id}_${formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT)}`;
    await this.set<IOtpValidation>(Category.OTP_VALIDATE, realKey, otpValidate, 3600 * 24);
  }

  async getResetPasswordData(otpKey: string, ctx: IContext) {
    try {
      const otpData = await this.get<IOtpData>(Category.RESET_PASSWORD_DATA, otpKey);
      return otpData;
    } catch (err) {
      Logger.error(ctx.id, 'get otp data error ', err);
      return null;
    }
  }

  async hget<T>(category: string, key: string): Promise<T> {
    return this.core.hget(category, key);
  }

  async hset<T>(category: string, key: string, value: T): Promise<void> {
    return this.core.hset(category, key, value);
  }
}
