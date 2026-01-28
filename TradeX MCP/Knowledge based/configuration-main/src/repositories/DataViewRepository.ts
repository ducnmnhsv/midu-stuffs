import DataView from "../models/db/DataView";
import { ViewSelectResponse } from "../models/response/admin/ViewSelectResponse";
import config from "../config";
import IKeyValue from "../models/request/IKeyValue";
import { AppDataSource } from "../AppDataSource";

export const DataViewRepository = AppDataSource.getRepository(DataView).extend({
  getSelectDataByView(
    view: string,
    fetchCount: number,
    lastSequence: number | string,
    args: IKeyValue[],
  ): Promise<ViewSelectResponse[]> {
    let whereCondition = "1 = 1";
    const parameters: any[] = [];
    if (lastSequence != null) {
      whereCondition += " AND id > ? ";
      parameters.push(lastSequence);
    }

    if (args != null && args.length > 0) {
      args.forEach((arg: IKeyValue) => {
        whereCondition += ` AND ${arg.key} = ?`;
        parameters.push(arg.value);
      });
    }

    return this.query(
      `SELECT * FROM ${view} WHERE ${whereCondition} ORDER BY id ASC LIMIT ?`,
      [
        ...parameters,
        fetchCount == null ? config.defaultFetchCount : fetchCount,
      ],
    );
  },
});
