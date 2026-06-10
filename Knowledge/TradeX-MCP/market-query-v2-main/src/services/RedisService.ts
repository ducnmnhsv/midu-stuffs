import { createClient, RedisClientType, RedisFunctions, RedisModules, RedisScripts } from 'redis';
import config from '../config';
import { Service } from 'typedi';
import { ISymbolInfo } from '../models/db/ISymbolInfo';
import { Logger } from 'tradex-common';
import Base from '../models/redis/Base';

const DATA_TYPE = {
  UNDEFINED: 'a',
  NULL: 'b',
  BOOLEAN: '0',
  STRING: '1',
  NUMBER: '2',
  DATE: '3',
  OBJECT: '4',
};

export const REDIS_KEY = {
  SYMBOL_INFO: 'realtime_mapSymbolInfo',
  SYMBOL_INFO_ODD_LOT: 'realtime_mapSymbolInfoOddLot',
  SYMBOL_DAILY: 'realtime_mapSymbolDaily',
  FOREIGNER_DAILY: 'realtime_mapForeignerDaily',
  SYMBOL_QUOTE: 'realtime_listQuote',
  SYMBOL_QUOTE_META: 'realtime_listQuoteMeta',
  SYMBOL_STATISTICS: 'realtime_mapSymbolStatistic',
  SYMBOL_BID_OFFER: 'realtime_listBidOffer',
  SYMBOL_QUOTE_MINUTE: 'realtime_listQuoteMinute',
  DEAL_NOTICE: 'realtime_listDealNotice',
  ADVERTISED: 'realtime_listAdvertised',
  MARKET_STATUS: 'realtime_mapMarketStatus',
  SYMBOL_INFO_EXTEND: 'symbolInfoExtend',
  STOCK_RANKING_PERIOD: 'market_rise_false_stock_ranking',
  SYMBOL_STOCK_RIGHT: 'market_right_info',
  NOTIFICATION: 'notice_',
};

export class KeyNotExist extends Error {
  constructor(key: string) {
    super(`key ${key} is not existed`);
  }
}

type RedisReadyCallback = () => void;

@Service()
export default class RedisService {
  private client: RedisClientType<RedisModules, RedisFunctions, RedisScripts>;
  private readyCallbacks: RedisReadyCallback[] = [];

  public async init(): Promise<void> {
    this.client = await createClient(config.redis)
      .on('error', (err) => {
        Logger.error('error on redis connection', err.message);
      })
      .on('reconnecting', () => {
        Logger.warn('redis connection is reconnecting');
      })
      .on('ready', () => {
        Logger.info('redis connection is ready', this.readyCallbacks.length);
        const cbs = this.readyCallbacks;
        this.readyCallbacks = [];
        cbs.forEach((cb) => cb());
      })
      .connect();
  }

  public async set<T>(key: string, value: T): Promise<string> {
    await this.waitForReady();
    return this.client.SET(key, formatData(value));
  }

  public async setEn<T extends Base>(key: string, value: T): Promise<string> {
    await this.waitForReady();
    return this.client.SET(key, value.encode());
  }

  public async rpush<T>(key: string, value: T): Promise<number> {
    await this.waitForReady();
    return this.client.RPUSH(key, formatData(value));
  }

  public async hset<T>(key: string, field: string, value: T): Promise<number> {
    let valueAsString = formatData(value);
    await this.waitForReady();
    return this.client.HSET(key, field, valueAsString);
  }

  public async lrange<T>(key: string, start: number, end: number): Promise<T[]> {
    await this.waitForReady();
    return (await this.client.LRANGE(key, start, end)).map(parseData<T>);
  }

  public async get<T>(key: string): Promise<T> {
    await this.waitForReady();
    return this.client.GET(key).then(parseData<T>);
  }

  public async getDe<T extends Base>(key: string, emptyInstance: T): Promise<T> {
    await this.waitForReady();
    const data = await this.client.GET(key);
    if (data == null) {
      return null;
    }
    emptyInstance.decode(data);
    return emptyInstance;
  }

