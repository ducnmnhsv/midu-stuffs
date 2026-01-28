import { Inject, Service } from 'typedi';
import { IContext } from '../models/IContext';
import { Errors, Logger, Utils } from 'tradex-common';
import { getElementAtIndex, parseMessages, setDefault } from '../utils/lotte';
import { GeneralError } from 'tradex-common/build/src/modules/errors';
import { IQueryStockRankingPeriodRequest } from '../models/request/IQueryStockRankingPeriodRequest';
import { MARKET_TYPE_PERIOD, RANKING, TradingSession } from '../constants/enum';
import { ILotteStockRankingPeriodRequest } from '../models/request/lotte/ILotteStockRankingPeriodRequest';
import { LotteMarketDao } from '../daos/LotteMarketDao';
import {
  ILotteStockRankingPeriodResponse,
  ILotteStockRankingPeriodResponseDataListItem,
} from '../models/response/lotte/ILotteStockRankingPeriodResponse';
import { IStockRankingPeriodResponse } from '../models/response/IStockRankingPeriodResponse';
import { Constants } from '../constants/Constants';
import { IMarketRightInfoRequest } from '../models/request/IMarketRightInfoRequest';
import { ILotteMarketRightInfoRequest } from '../models/request/lotte/ILotteMarketRightInfoRequest';
import {
  ILotteMarketRightInfoData,
  ILotteMarketRightInfoResponse,
} from '../models/response/lotte/ILotteMarketRightInfoResponse';
import { IBonus, IDividend, IIssue, IMarketRightInfoResponse } from '../models/response/IMarketRightInfoResponse';
import { checkDate } from '../utils/dateTimeUtil';
import { checkStringTrim } from '../utils/defaultUtils';
import { IMarketStockLatestRequest } from '../models/request/IMarketStockLatestRequest';
import { IMarketStockLatestResponse } from '../models/response/IMarketStockLatestResponse';
import { ILotteMarketStockLatestRequest } from '../models/request/lotte/ILotteMarketStockLatestRequest';
import { ILotteMarketStockLatestResponse } from '../models/response/lotte/ILotteMarketStockLatestResponse';
import { IMarketStockBidOfferRequest } from '../models/request/IMarketStockBidOfferRequest';
import { ILotteMarketStockBidOfferRequest } from '../models/request/lotte/ILotteMarketStockBidOfferRequest';
import { ILotteMarketStockBidOfferResponse } from '../models/response/lotte/ILotteMarketStockBidOfferResponse';
import { IMarketStockBidOfferResponse } from '../models/response/IMarketStockBidOfferResponse';
import { IMarketCwDetailRequest } from '../models/request/IMarketCwDetailRequest';
import { IMarketCwDetailResponse } from '../models/response/IMarketCwDetailResponse';
import {
  ILotteMarketCwDetailData,
  ILotteMarketCwDetailResponse,
} from '../models/response/lotte/ILotteMarketCwDetailResponse';
import { ILotteMarketCwDetailRequest } from '../models/request/lotte/ILotteMarketCwDetailRequest';

const { InvalidParameterError } = Errors;
const { validate, formatDateToDisplay, convertStringToDate, DATE_DISPLAY_FORMAT } = Utils;
const DATE_DISPLAY_FORMAT_LT = 'DD/MM/YYYY';

@Service()
export class MarketService {
  @Inject()
  private lotteMarketDao: LotteMarketDao;

