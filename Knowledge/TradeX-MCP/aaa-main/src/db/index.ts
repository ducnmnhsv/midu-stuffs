import {Errors, Logger, Utils} from "tradex-common";
import {Observable, Observer, Subject} from "rx";
import {getPool} from "./connection";
import {Connection as DbConnection, FieldInfo, MysqlError, PoolConnection} from "mysql";

const {SystemError} = Errors;
const logger = Logger;
const {logError} = Logger;

let totalTransaction: number = 0;

type Callback = (err: MysqlError | null, results?: any, fields?: FieldInfo[]) => void;
type Finish = () => void;

class Connection {
  constructor(private connection: DbConnection) {
  }

  public beginTransaction(callback: (err: Error) => void) {
    this.connection.beginTransaction(callback);
  }

  public rollback(callback: (err: MysqlError) => void) {
    this.connection.rollback(callback);
  }

  public commit(callback: (err: Error) => void) {
    this.connection.commit(callback);
  }

  public query(sql: string, params: any[]
    , callback: Callback
    , callbackError?: (err: Error) => void,
  ): void {
    this.connection.query(sql, params, (err: MysqlError | null, results?: any, fields?: FieldInfo[]) => {
      try {
        callback(err, results, fields);
      } catch (e) {
        if (callbackError) {
          callbackError(e);
        } else {
          callback(e, null, null);
        }
      }
    });
  }

  public queryObs<T>(sql: string, params: any[]
    , observer: Observer<T>
    , onFinish: Finish
    , callback: (results: any, fields?: FieldInfo[]) => void
    , callbackError?: (err: Error) => void,
  ): void {
    this.query(sql, params, (err: MysqlError, results: any, fields: FieldInfo[]) => {
      if (err) {
        Utils.onError(observer, err);
        onFinish && onFinish();
        return;
      }
      callback(results, fields);
    }, callbackError);
  }
}

function getConnection<T>(obs: Subject<T>
  , func: (connection: Connection, onFinish: Finish) => void)
  : Observable<T> {
  const subject: Subject<T> = obs ? obs : new Subject<T>();
  getPool().getConnection((err: Error, con: PoolConnection) => {
    if (err) {
      logError("error while getting db connection", err);
      subject.onError(new SystemError(err));
      subject.onCompleted();
      release(con);
      return;
    }
    func(new Connection(con), () => release(con));
  });
  return subject;
}

function query(querySql: string, params: any[], obs?: Subject<any>, connection?: Connection): Observable<any> {
  const subject: Subject<any> = obs ? obs : new Subject<any>();
  const doQuery = (conn: Connection, onFinish?: Finish) => {
    conn.query(querySql, params,
      (err: MysqlError, results: any, fields: FieldInfo[]) => {
        if (err) {
          subject.onError(new SystemError(err));
        } else {
          subject.onNext({
            results,
            fields,
          });
        }
        subject.onCompleted();
        onFinish && onFinish();
      },
    );
  };
  if (!connection) {
    getConnection(subject, doQuery);
  } else {
    doQuery(connection);
  }
  return subject;
}

function doJobInTransaction<T>(subj: Subject<T>, func: (connection: Connection) => Observable<T>): Observable<T> {
  logger.info("doJobInTransaction", totalTransaction);
  const subject: Subject<T> = subj ? subj : new Subject<T>();
  getConnection(subject, (connection: Connection, onFinish?: Finish) => {
    logger.info("got an connection");
    connection.beginTransaction((err: Error) => {
      if (err) {
        subject.onError(new SystemError(err));
        subject.onCompleted();
        onFinish && onFinish();
        return;
      }
      totalTransaction++;
      logger.info("total transaction", totalTransaction);
      const obs: Observable<T> = func(connection);
      obs.subscribe((data: T) => {
        totalTransaction--;
        logger.info("commit... total transaction", totalTransaction);
        connection.commit((e: Error) => {
          logger.info("committed", e);
          if (e) {
            connection.rollback(() => {
              onFinish && onFinish();
            });
            subject.onError(e);
            subject.onCompleted();
            return;
          }
          subject.onNext(data);
          subject.onCompleted();
          onFinish && onFinish();
        });
      }, (e: Error) => {
        totalTransaction--;
        logger.info("rollback... total transaction", totalTransaction);
        connection.rollback(() => {
          onFinish && onFinish();
        });
        subject.onError(e);
        subject.onCompleted();
      });
    });
  });

  return subject;
}

function release(con: PoolConnection) {
  try {
    con && con.release();
  } catch (e) {
    if (!(e instanceof Error) || e.message !== "Connection already released") {
      throw e;
    }
  }
}

export {
  getConnection,
  doJobInTransaction,
  query,
  Connection,
  Callback,
  Finish,
};
