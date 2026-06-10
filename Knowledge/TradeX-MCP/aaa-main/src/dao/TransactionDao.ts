import {connectAndDo, Connection} from "../db/async";
import Transaction, {ITransaction} from "../models/db/Transaction";
import {Query} from "../models/db/BaseModel";

export async function createTransaction(data: ITransaction, connection?: Connection): Promise<Transaction> {
  return connectAndDo(async (con: Connection) => {
    const transaction: Transaction = new Transaction(data);
    const query: Query<Transaction> = new Query<Transaction>(transaction);
    const results: any = await con.queryResult(query.insert(), [data]);
    transaction.id.set(results.insertId);
    return transaction;
  }, connection);
}
