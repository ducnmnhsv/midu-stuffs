import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { Cursor, FilterQuery, UpdateWriteOpResult } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_PAGE_SIZE } from '../constants';
import { ISymbolInfo } from '../models/db/ISymbolInfo';

@Service()
export class SymbolInfoRepository {
  public findAll(): Cursor<ISymbolInfo> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_INFO).find({}).sort({ _id: 1 });
  }

  public findBy(query: FilterQuery<ISymbolInfo>, limit: number = DEFAULT_PAGE_SIZE, sort: any = {}): Cursor<ISymbolInfo> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_INFO).find(query).limit(limit).sort(sort);
  }

  public resetBidOfferList(): Promise<UpdateWriteOpResult> {
    return getDb()
      .collection(COLLECTIONS_NAME.SYMBOL_INFO)
      .updateMany({}, { $set: { bidOfferList: [] } });
  }
}