  public async llen(key: string): Promise<number> {
    await this.waitForReady();
    return this.client.LLEN(key);
  }

  public async hlen(key: string): Promise<number> {
    await this.waitForReady();
    return this.client.HLEN(key);
  }

  public async isExists(key: string): Promise<boolean> {
    await this.waitForReady();
    return (await this.client.exists(key)) > 0;
  }

  public async hExists(key: string, field: string): Promise<boolean> {
    await this.waitForReady();
    return this.client.HEXISTS(key, field);
  }

  public async hget<T>(key: string, field: string): Promise<T> {
    await this.waitForReady();
    const reply: string = await this.client.HGET(key, field);
    return parseData<T>(reply);
  }

  public async hmget<T>(key: string, fields: string[]): Promise<T[]> {
    await this.waitForReady();
    const reply: string[] = await this.client.HMGET(key, fields);
    return reply.map(parseData<T>);
  }

  public async hgetall<T>(key: string): Promise<T[]> {
    await this.waitForReady();
    const reply: { [k: string]: string } = await this.client.HGETALL(key);
    return Object.values(reply).map(parseData<T>);
  }

  public async getSymbolInfo(symbol: string): Promise<ISymbolInfo> {
    await this.waitForReady();
    return this.hget<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO, symbol);
  }

  private waitForReady(timeout: number = 5000): Promise<null> {
    if (this.client.isReady) {
      return Promise.resolve(null);
    }
    return new Promise((resolve, reject) => {
      let finish = false;
      const timeOutHandler = setTimeout(() => {
        if (finish) {
          return;
        }
        finish = true;
        reject(new Error('redis connection is not ready'));
      }, timeout);
      this.readyCallbacks.push(() => {
        if (finish) {
          return;
        }
        finish = true;
        try {
          resolve(null);
        } catch (err) {
          Logger.error('fail to resolve result for waitd redis ready', err);
        }
        try {
          clearTimeout(timeOutHandler);
        } catch (err) {
          // swallow
        }
      });
    });
  }
}

function parseData<T>(value: string): T {
  if (value == null) return null as T;
  const type = value[0];
  if (type === DATA_TYPE.NULL) {
    return null as T;
  } else if (type === DATA_TYPE.UNDEFINED) {
    return undefined as T;
  } else if (typeof type === DATA_TYPE.BOOLEAN) {
    const content = value.substr(1);
    return (content === '1') as unknown as T;
  } else if (typeof type === DATA_TYPE.STRING) {
    const content = value.substr(1);
    return content as unknown as T;
  } else if (typeof type === DATA_TYPE.NUMBER) {
    const content = value.substr(1);
    return Number(content) as unknown as T;
  } else if (type === DATA_TYPE.DATE) {
    const content = value.substr(1);
    return new Date(Number(content)) as unknown as T;
  } else {
    const content = value.substr(1);
    return JSON.parse(content, receiver) as T;
  }
}

function formatData<T>(value: T): string {
  if (value === null) {
    return `${DATA_TYPE.NULL}${value}`;
  } else if (value === undefined) {
    return `${DATA_TYPE.UNDEFINED}${value}`;
  } else if (typeof value === 'boolean') {
    return `${DATA_TYPE.BOOLEAN}${value ? '1' : '0'}`;
  } else if (typeof value === 'string') {
    return `${DATA_TYPE.STRING}${value}`;
  } else if (typeof value === 'number') {
    return `${DATA_TYPE.NUMBER}${value}`;
  } else if (value instanceof Date) {
    return `${DATA_TYPE.DATE}${(value as unknown as Date).getTime()}`;
  } else {
    return `${DATA_TYPE.OBJECT}${JSON.stringify(value)}`;
  }
}

function receiver(key: string, value: string): any {
  const dateFormat = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/;
  if (typeof value === 'string' && dateFormat.test(value)) {
    return new Date(value);
  }
  return value;
}
