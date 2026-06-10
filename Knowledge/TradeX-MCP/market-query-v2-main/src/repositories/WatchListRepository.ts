import { Service } from 'typedi';
import { COLLECTIONS_NAME } from '../constants';
import { IWatchList } from '../models/db/IWatchList';
import { getDb } from '../utils/dbConnection';
import { FilterQuery, InsertOneWriteOpResult, UpdateWriteOpResult } from 'mongodb';
import * as crypto from 'crypto';

@Service()
export class WatchListRepository {
  public findBy(query: FilterQuery<any>) {
    return getDb().collection(COLLECTIONS_NAME.WATCH_LIST).find(query);
  }

  public findOneBy(query: FilterQuery<any>): Promise<IWatchList> {
    return getDb().collection(COLLECTIONS_NAME.WATCH_LIST).findOne(query);
  }

  public async save(data: IWatchList): Promise<InsertOneWriteOpResult<any>> {
    const currentDate = new Date();
    const baseId = Math.floor(currentDate.getTime() / 1000);
    const buffer = crypto.randomBytes(4);
    data._id = `${data.username}_${baseId}_${buffer.readUInt32BE(0)}`;
    return getDb().collection(COLLECTIONS_NAME.WATCH_LIST).insertOne(data);
  }

  public update(filter: any, dataUpdate: any): Promise<UpdateWriteOpResult> {
    return getDb().collection(COLLECTIONS_NAME.WATCH_LIST).updateOne(filter, dataUpdate);
  }

  public updateMany(filter: any, dataUpdate: any): Promise<UpdateWriteOpResult> {
    return getDb().collection(COLLECTIONS_NAME.WATCH_LIST).updateMany(filter, dataUpdate);
  }
}
