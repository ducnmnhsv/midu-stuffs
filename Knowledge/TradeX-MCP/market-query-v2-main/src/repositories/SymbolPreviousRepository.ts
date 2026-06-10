import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { BulkWriteResult, Cursor, FilterQuery, UpdateQuery } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_PAGE_SIZE } from '../constants';
import { ISymbolPrevious } from '../models/db/ISymbolPrevious';

@Service()
export class SymbolPreviousRepository {
  public findBy(query: FilterQuery<ISymbolPrevious>, limit: number = DEFAULT_PAGE_SIZE, skip: number = 0, sort: any = {}): Cursor<ISymbolPrevious> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_PREVIOUS).find(query).sort(sort).skip(skip).limit(limit);
  }

  public findOneBy(query: FilterQuery<ISymbolPrevious>): Promise<ISymbolPrevious> {
    return new Promise((resolve: Function, reject: Function) => {
      getDb()
        .collection(COLLECTIONS_NAME.SYMBOL_PREVIOUS)
        .findOne(query, (err: any, res: ISymbolPrevious) => {
          if (err != null) {
            reject(err);
          }
          resolve(res);
        });
    });
  }

  public async updateByBulk(listSymbolPrevious: ISymbolPrevious[]): Promise<BulkWriteResult> {
    const bulk = getDb().collection(COLLECTIONS_NAME.SYMBOL_PREVIOUS).initializeOrderedBulkOp();
    for (let i = 0; i < listSymbolPrevious.length; i++) {
      const symbolPrevious = listSymbolPrevious[i];
      bulk.find({ _id: symbolPrevious._id }).updateOne(symbolPrevious);
    }
    return bulk.execute();
  }

  public updateOne(filter: FilterQuery<ISymbolPrevious>, updateQuery: UpdateQuery<ISymbolPrevious>): Promise<any> {
    return new Promise((resolve: Function, reject: Function) => {
      getDb()
        .collection(COLLECTIONS_NAME.SYMBOL_PREVIOUS)
        .updateOne(filter, updateQuery, (err: any, res: any) => {
          if (err != null) {
            reject(err);
          }
          resolve(res);
        });
    });
  }
}
