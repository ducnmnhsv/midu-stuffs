import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { AggregationCursor, BulkWriteResult, Cursor, FilterQuery, UpdateQuery } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_PAGE_SIZE } from '../constants';
import { ISymbolDaily } from '../models/db/ISymbolDaily';
import { IGroupedSymbolDailyResponse } from '../models/response/IGroupedSymbolDailyResponse';
import { Utils } from 'tradex-common';

@Service()
export class SymbolDailyRepository {
  public findBy(query: FilterQuery<ISymbolDaily>, limit: number = DEFAULT_PAGE_SIZE, skip: number = 0, sort: any = {}): Cursor<ISymbolDaily> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_DAILY).find(query).sort(sort).skip(skip).limit(limit);
  }

  public findByNoPage(query: FilterQuery<ISymbolDaily>, sort: any = {}): Cursor<ISymbolDaily> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_DAILY).find(query).sort(sort);
  }

  public async updateByBulk(listSymbolDaily: ISymbolDaily[]): Promise<BulkWriteResult> {
    const bulk = getDb().collection(COLLECTIONS_NAME.SYMBOL_DAILY).initializeOrderedBulkOp();
    for (let i = 0; i < listSymbolDaily.length; i++) {
      const symbolDaily = listSymbolDaily[i];
      bulk.find({ _id: symbolDaily._id }).updateOne(symbolDaily);
    }
    return bulk.execute();
  }

  public async updateReturnsByBulk(listSymbolDaily: ISymbolDaily[]): Promise<BulkWriteResult> {
    const bulk = getDb().collection(COLLECTIONS_NAME.SYMBOL_DAILY).initializeOrderedBulkOp();
    for (const symbolDaily of listSymbolDaily) {
      bulk.find({ _id: symbolDaily._id }).updateOne({ $set: { returns: symbolDaily.returns } });
    }
    return bulk.execute();
  }

  public queryGroupedSymbolDailyList(symbolList: string[], limit: number, floorDate?: Date): AggregationCursor<IGroupedSymbolDailyResponse> {
    let lastDate: Date = new Date();

    if (floorDate == null) {
      lastDate.setDate(lastDate.getDate() - (limit + 7) * 2);
    } else {
      lastDate = new Date(floorDate);
    }
    const pipeline: Object[] = [
      {
        $match: {
          date: { $gte: Utils.getStartOfDate(lastDate) },
          code: { $in: symbolList },
        },
      },
      {
        $sort: { date: -1 },
      },
      {
        $group: {
          _id: '$code',
          items: { $push: '$$ROOT' },
        },
      },
      {
        $project: {
          items: { $slice: ['$items', limit] },
        },
      },
    ];
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_DAILY).aggregate(pipeline, { allowDiskUse: true });
  }

  public updateMany(filter: FilterQuery<ISymbolDaily>, updateQuery: UpdateQuery<ISymbolDaily>): Promise<any> {
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
