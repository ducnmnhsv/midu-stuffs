import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { COLLECTIONS_NAME, DIVIDEND_INFO } from '../constants';
import { IDividend } from '../models/db/IDividend';
import { Utils } from 'tradex-common';
import { BulkWriteResult, UnorderedBulkOperation } from 'mongodb';

@Service()
export class DividendRepository {
  public findBy(query: object): Promise<IDividend[]> {
    return getDb().collection(COLLECTIONS_NAME.DIVIDEND).find(query).toArray();
  }

  public async updateByBulk(dividendList: IDividend[]) {
    const bulk: UnorderedBulkOperation = getDb().collection(COLLECTIONS_NAME.DIVIDEND).initializeUnorderedBulkOp();
    dividendList.forEach((value: IDividend) => {
      bulk.find({ _id: value._id }).upsert().update({ $set: value });
    });
    const result: BulkWriteResult = await bulk.execute();
    if (result.hasWriteErrors()) {
      throw {
        message: result.getWriteErrors(),
        getErrors: () => result.getWriteErrors(),
      };
    }
  }

  public async findLastDividendLtDate(code: string, currentDividendDate: Date): Promise<IDividend> {
    const dividend = await getDb()
      .collection(COLLECTIONS_NAME.DIVIDEND)
      .find({
        code: code,
        exDividendDate: { $lt: Utils.getStartOfDate(currentDividendDate) },
        eventType: {
          $in: [DIVIDEND_INFO.BONUS_SHARE, DIVIDEND_INFO.DIVIDEND, DIVIDEND_INFO.RIGHTS_ISSUE],
        },
      })
      .sort({ exDividendDate: -1 })
      .limit(1)
      .toArray();
    return dividend[0];
  }
}
