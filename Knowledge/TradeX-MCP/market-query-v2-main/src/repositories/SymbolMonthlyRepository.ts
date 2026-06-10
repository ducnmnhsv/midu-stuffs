import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { Cursor, FilterQuery, BulkWriteResult } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_PAGE_SIZE } from '../constants';
import { ISymbolMonthly } from '../models/db/ISymbolMonthly';

@Service()
export class SymbolMonthlyRepository {
  public findBy(query: FilterQuery<ISymbolMonthly>, limit: number = DEFAULT_PAGE_SIZE, skip: number = 0, sort: any = {}): Cursor<ISymbolMonthly> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_MONTHLY).find(query).sort(sort).skip(skip).limit(limit);
  }
  public async updateByBulk(listSymbolMonthly: ISymbolMonthly[]): Promise<BulkWriteResult> {
    const bulk = getDb().collection(COLLECTIONS_NAME.SYMBOL_MONTHLY).initializeOrderedBulkOp();
    for (let i = 0; i < listSymbolMonthly.length; i++) {
      const symbolMonthly = listSymbolMonthly[i];
      bulk.find({ _id: symbolMonthly._id }).updateOne(symbolMonthly);
    }
    return bulk.execute();
  }
}