  async getStockRankingPeriod(
    request: IQueryStockRankingPeriodRequest,
    ctx: IContext
  ): Promise<IStockRankingPeriodResponse[]> {
    const error = new InvalidParameterError();
    validate(request.ranking, 'ranking')
      .setRequire()
      .throwValid(error);
    validate(request.period, 'period')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const marketType =
      request.marketType == null || MARKET_TYPE_PERIOD[request.marketType] == null
        ? MARKET_TYPE_PERIOD.ALL
        : MARKET_TYPE_PERIOD[request.marketType];
    const ranking = request.ranking == null || RANKING[request.ranking] == null ? RANKING.UP : RANKING[request.ranking];
    const pageNumber: number = request.pageNumber != null ? +request.pageNumber : +0;
    let pageSize: number = request.pageSize != null ? +request.pageSize : +20;
    pageSize = pageSize < 100 ? pageSize : 100;
    let nextKey: string = '';
    if (pageNumber > 0) {
      nextKey = (pageSize * pageNumber).toString();
    }
    const startDate: Date = new Date();
    const period: number = request.period;
    for (let i: number = 0; i < period; i++) {
      startDate.setDate(startDate.getDate() - 1);
      const dayOfWeek: number = startDate.getDay();
      if (dayOfWeek === 0 || dayOfWeek === 6) {
        i--;
      }
    }
    Logger.info(
      ctx.id,
      'marketType:',
      marketType,
      ' ranking:',
      ranking,
      ' startDate:',
      formatDateToDisplay(startDate, DATE_DISPLAY_FORMAT),
      ' nextKey:',
      nextKey
    );
    const lotteRequest: ILotteStockRankingPeriodRequest = {
      mkt_tp: marketType,
      srt_tp: ranking,
      st_dt: formatDateToDisplay(startDate, DATE_DISPLAY_FORMAT),
      end_dt: '',
      next_key: nextKey,
      row_count: pageSize.toString(),
    };
    const lotteRes: ILotteStockRankingPeriodResponse = await this.lotteMarketDao.getStockRankingPeriod(
      lotteRequest,
      ctx
    );
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);

