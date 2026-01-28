import { Service } from 'typedi';
import { IIndexStockList } from '../models/db/IIndexStockList';
import { getDb } from '../utils/dbConnection';
import { COLLECTIONS_NAME } from '../constants';
import { FilterQuery, Cursor, UpdateQuery, UpdateWriteOpResult } from 'mongodb';

@Service()
export class IndexStockListRepository {
  public findBy(query: FilterQuery<IIndexStockList>): Cursor<IIndexStockList> {
    return getDb().collection(COLLECTIONS_NAME.INDEX_STOCK_LIST).find(query);
  }

  public findOneBy(query: FilterQuery<IIndexStockList>): Promise<IIndexStockList> {
    return getDb().collection(COLLECTIONS_NAME.INDEX_STOCK_LIST).findOne(query);
  }

  public updateIndexStockList(filter: FilterQuery<IIndexStockList>, update: UpdateQuery<IIndexStockList>): Promise<UpdateWriteOpResult> {
    return getDb().collection(COLLECTIONS_NAME.INDEX_STOCK_LIST).updateOne(filter, update, { upsert: true });
  }
}
