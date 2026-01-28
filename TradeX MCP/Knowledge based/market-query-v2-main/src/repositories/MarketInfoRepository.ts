import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { FilterQuery, UpdateQuery, UpdateWriteOpResult } from 'mongodb';
import { COLLECTIONS_NAME } from '../constants';
import { IMarketInfo } from '../models/db/IMarketInfo';

@Service()
export class MarketInfoRepository {
  public findOne(query: FilterQuery<IMarketInfo>): Promise<IMarketInfo> {
    return getDb().collection(COLLECTIONS_NAME.MARKET_INFO).findOne(query);
  }

  public updateLastTradingDate(filter: FilterQuery<IMarketInfo>, update: UpdateQuery<IMarketInfo>): Promise<UpdateWriteOpResult> {
    return getDb().collection(COLLECTIONS_NAME.MARKET_INFO).updateOne(filter, update, { upsert: true });
  }

  public updateOne(filter: FilterQuery<IMarketInfo>, update: UpdateQuery<IMarketInfo>): Promise<UpdateWriteOpResult> {
    return getDb().collection(COLLECTIONS_NAME.MARKET_INFO).updateOne(filter, update, { upsert: true });
  }
}
