
import * as core from './RedisService';

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
};

export async function set<T>(
  category: string,
  key: string,
  value: T,
  durationInSeconds?: number,
  isNotSetCluster?: boolean
): Promise<void> {
  return core.set(category, key, value, isNotSetCluster, durationInSeconds);
}

export async function get<T>(category: string, key: string, isNotSetCluster?: boolean): Promise<T> {
  return core.get(category, key, isNotSetCluster);
}

export async function del(category: string, key: string): Promise<boolean> {
  return core.del(category, key);
}

export function getRedisKey(category: string, key: string): string {
  return core.getRedisKey(category, key);
}

export function getRedisKeyWithCluster(category: string, key: string, isNotSetCluster?: boolean): string {
  return core.getRedisKey(category, key, isNotSetCluster);
}

export async function exists(key: string): Promise<boolean> {
  return core.exists(key);
}

export async function hget<T>(category: string, key: string): Promise<T> {
  return core.hget(category, key);
}

export async function hset<T>(category: string, key: string, value: T): Promise<void> {
  return core.hset(category, key, value);
}
