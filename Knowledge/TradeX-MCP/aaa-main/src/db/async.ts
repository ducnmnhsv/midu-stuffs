import {Logger, Models} from "tradex-common";
import {getPool} from "./connection";
import {Connection as DbConnection, FieldInfo, MysqlError, PoolConnection} from "mysql";
import {BaseModel, Query} from "../models/db/BaseModel";

let totalTransaction: number = 0;

export class Connection {
  constructor(private connection: DbConnection) {
  }

  public release() {
    if ("release" in this.connection) {
      release(this.connection as PoolConnection);
    }
  }

  public beginTransaction(): Promise<any> {
    return new Promise((resolve: (data: any) => void, reject: (error: Error) => void) => {
      this.connection.beginTransaction((err: Error) => {
        if (err != null) {
          reject(err);
        } else {
          resolve(null);
        }
      });
    });
  }

  public rollback(): Promise<any> {
    return new Promise((resolve: (data: any) => void, reject: (error: Error) => void) => {
      this.connection.rollback((err: MysqlError) => {
        if (err != null) {
          reject(err);
        } else {
          resolve(null);
        }
      });
    });
  }

  public commit(): Promise<any> {
    return new Promise((resolve: (data: any) => void, reject: (error: Error) => void) => {
      this.connection.commit((err: Error) => {
        if (err != null) {
          reject(err);
        } else {
          resolve(null);
        }
      });
    });
  }

  public query(sql: string, params: any[]): Promise<Models.Pair<any, FieldInfo[]>> {
    return new Promise((resolve: (data: Models.Pair<any, FieldInfo[]>) => void, reject: (error: Error) => void) => {
      this.connection.query(sql, params, (err: MysqlError | null, results?: any, fields?: FieldInfo[]) => {
        try {
          if (err != null) {
            reject(err);
          } else {
            resolve(new Models.Pair<any, FieldInfo[]>(results, fields));
          }
        } catch (e) {
          reject(err);
        }
      });
    });
  }

  public queryResult(sql: string, params: any[]): Promise<any> {
    return new Promise((resolve: (data: any) => void, reject: (error: Error) => void) => {
      this.connection.query(sql, params, (err: MysqlError | null, results?: any, fields?: FieldInfo[]) => {
        try {
          if (err != null) {
            reject(err);
          } else {
            resolve(results);
          }
        } catch (e) {
          reject(err);
        }
      });
    });
  }

  public queryResultAndReleaseConnection(sql: string, params: any[]): Promise<any> {
    return new Promise((resolve: (data: Models.Pair<any, FieldInfo[]>) => void, reject: (error: Error) => void) => {
      this.connection.query(sql, params, (err: MysqlError | null, results?: any, fields?: FieldInfo[]) => {
        try {
          if (err != null) {
            reject(err);
            this.release()
          } else {
            resolve(results);
            this.release()
          }
        } catch (e) {
          reject(err);
          this.release()
        }
      });
    });
  }

  public async insertBulk<T extends BaseModel<T>>(insertQuery: Query<T>, records: T[], maxInsertRecords: number = 100): Promise<any> {
    let insertRecords: T[] = [];
    const doInsertScopeGroup = async () => {
      const [queryString, params] = insertQuery.insertByFields(null, insertRecords);
      await this.query(queryString, params);
      insertRecords = [];
    };

    for (let index = 0; index < records.length; index++) {
      const record: T = records[index];
      if (index > 0 && index % maxInsertRecords === 0) {
        await doInsertScopeGroup();
      }
      insertRecords.push(record);
    }
    await doInsertScopeGroup();
  }

  public async insertBulkIterator<T extends BaseModel<T>>(insertQuery: Query<T>, records: Iterator<T>, maxInsertRecords: number = 100): Promise<any> {
    let insertRecords: T[] = [];
    const doInsertScopeGroup = async () => {
      const [queryString, params] = insertQuery.insertByFields(null, insertRecords);
      await this.query(queryString, params);
      insertRecords = [];
    };
    let index = 0;
    let result: IteratorResult<T> = records.next();
    while (!result.done) {
      const record: T = result.value;
      if (index > 0 && index % maxInsertRecords === 0) {
        await doInsertScopeGroup();
      }
      insertRecords.push(record);
      index++;
      result = records.next();
    }
    await doInsertScopeGroup();
  }

  public async insertBulkTransform<T extends BaseModel<T>>(insertQuery: Query<T>, transform: (state?: any, index?: number) => [T, any], maxInsertRecords: number = 100): Promise<any> {
    let insertRecords: T[] = [];
    const doInsertScopeGroup = async () => {
      const [queryString, params] = insertQuery.insertByFields(null, insertRecords);
      await this.query(queryString, params);
      insertRecords = [];
    };
    let [record, state] = transform(null, 0);
    let index: number = 0;
    while (record != null) {
      if (index > 0 && index % maxInsertRecords === 0) {
        await doInsertScopeGroup();
      }
      insertRecords.push(record);
      index++;
      [record, state] = transform(state, index);
    }
    await doInsertScopeGroup();
  }
}

export async function connectAndDo<T>(func: (connection: Connection) => Promise<T>, connection?: Connection): Promise<T> {
  let con = connection;
  if (con == null) {
    con = await getConnection();
    try {
      const t: T = await func(con);
      con.release();
      return t;
    } catch (e) {
      con.release();
      throw e;
    }
  } else {
    return func(con);
  }
}

export function connectAndExecute<T>(func: (connection: Connection) => Promise<T>, errorhandler: (err: Error) => void, connection?: Connection): void {
  if (connection == null) {
    getConnection().then(
      (con: Connection) => func(con).then(() => con.release()).catch((e: Error) => {
        errorhandler(e);
        con.release();
      }),
    ).catch(errorhandler);
  } else {
    func(connection).then().catch(errorhandler);
  }
}

export function getConnection(): Promise<Connection> {
  return new Promise((resolve: (data: Connection) => void, reject: (error: Error) => void) => {
    getPool().getConnection((err: Error, con: PoolConnection) => {
      if (err) {
        Logger.error("error while getting db connection", err);
        release(con);
        reject(err);
      } else {
        resolve(new Connection(con));
      }
    });
  });
}

export async function query(querySql: string, params: any[], connection: Connection): Promise<Models.Pair<any, FieldInfo[]>> {
  try {
    const pair: Models.Pair<any, FieldInfo[]> = await connection.query(querySql, params);
    connection.release();
    return pair;
  } catch (e) {
    connection.release();
    throw e;
  }
}

export async function getConnectAndQuery(querySql: string, params: any[]): Promise<Models.Pair<any, FieldInfo[]>> {
  const connection: Connection = await getConnection();
  return query(querySql, params, connection);
}

export async function doJobInTransaction<T>(func: (connection: Connection) => Promise<T>, msgId?: string): Promise<T> {
  Logger.info(msgId, "doJobInTransaction", totalTransaction);
  totalTransaction++;
  const con: Connection = await getConnection();
  await con.beginTransaction();
  let result: T = null;
  try {
    result = await func(con);
  } catch (e) {
    Logger.error(msgId, "an error happen on transaction. rollback");
    await con.rollback();
    con.release();
    throw e;
  }
  try {
    await con.commit();
  } catch (e) {
    Logger.error(msgId, "fail to commit transaction. rollback");
    await con.rollback();
    con.release();
    throw e;
  }
  con.release();
  return result;
}

export function release(con: PoolConnection) {
  try {
    con && con.release();
  } catch (e) {
    if (!(e instanceof Error) || e.message !== "Connection already released") {
      throw e;
    }
  }
}
