import { Service } from 'typedi';
import { FilterQuery, Cursor, BulkWriteResult } from 'mongodb';
import { DEFAULT_PAGE_SIZE, COLLECTIONS_NAME } from '../constants';
import { getDb } from '../utils/dbConnection';
import { ISymbolQuoteMinutes } from '../models/db/ISymbolQuoteMinutes';
import { ISymbolQuoteMinutesBackup } from '../models/db/ISymbolQuoteMinutesBackup';

@Service()
export class SymbolQuoteMinutesBackupRepository {
  public findBy(filter: FilterQuery<ISymbolQuoteMinutesBackup>, limit: number = DEFAULT_PAGE_SIZE, sort: any = {}): Cursor<ISymbolQuoteMinutes> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE_MINUTE_BACKUP).find(filter).sort(sort).limit(limit);
  }

  public async updateByBulk(listSymbolMinute: ISymbolQuoteMinutesBackup[]): Promise<BulkWriteResult> {
    const bulk = getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE_MINUTE_BACKUP).initializeOrderedBulkOp();
    for (let i = 0; i < listSymbolMinute.length; i++) {
      const symbolMinute = listSymbolMinute[i];
      bulk.find({ _id: symbolMinute._id }).updateOne(symbolMinute);
    }
    return bulk.execute();
  }
}
