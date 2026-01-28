import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { COLLECTIONS_NAME } from '../constants';

@Service()
export class DealNoticeDataRepository {
  public async deleteAll(): Promise<any> {
    return getDb().collection(COLLECTIONS_NAME.DEAL_NOTICE_DATA).deleteMany({});
  }
}
