import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { Cursor, FilterQuery } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_PAGE_SIZE } from '../constants';
import { IEtfIndexDaily } from '../models/db/IEtfIndexDaily';

@Service()
export class EtfIndexDailyRepository {
  public findBy(query: FilterQuery<IEtfIndexDaily>, limit: number = DEFAULT_PAGE_SIZE, sort: any = {}): Cursor<IEtfIndexDaily> {
    return getDb().collection(COLLECTIONS_NAME.ETF_INDEX_DAILY).find(query).limit(limit).sort(sort);
  }
}
