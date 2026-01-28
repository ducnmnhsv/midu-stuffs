import {Connection} from "../db/async";
import {Query} from "../models/db/BaseModel";
import {Logger} from "tradex-common";
import OrderPass from "../models/db/OrderPass";

export const TYPE = {
  LOTTLE: "LOTTLE",
};

export async function storeLotteOrderPass(accountNo: string, subNo: string, secCode: string, orderPass: string, connection: Connection): Promise<any> {
  const orderPassEntity: OrderPass = new OrderPass();
  orderPassEntity.accountNo.set(accountNo);
  orderPassEntity.subNo.set(subNo);
  orderPassEntity.passType.set(TYPE.LOTTLE);
  orderPassEntity.password.set(orderPass);
  orderPassEntity.secCode.set(secCode);
  try {
    await connection.queryResult(new Query(orderPassEntity).insert(), orderPassEntity.getRow());
  } catch (e) {
    Logger.error("Fail to insert order pass to db", e);
  }
}
