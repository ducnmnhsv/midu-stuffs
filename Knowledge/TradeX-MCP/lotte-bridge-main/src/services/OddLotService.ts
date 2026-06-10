import { Service, Inject } from 'typedi';
// import LotteCommonDao from '../daos/LotteCommonDao';
// import Redis, { Category } from './Redis';
// import { ILotteOddLotRequest } from '../models/request/lotte/ILotteOddLotRequest';
// import { LOTTE_INDEX, LOTTE_MARKET_TYPE, MARKET_LIST } from '../constants/enum';
// import config from '../config';
// import { IParam } from '../models/request/lotte/ILotteRequest';
// import { ILotteOddLotResponse, toSymbolInfo } from '../models/response/lotte/ILotteOddLotResponse';
// import { ISymbolInfo } from '../models/ISymbolInfo';
// import { scheduleJob } from 'node-schedule';
// import { getInstance } from 'tradex-common/build/src/modules/kafka';
// import { v4 as uuid } from 'uuid';
// import { IBidOfferOddLotUpdate } from '../models/IBidOfferOddLotUpdate';
// import { getCurrentTime } from '../utils/dateTimeUtil';

import { IContext } from '../models/IContext';
import { Errors, Logger, Utils } from 'tradex-common';
import { Constants } from '../constants/Constants';
import { parseMessages } from '../utils/lotte';
import { LotteMarketDao } from '../daos/LotteMarketDao';
import { ILotteOddlotLatestRequest } from '../models/request/lotte/ILotteOddlotLatestRequest';
import { IOddlotLatestRequest } from '../models/request/IOddlotLatestRequest';
import {
  ILotteOddlotLatestResponse,
  ILotteOddlotLatestResponseDataItem,
} from '../models/response/lotte/ILotteOddlotLatestResponse';
import { IOddlotLatestResponse, IPriceVolItem, IForeign } from '../models/response/IOddlotLatestResponse';

const { GeneralError, InvalidParameterError } = Errors;
const { validate } = Utils;

@Service()
export class OddLotService {
  // @Inject()
  // private redis: Redis;
  // @Inject()
  // private commonDao: LotteCommonDao;
  @Inject()
  private lotteMarketDao: LotteMarketDao;

  // async queryOddLot() {
  //   scheduleJob(config.schedule.queryOddLot, async () => {
  //     if (!config.enableQueryOddLot) return;
  //     try {
  //       Logger.info('queryOddLot at ' + new Date().toLocaleString(), 'OddLotService');
  //       const headers: IParam = {
  //         'Content-Type': 'application/json',
  //         apiKey: config.lotte.apiKey,
  //       };
  //       const ctx: IContext = {
  //         id: 'queryOddLot',
  //         txId: 'queryOddLot',
  //         orgMsg: null,
  //       };
  //       for (const market of MARKET_LIST) {
  //         const oddLotRequest: ILotteOddLotRequest = {
  //           market_type: LOTTE_MARKET_TYPE[market],
  //           index: LOTTE_INDEX[market],
  //         };
  //         Logger.info('queryOddLot ' + oddLotRequest.market_type + ' ,' + oddLotRequest.index + ' OddLotService');
  //         let oddLotResponse: ILotteOddLotResponse = await this.commonDao.get(
  //           config.lotte.apis.getOddLot,
  //           headers,
  //           oddLotRequest,
  //           null,
  //           null,
  //           ctx
  //         );
  //         if (oddLotResponse.data_list[0].list.length > 0) {
  //           oddLotResponse.data_list[0].list.forEach(async (item) => {
  //             let symbolInfo: ISymbolInfo;
  //             try {
  //               symbolInfo = await this.redis.hget(Category.SYMBOL_INFO_ODD_LOT, item.code);
  //             } catch (err) {}
  //             if (symbolInfo != null) {
  //               const bidOfferOddLot: IBidOfferOddLotUpdate = {};
  //               toSymbolInfo(item, symbolInfo, bidOfferOddLot);
  //               if (Object.keys(bidOfferOddLot).length > 0) {
  //                 bidOfferOddLot.code = symbolInfo.code;
  //                 bidOfferOddLot.time = getCurrentTime();
  //                 getInstance().sendMessage(uuid(), config.topic.bidOfferOddLotUpdate, 'OddLotUpdate', bidOfferOddLot);
  //               }
  //             }
  //           });
  //         }
  //         while (oddLotResponse.data_list[0].hasNext === 'true') {
  //           oddLotRequest.next_key = oddLotResponse.data_list[0].nextKey;
  //           oddLotResponse = await this.commonDao.get(
  //             config.lotte.apis.getOddLot,
  //             headers,
  //             oddLotRequest,
  //             null,
  //             null,
  //             ctx
  //           );
  //           if (oddLotResponse.data_list[0].list.length > 0) {
  //             oddLotResponse.data_list[0].list.forEach(async (item) => {
  //               let symbolInfo: ISymbolInfo;
  //               try {
  //                 symbolInfo = await this.redis.hget(Category.SYMBOL_INFO_ODD_LOT, item.code);
  //               } catch (err) {}
  //               if (symbolInfo != null) {
  //                 const bidOfferOddLot: IBidOfferOddLotUpdate = {};
  //                 toSymbolInfo(item, symbolInfo, bidOfferOddLot);
  //                 if (Object.keys(bidOfferOddLot).length > 0) {
  //                   bidOfferOddLot.code = symbolInfo.code;
  //                   bidOfferOddLot.time = getCurrentTime();
  //                   getInstance().sendMessage(
  //                     uuid(),
  //                     config.topic.bidOfferOddLotUpdate,
  //                     'OddLotUpdate',
  //                     bidOfferOddLot
  //                   );
  //                 }
  //               }
  //             });
  //           }
  //         }
  //       }
  //     } catch (error) {
  //       Logger.error(error, 'OddLotService');
  //     }
  //     Logger.info('queryOddLot end at ' + new Date().toLocaleString(), 'OddLotService');
  //   });
  // }

