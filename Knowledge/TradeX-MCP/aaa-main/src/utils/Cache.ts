import { ICache } from "../models/ICache";

export default class Cache<T> {
  private dataMap: Map<string, ICache<T>> = new Map();

  public findOrget = async (key: string, getFunc: (s: string) => Promise<T>, timeOutInMs: number) => {
    const data: ICache<T> = this.dataMap.get(key);
    if (this.isValid(data)) {
      return data.data;
    }
    this.insertCache(key, await getFunc(key), timeOutInMs);
    return this.find(key);
  };

  private insertCache = (key: string, data: T, timeOutInMs: number) => {
    const time: number = new Date().getTime();
    this.dataMap.set(key, {
      createdTime: time,
      expiredTime: time + timeOutInMs,
      data,
    });
  };

  private find = (key: string) => {
    const data: ICache<T> = this.dataMap.get(key);
    if (this.isValid(data)) {
      return data.data;
    } else {
      delete this.dataMap[key];
      return null;
    }
  };

  private isValid = (data: ICache<T>) => data && data.expiredTime > new Date().getTime();
}
