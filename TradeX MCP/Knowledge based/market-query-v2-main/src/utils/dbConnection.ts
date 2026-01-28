import { Db, MongoClient } from 'mongodb';
import config from '../config';

let database: Db;

export function connectToMongo(): Promise<Db> {
  let dbOptions = {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    server: { reconnectTries: 1000, reconnectInterval: 1000 },
  };
  if (config.db.options !== null) {
    dbOptions = { ...dbOptions, ...config.db.options };
  }
  return new Promise((resolve: Function, reject: Function) => {
    MongoClient.connect(config.db.connection.url, dbOptions, (err: unknown, db: MongoClient) => {
      if (err != null) {
        reject(err);
      }
      database = db.db(config.db.connection.database);
      // Set log level
      // Logger.setLevel('info');
      resolve(database);
    });
  });
}

export function getDb(): Db {
  return database;
}