  async getOddlotLatest(request: IOddlotLatestRequest, ctx: IContext): Promise<IOddlotLatestResponse[]> {
    const error = new InvalidParameterError();
    validate(request.symbolList, 'symbolList')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const formattedSymbols = request.symbolList.map((symbol) => {
      if (symbol.length > 12) {
        throw new GeneralError(`STOCK_${symbol}_IS_LONGER_THAN_12_CHARACTERS`);
      }
      const symbolTrim = symbol.trim();
      return symbolTrim.slice(0, 12) + ' '.repeat(12 - symbolTrim.length);
    });
    const result = formattedSymbols.join('');
    Logger.info(ctx.id, 'symbols lotte request:', result);

    const lotteResquest: ILotteOddlotLatestRequest = {
      mkt_tp: 'NA',
      market: '5',
      stk_cds: result,
      idx: 'NA',
    };
    const lotteRes: ILotteOddlotLatestResponse = await this.lotteMarketDao.getOddlotLatest(lotteResquest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (lotteRes.error_code === '0000' && lotteRes.data_list && lotteRes.data_list[0] && lotteRes.data_list[0].list) {
      const list: ILotteOddlotLatestResponseDataItem[] = lotteRes.data_list[0].list;

      if (!list) {
        return [];
      } else {
        return list.map((item) => {
          const bid1: IPriceVolItem = {
            p: item.bid1 == null ? null : Number(item.bid1),
            v: item.bid1Size == null ? null : Number(item.bid1Size),
          };
          const bid2: IPriceVolItem = {
            p: item.bid2 == null ? null : Number(item.bid2),
            v: item.bid2Size == null ? null : Number(item.bid2Size),
          };
          const bid3: IPriceVolItem = {
            p: item.bid3 == null ? null : Number(item.bid3),
            v: item.bid3Size == null ? null : Number(item.bid3Size),
          };
          const bid: IPriceVolItem[] = [bid1, bid2, bid3];

          const offer1: IPriceVolItem = {
            p: item.offer1 == null ? null : Number(item.offer1),
            v: item.offer1Size == null ? null : Number(item.offer1Size),
          };
          const offer2: IPriceVolItem = {
            p: item.offer2 == null ? null : Number(item.offer2),
            v: item.offer2Size == null ? null : Number(item.offer2Size),
          };
          const offer3: IPriceVolItem = {
            p: item.offer3 == null ? null : Number(item.offer3),
            v: item.offer3Size == null ? null : Number(item.offer3Size),
          };

          const offer: IPriceVolItem[] = [offer1, offer2, offer3];

          const foreign: IForeign = {
            bv: item.foreignBuyVol == null ? null : Number(item.foreignBuyVol),
            sv: item.foreignSellVol == null ? null : Number(item.foreignSellVol),
          };

          const response: IOddlotLatestResponse = {
            s: item.code,
            vo: item.vol == null ? null : Number(item.vol),
            va: null,
            bot: null,
            mc: null,
            bb: bid,
            bo: offer,
            fr: foreign,
            tb: null,
            to: null,
            pva: null,
            pvo: null,
          };
          return response;
        });
      }
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.MARKET_SYMBOL_ODDLOT_LATEST}${codes}`);
    }
  }
}
