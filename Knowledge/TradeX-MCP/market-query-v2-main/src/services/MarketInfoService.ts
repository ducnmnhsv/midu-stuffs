import { Utils } from 'tradex-common';
import { MARKET_INFO_FIELD } from '../constants';
import { ILastTradingDateResponse } from '../models/response/ILastTradingDateResponse';
import { Inject, Service } from 'typedi';
import { MarketInfoRepository } from '../repositories/MarketInfoRepository';
import { ICurrentDividendEventResponse } from '../models/response/ICurrentDividendEventResponse';

@Service()
export default class MarketInfoService {
  @Inject()
  public marketInfoRepository: MarketInfoRepository;

  public async getLastTradingDate(): Promise<ILastTradingDateResponse> {
    const marketInfo = await this.marketInfoRepository.findOne({ _id: MARKET_INFO_FIELD.LAST_TRADING_DATE });
    return { lastTradingDate: Utils.formatDateToDisplay(marketInfo.lastTradingDate, 'YYYYMMDD') };
  }

  public async getCurrentDividendList(): Promise<ICurrentDividendEventResponse> {
    const marketInfo = await this.marketInfoRepository.findOne({ _id: MARKET_INFO_FIELD.CURRENT_DIVIDEND_EVENT });
    return {
      date: Utils.formatDateToDisplay(marketInfo.createdAt),
      eventList: marketInfo.eventList,
    };
  }
}