    if (lotteRes.error_code === '0000') {
      if (lotteRes.data_list && lotteRes.data_list[0] && lotteRes.data_list[0].list) {
        const itemResponses: IStockRankingPeriodResponse[] = [];
        lotteRes.data_list[0].list.forEach((listItem: ILotteStockRankingPeriodResponseDataListItem) => {
          const itemResponse: IStockRankingPeriodResponse = {
            sq: Number(listItem.seq),
            s: listItem.code,
            c: Number(listItem.last),
            ch: Number(listItem.change),
            ra: Utils.round(Number(listItem.changeRate)),
            vo: Number(listItem.volume),
            udra: Utils.round(Number(listItem.upDownRate)),
            udrg: Number(listItem.upDownRange),
            sp: Number(listItem.startPrice),
            ep: Number(listItem.endPrice),
          };
          itemResponses.push(itemResponse);
        });
        return itemResponses;
      } else {
        return [];
      }
    } else {
      throw new GeneralError(`${Constants.STOCK_RANKING_PERIOD}${codes}`);
    }
  }

  async getMarketRightInfo(request: IMarketRightInfoRequest, ctx: IContext): Promise<IMarketRightInfoResponse> {
    const error = new InvalidParameterError();
    validate(request.symbol, 'symbol')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const lotteRequest: ILotteMarketRightInfoRequest = {
      stock_code: request.symbol,
    };
    const lotteRes: ILotteMarketRightInfoResponse = await this.lotteMarketDao.getMarketRightInfo(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);

    if (lotteRes.error_code === '0000' && lotteRes.data_list && lotteRes.data_list[0]) {
      const data: ILotteMarketRightInfoData = lotteRes.data_list[0];
      const cr: string = checkStringTrim(data.dividend_cashrate);
      const crNumber: number = cr == null ? null : Number(cr);
      const div: IDividend = {
        bd: checkDate(data.dividend_basedate),
        br: checkStringTrim(data.dividend_baserate),
        sr: checkStringTrim(data.dividend_stockrate),
        cr,
        cpd: checkDate(data.dividend_cashpaydate),
        fpd: checkDate(data.dividend_oddlotpaydate),
        dp: crNumber == null ? null : crNumber * 10000,
        fp: checkStringTrim(data.dividend_oddlotprice) == null ? null : Number(data.dividend_oddlotprice),
        ed: checkDate(data.dividend_effectdate),
      };
      const bonus: IBonus = {
        bd: checkDate(data.withoutcon_basedate),
        br: checkStringTrim(data.withoutcon_baserate),
        r: checkStringTrim(data.withoutcon_allocrate),
        p: checkStringTrim(data.withoutcon_oddlotprice) == null ? null : Number(data.withoutcon_oddlotprice),
        pd: checkDate(data.withoutcon_oddlotpaydate),
        ed: checkDate(data.withoutcon_effectdate),
      };
      let ap: string = null;
      let tp: string = null;
      const subscDateBegin: string =
        checkDate(data.withcon_subscdatebegin) == null
          ? ''
          : formatDateToDisplay(
              convertStringToDate(data.withcon_subscdatebegin, DATE_DISPLAY_FORMAT),
              DATE_DISPLAY_FORMAT_LT
            );
      const subscDateEnd: string =
        checkDate(data.withcon_subscdateend) == null
          ? ''
          : formatDateToDisplay(
              convertStringToDate(data.withcon_subscdateend, DATE_DISPLAY_FORMAT),
              DATE_DISPLAY_FORMAT_LT
            );
      const transferDateBegin: string =
        checkDate(data.withcon_transferdatebegin) == null
          ? ''
          : formatDateToDisplay(
              convertStringToDate(data.withcon_transferdatebegin, DATE_DISPLAY_FORMAT),
              DATE_DISPLAY_FORMAT_LT
            );
      const transferDateEnd: string =
        checkDate(data.withcon_transferdateend) == null
          ? ''
          : formatDateToDisplay(
              convertStringToDate(data.withcon_transferdateend, DATE_DISPLAY_FORMAT),
              DATE_DISPLAY_FORMAT_LT
            );
      if (subscDateBegin !== '' || subscDateEnd !== '') {
        ap = subscDateBegin + ' - ' + subscDateEnd;
      }
      if (transferDateBegin !== '' || transferDateEnd !== '') {
        tp = transferDateBegin + ' - ' + transferDateEnd;
      }
      const issue: IIssue = {
        bd: checkDate(data.withcon_basedate),
        br: checkStringTrim(data.withcon_baserate),
        r: checkStringTrim(data.withcon_allocrate),
        p: checkStringTrim(data.withcon_issueprice) == null ? null : Number(data.withcon_issueprice),
        ap,
        tp,
        ed: checkDate(data.withcon_rcpdate),
      };
      return {
        div,
        bonus,
        issue,
      };
    } else if (codes !== null && codes === '2016') {
      return {};
    } else {
      throw new GeneralError(`${Constants.MARKET_RIGHT_INFO}${codes}`);
    }
  }

  async getMarketCwDetail(request: IMarketCwDetailRequest, ctx: IContext): Promise<IMarketCwDetailResponse> {
    const error = new InvalidParameterError();
    validate(request.symbol, 'symbol')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const lotteRequest: ILotteMarketCwDetailRequest = {
      stock_code: request.symbol.toUpperCase(),
    };

    const lotteResponse: ILotteMarketCwDetailResponse = await this.lotteMarketDao.getMarketCwDetail(lotteRequest, ctx);

    const { codes } = parseMessages(lotteResponse.error_desc, lotteResponse.error_code);

    if (lotteResponse.error_code === '0000' && lotteResponse.data_list && lotteResponse.data_list[0]) {
      const data: ILotteMarketCwDetailData = getElementAtIndex<ILotteMarketCwDetailData>(lotteResponse.data_list);

      return {
        cwType: data.cwType,
        cwStyle: data.cwExecuteType,
        issuer: data.issuerNm,
        underlyingStock: data.cwUnderSymbol,
        maturityDate: data.cwMaturityDate,
        lastTradingDate: data.cwLastTradeDate,
        conversionRatio: Number(data.cwExpiredRate),
        exercisePrice: Number(data.cwExpiredPrice),
        settlementMethod: data.cwSettlementMethod,
        symbol: data.cwCode,
        underlyingPrice: Number(data.underClassicPrice),
        status: data.profitLossStatus,
        priceDiff: Number(data.priceDiff),
        breakEvenPoint: Number(data.breanEvenPoint),
        expiredDay: data.expiredDay,
        issuedQuantity: Number(data.cwListStockQuantity),
      };
    } else if (codes !== null && codes === '2016') {
      return {};
    } else {
      throw new GeneralError(lotteResponse.error_desc);
    }
  }

  async getMarketStockLatest(request: IMarketStockLatestRequest, ctx: IContext): Promise<IMarketStockLatestResponse[]> {
    const symbolList: string[] | string = setDefault<string[] | string>(request.symbolList, []);
    const lotteRequest: ILotteMarketStockLatestRequest = {
      stk_cds: Array.isArray(symbolList) ? symbolList : [symbolList],
    };
    const lotteRes: ILotteMarketStockLatestResponse = await this.lotteMarketDao.getMarketStockLatest(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (lotteRes.error_code === '0000' && lotteRes.data_list?.[0]) {
      const data = lotteRes.data_list[0];

      return data.list.map(
        (item): IMarketStockLatestResponse => ({
          s: item.code,
          n: item.name,
          o: Number(item.open),
          h: Number(item.high),
          l: Number(item.low),
          c: Number(item.last),
          a: Number(item.avgPrice),
          ch: Number(item.change),
          ra: Number(item.changeRate),
          vo: Number(item.volume),
          va: Number(item.amount),
          tor: Number(item.turnoverRatio),
          bot: item.time,
          hly: [
            {
              h: Number(item.high52),
              l: Number(item.low52),
            },
          ],
          mv: Number(item.totalAmt),
          ss: TradingSession[item.controlCode as keyof typeof TradingSession],
          fr: [
            {
              bv: Number(item.foreignBuyVol),
              sv: Number(item.foreignSellVol),
              tr: Number(item.foreignTotalRoom),
              cr: Number(item.foreignCurrRoom),
            },
          ],
          ep: Number(item.projectOpen),
          pva: Number(item.ptAmt),
          pvo: Number(item.ptVol),
          mc: Number(item.listedStockQty) * Number(item.last),
        })
      );
    }

    if (codes === '2016') {
      return [];
    }

    throw new GeneralError(messages);
  }

  async getMarketStockBidOffer(
    request: IMarketStockBidOfferRequest,
    ctx: IContext
  ): Promise<IMarketStockBidOfferResponse[]> {
    const error = new InvalidParameterError();
    validate(request.symbol, 'symbol')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteMarketStockBidOfferRequest = {
      stk_cd: request.symbol,
      bo_cnt: `${setDefault<number>(request.numberOfBidOffer, 3)}`,
    };
    const lotteRes: ILotteMarketStockBidOfferResponse = await this.lotteMarketDao.getMarketStockBidOffer(
      lotteRequest,
      ctx
    );
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (lotteRes.error_code === '0000' && lotteRes.data_list) {
      return lotteRes.data_list.map(
        (item): IMarketStockBidOfferResponse => {
          const bidOffers = item.bidOfferList.map((bo) => ({
            bid: {
              p: Number(bo.bid),
              v: Number(bo.bidSize),
            },
            offer: {
              p: Number(bo.offer),
              v: Number(bo.offerSize),
            },
          }));

          return {
            s: item.code,
            ce: Number(item.ceiling),
            fl: Number(item.floor),
            re: Number(item.refPrice),
            a: Number(item.avgPrice),
            o: Number(item.open),
            h: Number(item.high),
            l: Number(item.low),
            c: Number(item.last),
            ch: Number(item.change),
            ra: Number(item.changeRate),
            ss: TradingSession[item.controlCode as keyof typeof TradingSession],
            bot: item.time,
            tb: Number(item.totalBidSize),
            to: Number(item.totalOfferSize),
            m: item.marketName,
            mv: Number(item.matchedVol),
            bb: bidOffers.map((bo) => bo.bid),
            bo: bidOffers.map((bo) => bo.offer),
          };
        }
      );
    }

    if (codes === '2016') {
      return [];
    }

    throw new GeneralError(messages);
  }
}
