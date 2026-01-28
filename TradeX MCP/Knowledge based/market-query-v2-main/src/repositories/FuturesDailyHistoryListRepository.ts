import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { COLLECTIONS_NAME } from '../constants';
import { InsertOneWriteOpResult } from 'mongodb';
import { IFuturesDailyListHistory } from '../models/db/IFuturesDailyListHistory';

@Service()
export class FuturesDailyHistoryListRepository {
  public async insertDailyFuturesList(data: IFuturesDailyListHistory): Promise<InsertOneWriteOpResult<IFuturesDailyListHistory>> {
    return getDb().collection(COLLECTIONS_NAME.FUTURES_DAILY_LIST_HISTORY).insertOne(data);
  }
}
