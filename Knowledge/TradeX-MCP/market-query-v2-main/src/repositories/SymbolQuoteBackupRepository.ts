import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { BulkWriteResult, Cursor, FilterQuery } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_PAGE_SIZE } from '../constants';
import { ISymbolQuoteBackup } from '../models/db/ISymbolQuoteBackup';

@Service()
export class SymbolQuoteBackupRepository {
  public findBy(query: FilterQuery<ISymbolQuoteBackup>, limit: number = DEFAULT_PAGE_SIZE, sort: any): Cursor<ISymbolQuoteBackup> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE_BACKUP).find(query).sort(sort).limit(limit);
  }

  public async updateByBulk(symbolQuoteList: ISymbolQuoteBackup[]): Promise<BulkWriteResult> {
    const bulk = getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE_BACKUP).initializeOrderedBulkOp();
    for (let i = 0; i < symbolQuoteList.length; i++) {
      const symbolMinute = symbolQuoteList[i];
      bulk.find({ _id: symbolMinute._id }).updateOne(symbolMinute);
    }
    return bulk.execute();
  }
}
