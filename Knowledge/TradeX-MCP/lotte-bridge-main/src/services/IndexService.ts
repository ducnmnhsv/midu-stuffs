import { Service, Inject } from 'typedi';
import LotteCommonDao from '../daos/LotteCommonDao';
import { LOTTE_MARKET_TYPE_EXCHANGE_INFO } from '../constants/enum';
import config from '../config';
import { IParam } from '../models/request/lotte/ILotteRequest';
import { Logger } from 'tradex-common';
import { scheduleJob } from 'node-schedule';
import { IContext } from '../models/IContext';
import { getInstance } from 'tradex-common/build/src/modules/kafka';
import { v4 as uuid } from 'uuid';
import { ILotteIndexListRequest, ILotteIndexStockListRequest } from '../models/request/lotte/ILotteIndexListRequest';
import {
  ILotteIndexListResponse,
  ILotteIndexStockListResponse,
} from '../models/response/lotte/ILotteIndexListResponse';
import { IIndexStockList } from '../models/db/IIndexStockList';

@Service()
export class IndexService {
  @Inject()
  private commonDao: LotteCommonDao;

  async getIndexList() {
    scheduleJob(config.schedule.getIndexList, async () => {
      try {
        Logger.info('job getIndexList at ' + new Date().toLocaleString(), 'IndexService');
        const headers: IParam = {
          'Content-Type': 'application/json',
          apiKey: config.lotte.apiKey,
        };
        const ctx: IContext = {
          id: 'getIndexList',
          txId: 'getIndexList',
          orgMsg: null,
        };
        const indexListRequest: ILotteIndexListRequest = {
          mkt_tp: LOTTE_MARKET_TYPE_EXCHANGE_INFO.ALL,
        };
        const indexListResponse: ILotteIndexListResponse = await this.commonDao.get(
          config.lotte.apis.getIndexList,
          headers,
          indexListRequest,
          null,
          null,
          ctx
        );
        if (indexListResponse.data_list[0].list.length > 0) {
          const indexList = indexListResponse.data_list[0].list;
          for (const index of indexList) {
            const indexStockListRequest: ILotteIndexStockListRequest = {
              mkt_tp: index.exchange.toLowerCase(),
              idx: index.code,
            };
            const indexStockListResponse: ILotteIndexStockListResponse = await this.commonDao.get(
              config.lotte.apis.getIndexStockList,
              headers,
              indexStockListRequest,
              null,
              null,
              ctx
            );
            const indexStockList: IIndexStockList = {
              indexCode: index.symbol,
              stockList: [],
            };
            if (indexStockListResponse.data_list[0].list.length > 0) {
              for (const indexStock of indexStockListResponse.data_list[0].list) {
                indexStockList.stockList.push(indexStock.code);
              }
            }
            getInstance().sendMessage(uuid(), config.topic.indexStockListUpdate, 'IndexListUpdate', indexStockList);
          }
        }
        // while (indexListResponse.data_list[0].hasNext === 'true') {
        //   indexListRequest.next_data = indexListResponse.data_list[0].nextKey;
        //   indexListResponse = await this.commonDao.get(
        //     config.lotte.apis.getOddLot,
        //     headers,
        //     indexListRequest,
        //     null,
        //     null,
        //     ctx
        //   );
        //   if (indexListResponse.data_list[0].list.length > 0) {
        //     const indexList = indexListResponse.data_list[0].list;
        //     for (const index of indexList) {
        //       const indexStockListRequest: ILotteIndexStockListRequest = {
        //         mkt_tp: index.exchange.toLowerCase(),
        //         idx: index.code,
        //       };
        //       const indexStockListResponse: ILotteIndexStockListResponse = await this.commonDao.get(
        //         config.lotte.apis.getIndexStockList,
        //         headers,
        //         indexStockListRequest,
        //         null,
        //         null,
        //         ctx
        //       );
        //       const indexStockList: IIndexStockList = {
        //         indexCode: index.symbol,
        //         stockList: [],
        //       };
        //       if (indexStockListResponse.data_list[0].list.length > 0) {
        //         for (const indexStock of indexStockListResponse.data_list[0].list) {
        //           indexStockList.stockList.push(indexStock.code);
        //         }
        //       }
        //       getInstance().sendMessage(uuid(), config.topic.indexStockListUpdate, 'IndexListUpdate', indexStockList);
        //     }
        //   }
        // }
      } catch (error) {
        Logger.error(error, 'IndexService');
      }
      Logger.info('job getIndexList end at ' + new Date().toLocaleString(), 'IndexService');
    });
  }
}
