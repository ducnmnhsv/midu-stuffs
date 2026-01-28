import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { IChart } from '../models/db/IChart';
import { Cursor, DeleteWriteOpResultObject, ObjectID, UpdateWriteOpResult } from 'mongodb';
import { COLLECTIONS_NAME } from '../constants';

@Service()
export class ChartRepository {
  public findByUserAndClient(user: string, client: string): Cursor<IChart> {
    return getDb()
      .collection(COLLECTIONS_NAME.CHART)
      .find(
        {
          ownerSource: client,
          ownerId: user,
        },
        {
          projection: {
            _id: 1,
            name: 1,
            symbol: 1,
            resolution: 1,
            lastModified: 1,
          },
        },
      );
  }

  public findByUserAndClientAndChartId(user: string, client: string, chart: string): Promise<IChart> {
    return getDb()
      .collection(COLLECTIONS_NAME.CHART)
      .findOne({
        ownerSource: client,
        ownerId: user,
        _id: new ObjectID(chart),
      });
  }

  public save(data: any): Promise<any> {
    return getDb().collection(COLLECTIONS_NAME.CHART).insertOne(data);
  }

  public update(filter: any, dataUpdate: any): Promise<UpdateWriteOpResult> {
    return getDb().collection(COLLECTIONS_NAME.CHART).updateOne(filter, dataUpdate);
  }

  public deleteChart(user: string, client: string, chart: string): Promise<DeleteWriteOpResultObject> {
    return getDb()
      .collection(COLLECTIONS_NAME.CHART)
      .deleteOne({
        ownerSource: client,
        ownerId: user,
        _id: new ObjectID(chart),
      });
  }
}
