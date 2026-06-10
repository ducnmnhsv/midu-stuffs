import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { Cursor, FilterQuery } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_PAGE_SIZE } from '../constants';
import { ISymbolQuote } from '../models/db/ISymbolQuote';
import { Logger } from 'tradex-common';
import { ISymbolQuoteMinutes } from '../models/db/ISymbolQuoteMinutes';

@Service()
export class SymbolQuoteRepository {
  public findBy(query: FilterQuery<ISymbolQuote>, limit: number = DEFAULT_PAGE_SIZE, sort: any): Cursor<ISymbolQuote> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE).find(query).sort(sort).limit(limit);
  }

  public deleteAll(): Promise<any> {
    return new Promise((resolve: Function, reject: Function) => {
      getDb()
        .collection(COLLECTIONS_NAME.SYMBOL_QUOTE)
        .deleteMany({}, (err: any, result: any) => {
          if (err) {
            reject(err);
          } else {
            resolve(result);
          }
        });
    });
  }

  public moveToHistory() {
    const cached: any[] = [];
    const maxCacheSize = 100;
    const cursor: Cursor<ISymbolQuote> = getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE).find({});

    cursor
      .forEach(async (value: any) => {
        cached.push(value);
        if (cached.length >= maxCacheSize) {
          const data = [...cached];
          cached.length = 0;
          getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE_HISTORY).insertMany(data).then().catch();
        }
      })
      .then(() => {
        if (cached.length > 0) {
          getDb().collection(COLLECTIONS_NAME.SYMBOL_QUOTE_HISTORY).insertMany(cached).then().catch();
        }
        Logger.info('Store Symbol Quote To History Done');
      })
      .catch();
  }

  public querySymbolQuoteMinutes(code: string, from: Date, to: Date): Promise<ISymbolQuoteMinutes[]> {
    const pipeline: Object[] = [
      { $match: { code: code } },
      {
        $match: {
          date: { $gte: from, $lte: to },
        },
      },
      {
        $project: {
          code: 1,
          refCode: 1,
          open: 1,
          high: 1,
          low: 1,
          last: 1,
          tradingValue: 1,
          tradingVolume: 1,
          matchingVolume: 1,
          date: 1,
          id: {
            $concat: ['$code', '_', { $dateToString: { format: '%Y%m%d%H%M', date: '$date' } }],
          },
        },
      },
      { $sort: { tradingVolume: 1 } },
      {
        $group: {
          _id: '$id',
          code: { $last: '$code' },
          refCode: { $last: '$refCode' },
          date: { $first: '$date' },
          open: { $first: '$last' },
          high: { $max: '$last' },
          low: { $min: '$last' },
          last: { $last: '$last' },
          tradingValue: { $last: '$tradingValue' },
          tradingVolume: { $last: '$tradingVolume' },
          periodTradingVolume: { $sum: '$matchingVolume' },
        },
      },
    ];

    return new Promise((resolve: Function, reject: Function) => {
      getDb()
        .collection(COLLECTIONS_NAME.SYMBOL_QUOTE)
        .aggregate(pipeline, { allowDiskUse: true })
        .toArray((err: any, result: ISymbolQuoteMinutes[]) => {
          if (err) {
            reject(err);
          } else {
            resolve(result);
          }
        });
    });
  }
}
