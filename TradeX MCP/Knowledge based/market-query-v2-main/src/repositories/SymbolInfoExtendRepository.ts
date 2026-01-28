import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { BulkWriteResult, Cursor, FilterQuery } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_PAGE_SIZE } from '../constants';
import { ISymbolPrevious } from '../models/db/ISymbolPrevious';
import { ISymbolInfoExtend } from '../models/db/ISymbolInfoExtend';

@Service()
export class SymbolInfoExtendRepository {
  public findBy(query: FilterQuery<ISymbolInfoExtend>, limit: number = DEFAULT_PAGE_SIZE, skip: number = 0, sort: any = {}): Cursor<ISymbolPrevious> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_INFO_EXTEND).find(query).sort(sort).skip(skip).limit(limit);
  }

  public async upsertByBulk(listSymbolInfoExtend: ISymbolInfoExtend[]): Promise<BulkWriteResult> {
    const bulk = getDb().collection(COLLECTIONS_NAME.SYMBOL_INFO_EXTEND).initializeOrderedBulkOp();
    for (let i = 0; i < listSymbolInfoExtend.length; i++) {
      const symbolInfoExtend = listSymbolInfoExtend[i];
      bulk.find({ _id: symbolInfoExtend._id }).upsert().update({ $set: symbolInfoExtend });
    }
    return bulk.execute();
  }
}
