import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { Cursor, FilterQuery, BulkWriteResult } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_PAGE_SIZE } from '../constants';
import { ISymbolWeeklyOrMonthly } from '../models/db/ISymbolWeeklyOrMonthly';

@Service()
export class SymbolWeeklyRepository {
  public findBy(
    query: FilterQuery<ISymbolWeeklyOrMonthly>,
    limit: number = DEFAULT_PAGE_SIZE,
    skip: number = 0,
    sort: any = {},
  ): Cursor<ISymbolWeeklyOrMonthly> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_WEEKLY).find(query).sort(sort).skip(skip).limit(limit);
  }
  public async updateByBulk(listSymbolWeekly: ISymbolWeeklyOrMonthly[]): Promise<BulkWriteResult> {
    const bulk = getDb().collection(COLLECTIONS_NAME.SYMBOL_WEEKLY).initializeOrderedBulkOp();
    for (let i = 0; i < listSymbolWeekly.length; i++) {
      const symbolWeekly = listSymbolWeekly[i];
      bulk.find({ _id: symbolWeekly._id }).updateOne(symbolWeekly);
    }
    return bulk.execute();
  }
}
