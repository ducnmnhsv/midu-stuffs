export interface ICache<T> {
  createdTime: number; // time in ms
  expiredTime: number; // time in ms
  data: T;
}
