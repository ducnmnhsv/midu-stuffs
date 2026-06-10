import { Service } from 'typedi';
import { FilterQuery, Cursor, BulkWriteResult, UnorderedBulkOperation, UpdateQuery, AggregationCursor } from 'mongodb';
import { DEFAULT_PAGE_SIZE, COLLECTIONS_NAME } from '../constants';
import { getDb } from '../utils/dbConnection';
import { ISymbolQuoteMinutes } from '../models/db/ISymbolQuoteMinutes';

@Service()
export class SymbolQuoteMinutesRepository {
  public createBulk(): UnorderedBulkOperation {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE_MINUTE).initializeUnorderedBulkOp();
  }

  public findBy(filter: FilterQuery<ISymbolQuoteMinutes>, limit: number = DEFAULT_PAGE_SIZE, sort: any = {}): Cursor<ISymbolQuoteMinutes> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE_MINUTE).find(filter).sort(sort).limit(limit);
  }

  public findByNoPage(filter: FilterQuery<ISymbolQuoteMinutes>, sort: any = {}): Cursor<ISymbolQuoteMinutes> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE_MINUTE).find(filter).sort(sort);
  }

  public findTopWithMatch(topN: number, filter: FilterQuery<ISymbolQuoteMinutes>, sort: any = {}): AggregationCursor<ISymbolQuoteMinutes> {
    return getDb()
      .collection(COLLECTIONS_NAME.SYMBOL_QUOTE_MINUTE)
      .aggregate([{ $match: filter }, { $sort: sort }, { $limit: topN }]);
  }

  public async updateByBulk(listSymbolMinute: ISymbolQuoteMinutes[]): Promise<BulkWriteResult> {
    const bulk = getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE_MINUTE).initializeOrderedBulkOp();
    for (let i = 0; i < listSymbolMinute.length; i++) {
      const symbolMinute = listSymbolMinute[i];
      bulk.find({ _id: symbolMinute._id }).updateOne(symbolMinute);
    }
    return bulk.execute();
  }

  public updateMany(filter: FilterQuery<ISymbolQuoteMinutes>, updateQuery: UpdateQuery<ISymbolQuoteMinutes>): Promise<any> {
    return new Promise((resolve: Function, reject: Function) => {
      getDb()
        .collection(COLLECTIONS_NAME.SYMBOL_DAILY)
        .updateMany(filter, updateQuery, (err: any, res: any) => {
          if (err != null) {
            reject(err);
          }
          resolve(res);
        });
    });
  }
}
