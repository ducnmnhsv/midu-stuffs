import { Service } from 'typedi';
import { IForeignerDaily } from '../models/db/IForeignerDaily';
import { FilterQuery, Cursor } from 'mongodb';
import { DEFAULT_PAGE_SIZE, COLLECTIONS_NAME } from '../constants';
import { getDb } from '../utils/dbConnection';

@Service()
export class ForeignerDailyRepository {
  public findBy(query: FilterQuery<IForeignerDaily>, limit: number = DEFAULT_PAGE_SIZE, sort: any = {}): Cursor<IForeignerDaily> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_FOREIGNER_DAILY).find(query).limit(limit).sort(sort);
  }
}
