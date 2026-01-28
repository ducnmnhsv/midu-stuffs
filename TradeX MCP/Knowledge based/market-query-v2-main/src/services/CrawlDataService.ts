import { Service } from 'typedi';
import { ICrawlDailyDataRequest } from '../models/request/ICrawlDailyDataRequest';
import config from '../config';
import { Logger, Utils } from 'tradex-common';
import { getDb } from '../utils/dbConnection';
import { IDaily } from '../models/response/IDaily';
import { ISymbolDaily } from '../models/db/ISymbolDaily';
import fetch, { Response } from 'node-fetch';
import { COLLECTIONS_NAME } from '../constants';

const defaultCrawDailyCodeMap: { [k: string]: string } = {
  VNINDEX: 'VN',
  HNXINDEX: 'HNX',
  UPCOMINDEX: 'UPCOM',
};

@Service()
export default class CrawlDataService {
  private async fetchRetry(url: string, time: number, retry: number = 5): Promise<Response> {
    let res: Response;
    try {
      res = await fetch(url, {});
      return res;
    } catch (e) {
      if (time < retry) {
        const retryTime = time + 1;
        return this.fetchRetry(url, retryTime, retry);
      }
      throw e;
    }
  }

  public async crawlChartData(request: ICrawlDailyDataRequest) {
    const noInfoList: string[] = [];
    const resolutions = { D: 'daily' };
    let from: number = config.crawlingChart.defaultFrom;
    if (request.from != null && request.from !== '') {
      from = Math.floor(Utils.convertStringToDate(request.from, 'YYYYMMDD').getTime() / 1000);
    }
    Logger.info(`Crawl from : ${from} - ${request.from}`);
    let to: number = Math.floor(new Date().getTime() / 1000);
    if (request.to != null && request.to !== '') {
      to = Math.floor(Utils.convertStringToDate(request.to, 'YYYYMMDD').getTime() / 1000);
    }
    Logger.info(`Crawl to : ${to} - ${request.to}`);
    Logger.info(`Start get stock list in symbol info....`);
    const codeMap = request.symbolCodeMap || defaultCrawDailyCodeMap;
    let symbolList: string[] = [];
    if (request.symbols != null && request.symbols.length > 0) {
      const reverseCodeMap = {};
      Object.keys(codeMap).forEach((k: string) => {
        reverseCodeMap[codeMap[k]] = k;
      });
      request.symbols.forEach((it: string) => {
        const vietstockCode = reverseCodeMap[it];
        symbolList.push(vietstockCode || it);
      });
    } else {
      symbolList = (await getDb().collection(COLLECTIONS_NAME.SYMBOL_INFO).find({}).sort({ _id: 1 }).toArray()).map((obj: any) => obj._id);
      symbolList.push('VN30F1M');
      symbolList.push('VN30F2M');
      symbolList.push('VN30F1Q');
      symbolList.push('VN30F2Q');
      symbolList.push('VNINDEX');
      symbolList.push('HNXINDEX');
      symbolList.push('UPCOMINDEX');
    }
    Logger.info(`Finish get stock list in symbol info....`);
    Logger.info(`Start delete stock list in symbol daily....`);
    const gte = new Date(from * 1000);
    gte.setHours(12);
    const lte = new Date();
    lte.setHours(12);
    await getDb()
      .collection(COLLECTIONS_NAME.SYMBOL_DAILY)
      .deleteMany({
        code: { $in: symbolList },
        date: {
          $gte: gte,
          $lte: lte,
        },
      });
    Logger.info(`Finish delete stock list in symbol daily....`);
    Logger.info(`Start crawling....`);
    const symbolListSize = symbolList.length;
    for (const resolution of Object.keys(resolutions)) {
      for (let i = 0; i < symbolListSize; i++) {
        const code = symbolList[i];
        const correctCode = codeMap[code] || code;
        Logger.info(`crawl: ${code} - ${i}/${symbolListSize}`);
        const url = config.crawlingChart.url
          .replace(/{code}/g, code)
          .replace(/{resolution}/g, resolution)
          .replace(/{from}/g, `${from}`)
          .replace(/{to}/g, `${to}`);
        Logger.info(url);
        const res: Response = await this.fetchRetry(url, 0);
        const data = await res.json();
        const response: IDaily = JSON.parse(data);
        if (response.s === 'no_data') {
          noInfoList.push(code);
        } else {
          const insertList: ISymbolDaily[] = [];
          response.t.forEach((element: number, index: number) => {
            const date = new Date(element * 1000);
            date.setHours(12, 0, 0, 0);
            const symbolDaily: ISymbolDaily = {
              _id: `${correctCode}_${Utils.formatDateToDisplay(new Date(element * 1000))}`,
              last: response.c[index],
              high: response.h[index],
              low: response.l[index],
              open: response.o[index],
              tradingVolume: response.v[index],
              code: correctCode,
              date: date,
              rate: 0,
              tradingValue: 0,
              change: 0,
            };
            if (index > 0) {
              symbolDaily.change = response.c[index] - response.c[index - 1];
              symbolDaily.rate = symbolDaily.change / response.c[index - 1];
            }
            insertList.push(symbolDaily);
          });
          Logger.info(`number of records: ${insertList.length}`);
          await getDb().collection(COLLECTIONS_NAME.SYMBOL_DAILY).insertMany(insertList);
        }
      }
    }
    Logger.info(noInfoList);
    Logger.info(`Finish crawling....`);
  }
}
