import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { COLLECTIONS_NAME } from '../constants';

@Service()
export class BidOfferRepository {
  public async deleteAll(): Promise<any> {
    return getDb().collection(COLLECTIONS_NAME.BID_OFFER).deleteMany({});
  }
}
