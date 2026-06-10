import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { Cursor, FilterQuery } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_PAGE_SIZE } from '../constants';
import { IEtfNavDaily } from '../models/db/IEtfNavDaily';

@Service()
export class EtfNavDailyRepository {
  public findBy(query: FilterQuery<IEtfNavDaily>, limit: number = DEFAULT_PAGE_SIZE, sort: any = {}): Cursor<IEtfNavDaily> {
    return getDb().collection(COLLECTIONS_NAME.ETF_NAV_DAILY).find(query).limit(limit).sort(sort);
  }
}
