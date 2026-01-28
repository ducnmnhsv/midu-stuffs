import config from '../../config';
import 'reflect-metadata';
import { connectToMongo } from '../../utils/dbConnection';
import { Logger, Utils } from 'tradex-common';
import { Db } from 'mongodb';
import * as https from 'https';
import { ISymbolDaily } from '../../models/db/ISymbolDaily';
import { COLLECTIONS_NAME } from '../../constants';

Logger.create(config.logger.config, true);
Logger.info('Starting...');

connectToMongo()
  .then(async (database: Db) => {
    Logger.info('Connect to Database Success!');
    const symbolList: CodeMapping[] = [
      { code: 'VN30F1M', realCode: 'VN30F1M' },
      { code: 'VN30F2M', realCode: 'VN30F2M' },
      { code: 'VN30F1Q', realCode: 'VN30F1Q' },
      { code: 'VN30F2Q', realCode: 'VN30F2Q' },
      { code: 'GB05F1Q', realCode: 'GB05F1Q' },
      { code: 'GB05F2Q', realCode: 'GB05F2Q' },
      { code: 'GB05F3Q', realCode: 'GB05F3Q' },
    ];
    Logger.info(`total symbol: ${symbolList.length}`);

    for (let i = 0; i < symbolList.length; i++) {
      const dateFrom: Date = new Date();
      dateFrom.setFullYear(2018, 7, 10);
      dateFrom.setHours(0, 0, 0, 0);

      const dateTo: Date = new Date();
      dateTo.setFullYear(2020, 8, 19);
      dateTo.setHours(0, 0, 0, 0);

      const symbolCode: string = symbolList[i].code;
      Logger.info(`--------------------- crawl: ${symbolCode} ---------------- ${i}`);

      while (dateTo.getTime() > dateFrom.getTime()) {
        Logger.info(`dateTo: ${dateTo}`);

        const to: number = dateTo.getTime() / 1000;
        dateTo.setDate(dateTo.getDate() - 390);
        if (dateTo.getTime() < dateFrom.getTime()) {
          dateTo.setTime(dateFrom.getTime());
        }
        const from: number = dateTo.getTime() / 1000;

        // const url: string = `https://api.vietstock.vn/ta/history?symbol=${symbolCode}&resolution=1&from=${from}&to=${to}`;
        const url: string = `https://dchart-api.vndirect.com.vn/dchart/history?symbol=${symbolCode}&resolution=D&from=${from}&to=${to}`;
        let data: string = await doRequest(url);
        data = data.replace(new RegExp('}"', 'g'), '}');
        data = data.replace(new RegExp('"{', 'g'), '{');
        data = data.replace(new RegExp('\\\\', 'g'), '');
        const fromSlice: number = data.indexOf('{');
        const toSlice: number = data.indexOf('}');
        data = data.substring(fromSlice, toSlice + 1);

        if (!Utils.isEmpty(data)) {
          const response: HistoryResponse = JSON.parse(data);
          Logger.info(response.t.length);
          if (response.s === 'ok') {
            const symbolDailyList: ISymbolDaily[] = parseToSymbolDaily(response, symbolList[i].realCode);
            if (symbolDailyList.length > 0) {
              try {
                await saveToDataBase(
                  symbolDailyList,
                  // 't_futures_daily',
                  COLLECTIONS_NAME.SYMBOL_DAILY,
                  database
                );
              } catch (e) {
                Logger.error(`error save to database: ${e}`);
              }
            }
          } else {
            Logger.error(`response is not ok: ${response.s}`);
          }
        }
      }
    }
    Logger.info(`----------------------- Finish --------------`);
  })
  .catch((error: any) => Logger.error(error));

const doRequest = (url: string): Promise<string> => {
  Logger.info(url);
  return new Promise((resolve: Function) => {
    https
      .get(url, (resp: any) => {
        let data = '';

        // A chunk of data has been received.
        resp.on('data', (chunk: any) => {
          data += chunk;
        });

        // The whole response has been received.
        resp.on('end', () => {
          resolve(data);
        });
      })
      .on('error', (err: any) => {
        Logger.error(`error crawl url: ${url} - ${err}`);
        resolve('');
      });
  });
};

const parseToSymbolDaily = (response: HistoryResponse, symbolCode: string): ISymbolDaily[] => {
  const symbolDailyList: ISymbolDaily[] = [];
  if (response.s === 'ok') {
    for (let i = 0; i < response.t.length; i++) {
      const dateHistory: Date = new Date(response.t[i] * 1000);

      const symbolDaily: ISymbolDaily = {
        _id: `${symbolCode}_${Utils.formatDateToDisplay(dateHistory, 'YYYYMMDD')}`,
        code: symbolCode,
        refCode: symbolCode,
        date: dateHistory,
        last: response.c[i],
        open: response.o[i],
        high: response.h[i],
        low: response.l[i],
        tradingValue: null,
        tradingVolume: response.v[i],
      };

      symbolDailyList.push(symbolDaily);
    }
  }
  return symbolDailyList;
};

const saveToDataBase = (data: object[], collectionName: string, database: Db): Promise<any> => {
  return new Promise((resolve: Function, reject: Function) => {
    database.collection(collectionName).insertMany(data, (err: any, res: any) => {
      if (err != null) {
        reject(err);
      }
      resolve(res);
    });
  });
};

class HistoryResponse {
  public t: number[] = [];
  public o: number[] = [];
  public h: number[] = [];
  public l: number[] = [];
  public c: number[] = [];
  public v: number[] = [];
  public s: string;
}

class CodeMapping {
  public code: string;
  public realCode: string;
}
