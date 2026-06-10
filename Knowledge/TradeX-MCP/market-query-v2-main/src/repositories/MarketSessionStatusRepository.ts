import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { COLLECTIONS_NAME } from '../constants';
import { Cursor, FilterQuery } from 'mongodb';
import { IMarketSessionStatus } from '../models/db/IMarketSessionStatus';

@Service()
export default class MarketSessionStatusRepository {
  public queryBy(filter: FilterQuery<IMarketSessionStatus>): Cursor<IMarketSessionStatus> {
    return getDb().collection(COLLECTIONS_NAME.MARKET_SESSION_STATUS).find(filter);
  }
}
