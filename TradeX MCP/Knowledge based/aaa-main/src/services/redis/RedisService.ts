import * as redis from 'redis';
import conf from '../../conf';


const DataType = {
  UNDEFINED: 'a',
  NULL: 'b',
  BOOLEAN: '0',
  STRING: '1',
  NUMBER: '2',
  DATE: '3',
  OBJECT: '4',
};

export const Category = {
  TOKEN: 'catToken',
  OTP: 'catOtp',
  PIN: 'catPin',
  REMEMBER_ORDER_PASS: 'catRememberOrderPass',
  USER_INFO: 'catUserInfo',
  USER_ACC_INFO: 'catUserAccInfo',
  USER_TOKEN_MAP: 'catUserTokenMap',
  LAST_TOKEN_ID: 'catLastTokenId',
  BANK_INFO: 'catBankInfo',
  BANK_BRANCH_INFO: 'catBankBranchInfo',
  TIME_QUERY: 'catTime',
  CW_INFO: 'catCW',
  FUTURE_INFO: 'catFuture',
  INDEX_STOCK_LIST: 'catIndexStockList',
  PAGING_STORE: 'pagingStore',
  OTP_INFO: 'otpInfo',
  TOKEN_OPERATOR: 'masTokenOperator',
};

export let client = redis.createClient(conf.redis.port, conf.redis.host, conf.redis.options);

export class KeyNotExist extends Error {
  constructor(key: string) {
    super(`key ${key} is not existed`);
  }
}

export async function init(): Promise<void> {
  client = redis.createClient(conf.redis.port, conf.redis.host, conf.redis.options);
    client.on('error', console.error);
    return new Promise((resolve) => {
      client.on('ready', () => {
        resolve();
      });
    });
}

export async function set<T>(
  category: string,
  key: string,
  value: T,
  isNotSetCluster?: boolean,
  durationInSeconds?: number
): Promise<void> {
  let valueAsString = null;
  if (value === null) {
    valueAsString = `${DataType.NULL}${value}`;
  } else if (value === undefined) {
    valueAsString = `${DataType.UNDEFINED}${value}`;
  } else if (typeof value === 'boolean') {
    valueAsString = `${DataType.BOOLEAN}${value ? '1' : '0'}`;
  } else if (typeof value === 'string') {
    valueAsString = `${DataType.STRING}${value}`;
  } else if (typeof value === 'number') {
    valueAsString = `${DataType.NUMBER}${value}`;
  } else if (value instanceof Date) {
    valueAsString = `${DataType.DATE}${((value as unknown) as Date).getTime()}`;
  } else {
    valueAsString = `${DataType.OBJECT}${JSON.stringify(value)}`;
  }
  const duration = durationInSeconds == null ? conf.defaultLongRedisDuration : durationInSeconds;
  return new Promise((resolve, reject) => {
    client.set(
      getRedisKey(category, key, isNotSetCluster),
      valueAsString,
      'EX',
      duration,
      (err: Error | null) => handleSet(resolve, reject, err)
    );
  });
}

function handleSet(resolve, reject, err: Error | null) {
  if (err != null) {
    reject(err);
  } else {
    resolve();
  }
}

export async function get<T>(category: string, key: string, isNotSetCluster?: boolean): Promise<T> {
  return new Promise((resolve, reject) => {
    const redisKey = getRedisKey(category, key, isNotSetCluster);
    client.get(redisKey, (err: Error | null, reply: string) => {
      if (err != null) {
        reject(err);
      } else {
        if (reply == null) {
          reject(new KeyNotExist(redisKey));
        } else {
          const type = reply[0];
          if (type === DataType.NULL) {
            resolve(null);
          } else if (type === DataType.UNDEFINED) {
            resolve(undefined);
          } else if (type === DataType.BOOLEAN) {
            const content = reply.substr(1);
            resolve(((content === '1') as unknown) as T);
          } else if (type === DataType.STRING) {
            const content = reply.substr(1);
            resolve((content as unknown) as T);
          } else if (type === DataType.NUMBER) {
            const content = reply.substr(1);
            resolve((Number(content) as unknown) as T);
          } else if (type === DataType.DATE) {
            const content = reply.substr(1);
            resolve((new Date(Number(content)) as unknown) as T);
          } else {
            const content = reply.substr(1);
            resolve(JSON.parse(content));
          }
        }
      }
    });
  });
}

export async function del(category: string, key: string): Promise<boolean> {
  const redisKey = getRedisKey(category, key);
  return client.del(redisKey, (err: Error | null) => {
    if (err != null) {
      throw err;
    }
  });
}

export function getRedisKey(category: string, key: string, isNotSetCluster?: boolean): string {
  if (isNotSetCluster) {
    return `${category}_${key}`;
  } else {
    return `${conf.clusterId}_${category}_${key}`;
  }
}

export async function hget<T>(key: string, field: string): Promise<T> {
  return new Promise((resolve, reject) => {
    client.hget(key, field, (err: Error | null, reply: string) => {
      if (err != null) {
        reject(err);
      } else {
        if (reply == null) {
          resolve(null);
        } else {
          const type = reply[0];
          if (type === DataType.NULL) {
            resolve(null);
          } else if (type === DataType.UNDEFINED) {
            resolve(undefined);
          } else if (typeof type === DataType.BOOLEAN) {
            const content = reply.substr(1);
            resolve(((content === '1') as unknown) as T);
          } else if (typeof type === DataType.STRING) {
            const content = reply.substr(1);
            resolve((content as unknown) as T);
          } else if (typeof type === DataType.NUMBER) {
            const content = reply.substr(1);
            resolve((Number(content) as unknown) as T);
          } else if (type === DataType.DATE) {
            const content = reply.substr(1);
            resolve((new Date(Number(content)) as unknown) as T);
          } else {
            const content = reply.substr(1);
            resolve(JSON.parse(content, receiver));
          }
        }
      }
    });
  });
}

export function receiver(key: string, value: string): Date | string {
  const dateFormat = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/;
  if (typeof value === 'string' && dateFormat.test(value)) {
    return new Date(value);
  }
  return value;
}

export async function exists(key: string): Promise<boolean> {
  return new Promise((resolve, reject) => {
    client.exists(key, (err, reply) => {
      if (err != null) {
        reject(err);
      } else {
        resolve(reply);
      }
    });
  });
}

export async function hset<T>(key: string, field: string, value: T): Promise<void> {
  let valueAsString = null;
  if (value === null) {
    valueAsString = `${DataType.NULL}${value}`;
  } else if (value === undefined) {
    valueAsString = `${DataType.UNDEFINED}${value}`;
  } else if (typeof value === 'boolean') {
    valueAsString = `${DataType.BOOLEAN}${value ? '1' : '0'}`;
  } else if (typeof value === 'string') {
    valueAsString = `${DataType.STRING}${value}`;
  } else if (typeof value === 'number') {
    valueAsString = `${DataType.NUMBER}${value}`;
  } else if (value instanceof Date) {
    valueAsString = `${DataType.DATE}${((value as unknown) as Date).getTime()}`;
  } else {
    valueAsString = `${DataType.OBJECT}${JSON.stringify(value)}`;
  }
  return new Promise((resolve, reject) => {
    client.hset(key, field, valueAsString, (err: Error | null) => {
      if (err != null) {
        reject(err);
      } else {
        resolve();
      }
    });
  });
}
