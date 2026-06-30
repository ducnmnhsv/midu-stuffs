import {
  SymbolPeriodResponse,
  // SymbolQuoteResponse,
  MarketSessionStatusResponse,
  SymbolQuoteMinuteResponse,
  SymbolQuoteTickResponse,
  EtfNavDailyResponse,
  EtfIndexDailyResponse,
  ForeignerDailyResponse,
  StockRankingTradeResponse,
  IndexStockListResponse,
  FixSecurityListQueryResponse,
  TradingViewHistoryResponse,
  TradingViewSymbolSearchResponse,
  StockRankingTopResponse,
  ForeignerSummaryResponse,
  TopForeignerTradingResponse,
  SymbolTickSizeMatchResponse,
  TopAiRatingResponse,
} from 'tradex-models-market';
import { IBidOfferItem, IFuturesHighLowYearItem, ISymbolInfo } from '../models/db/ISymbolInfo';
import { Utils } from 'tradex-common';
import { ISymbolDaily } from '../models/db/ISymbolDaily';
import { ISymbolQuote } from '../models/db/ISymbolQuote';
import { IMarketSessionStatus } from '../models/db/IMarketSessionStatus';
import {
  MarketTypeEnum,
  MarketSessionStatusEnum,
  MarketSessionTypeEnum,
  SecuritiesTypeEnum,
  MatchByTypeEnum,
  BaseCodeSecuritiesTypeEnum,
  StatusResponseEnum,
  TopSortTypeEnum,
  UpDownTypeEnum,
  PRICE_BOARD_CATEGORY,
} from '../constants';
import { ISymbolQuoteMinutes } from '../models/db/ISymbolQuoteMinutes';
import { IForeignerDaily } from '../models/db/IForeignerDaily';
import { IEtfNavDaily } from '../models/db/IEtfNavDaily';
import { IDealNoticeData as DealNoticeData, IDealNoticeData } from '../models/db/IDealNoticeData';
import { IAdvertiseData } from '../models/db/IAdvertiseData';
import { IIndexStockList } from '../models/db/IIndexStockList';
import { IChart } from '../models/db/IChart';
import { ISymbolWeeklyOrMonthly } from '../models/db/ISymbolWeeklyOrMonthly';
import { ITopAiRating } from '../models/db/ITopAiRating';
import { ISymbolInfoExtend } from '../models/db/ISymbolInfoExtend';
import IMinuteChartResponse from '../models/response/IMinuteChartResponse';
import IPriceBoardResponse, { IAsk, IBid, IForeigner, IIndexChange } from '../models/response/IPriceBoardResponse';
import { IPutthroughDealResponse } from '../models/response/IPutthroughDealResponse';
import { IPutthroughAdvertiseResponse } from '../models/response/IPutthroughAdvertiseResponse';
import IPtDealTotalResponse from '../models/response/PtDealTotalResponse';
import IQuoteResponse from '../models/response/IQuoteResponse';
import { ISymbolLatestResponse } from '../models/response/ISymbolLatestResponse';
import { ISymbolStaticInfoResponse } from '../models/response/ISymbolStaticInfoResponse';
import { IPriceSteps, IPriceStepsResponse, ISymbolStatistics, ISymbolStatisticsResponse } from '../models/db/ISymbolStatistics';
import { IRedisStockRankingPeriodItemResponse, IStockRankingPeriodResponse } from '../models/response/IStockRankingPeriodResponse';
import config from '../config';
import { ISymbolStockRight } from '../models/db/ISymbolStockRighte';
import { IBonusResponse, IDividendResponse, IIssueResponse, ISymbolStockRightResponse } from '../models/response/ISymbolStockRightResponse';
import { checkDate } from './parse';
import { IForeignerRankingResponse } from '../models/response/IForeignerRankingResponse';
import { SymbolQuoteResponse } from '../models/response/SymbolQuoteResponse';

const toSymbolLatestResponse = (symbolInfo: ISymbolInfo, isOddLot: boolean): ISymbolLatestResponse => {
  const response: ISymbolLatestResponse = {};
  response.s = symbolInfo.code;
  response.t = SecuritiesTypeEnum[symbolInfo.type];
  response.o = Utils.round(symbolInfo.open);
  response.h = Utils.round(symbolInfo.high);
  response.l = Utils.round(symbolInfo.low);
  response.c = Utils.round(symbolInfo.last);
  response.a = Utils.round(symbolInfo.averagePrice);
  response.ch = Utils.round(symbolInfo.change);
  response.ra = Utils.round(symbolInfo.rate);
  response.vo = Utils.round(symbolInfo.tradingVolume);
  response.va = Utils.round(symbolInfo.tradingValue);
  response.tor = symbolInfo.turnoverRate;
  if (!isOddLot) {
    response.bot = symbolInfo.bidofferTime;
  } else {
    response.bot = symbolInfo.oddlotBidofferTime;
  }
  response.mc = Utils.round(symbolInfo.listedQuantity * symbolInfo.last);
  if (symbolInfo.highLowYearData != null && symbolInfo.highLowYearData.length > 0) {
    response.hly = symbolInfo.highLowYearData
      .filter((it: IFuturesHighLowYearItem) => it != null)
      .map((it: IFuturesHighLowYearItem) => ({
        h: it.highPrice || 0,
        l: it.lowPrice || 0,
        hd: it.dateOfHighPrice,
        ld: it.dateOfLowPrice,
      }));
  }
  if (symbolInfo.type !== SecuritiesTypeEnum.INDEX) {
    response.mv = Utils.round(symbolInfo.matchingVolume);
    response.mb = MatchByTypeEnum[symbolInfo.matchBy];
    if (config.domain === 'nhsv' || symbolInfo.marketType !== MarketTypeEnum.HOSE) {
      // HOSE don't provide this data but crawl from MASV has wrong data
      if (symbolInfo.totalBidVolume != null) {
        response.tb = Utils.round(symbolInfo.totalBidVolume);
      }
      if (symbolInfo.totalOfferVolume != null) {
        response.to = Utils.round(symbolInfo.totalOfferVolume);
      }
    }
    response.ss = symbolInfo.sessions;

    response.bb = [];
    response.bo = [];

    let bidOfferList = symbolInfo.bidOfferList;
    if (isOddLot) {
      bidOfferList = symbolInfo.oddlotBidOfferList;
    }

    if (bidOfferList) {
      bidOfferList.forEach((bidOfferItem: IBidOfferItem) => {
        response.bb.push({
          p: Utils.round(bidOfferItem.bidPrice),
          v: Utils.round(bidOfferItem.bidVolume),
          c: Utils.round(bidOfferItem.bidVolumeChange),
        });
        response.bo.push({
          p: Utils.round(bidOfferItem.offerPrice),
          v: Utils.round(bidOfferItem.offerVolume),
          c: Utils.round(bidOfferItem.offerVolumeChange),
        });
      });
    }
    response.fr = {
      bv: Utils.round(symbolInfo.foreignerBuyVolume),
      sv: Utils.round(symbolInfo.foreignerSellVolume),
      tr: Utils.round(symbolInfo.foreignerTotalRoom),
      cr: Utils.round(symbolInfo.foreignerCurrentRoom),
    };
  } else {
    response.ic = {
      ce: symbolInfo.ceilingCount,
      fl: symbolInfo.floorCount,
      up: symbolInfo.upCount,
      dw: symbolInfo.downCount,
      uc: symbolInfo.unchangedCount,
      tc: symbolInfo.tradeCount,
      utc: symbolInfo.unTradeCount,
    };
  }
  if (symbolInfo.type === SecuritiesTypeEnum.FUTURES) {
    response.oi = symbolInfo.openInterest;
    response.ba = Utils.round(symbolInfo.basis);
  }
  response.exp = Utils.round(symbolInfo.exercisePrice);
  response.ep = Utils.round(symbolInfo.expectedPrice);
  response.exc = Utils.round(symbolInfo.expectedChange);
  response.exr = Utils.round(symbolInfo.expectedRate);
  response.exv = Utils.round(symbolInfo.expectedVolume);
  response.be = Utils.round(symbolInfo.breakEven);
  response.pe = Utils.round(symbolInfo.cwPremium);
  response.pva = Utils.round(symbolInfo.ptTradingVolume);
  response.pvo = Utils.round(symbolInfo.ptTradingValue);
  if (config.domain === 'nhsv') {
    response.pva = Utils.round(symbolInfo.ptTradingValue);
    response.pvo = Utils.round(symbolInfo.ptTradingVolume);
  }
  response.inav = symbolInfo.iNAV;
  response.iidx = symbolInfo.iIndexValue;
  return response;
};

const toSymbolStaticInfoResponse = (symbolInfo: ISymbolInfo, symbolExtend?: ISymbolInfoExtend): ISymbolStaticInfoResponse => {
  const response: ISymbolStaticInfoResponse = {};
  response.s = symbolInfo.code;
  response.t = SecuritiesTypeEnum[symbolInfo.type];
  response.n1 = symbolInfo.name;
  response.n2 = symbolInfo.nameEn;
  response.m = MarketTypeEnum[symbolInfo.marketType];
  response.re = Utils.round(symbolInfo.referencePrice);
  response.ce = Utils.round(symbolInfo.ceilingPrice);
  response.fl = Utils.round(symbolInfo.floorPrice);
  if (symbolExtend != null) {
    response.av = symbolExtend.avgTradingVol10;
    response.ie = symbolExtend.isExDividendDate;
  }
  if (symbolInfo.firstTradingDate != null) {
    response.ftd = Utils.formatDateToDisplay(symbolInfo.firstTradingDate);
  }
  if (symbolInfo.lastTradingDate != null) {
    response.ltd = Utils.formatDateToDisplay(symbolInfo.lastTradingDate);
  }
  if (symbolInfo.maturityDate != null) {
    response.md = Utils.formatDateToDisplay(symbolInfo.maturityDate);
  }
  response.ud = symbolInfo.underlyingSymbol;
  response.b = symbolInfo.baseCode;
  response.bs = BaseCodeSecuritiesTypeEnum[symbolInfo.baseCodeSecuritiesType];
  response.i = symbolInfo.isHighlight === 1000;

  if (symbolInfo.type !== SecuritiesTypeEnum.INDEX) {
    response.mv = Utils.round(symbolInfo.matchingVolume);
    response.mb = MatchByTypeEnum[symbolInfo.matchBy];
    if (symbolInfo.totalBidVolume != null) {
      response.tb = Utils.round(symbolInfo.totalBidVolume);
    }
    if (symbolInfo.totalOfferVolume != null) {
      response.to = Utils.round(symbolInfo.totalOfferVolume);
    }
    response.ss = symbolInfo.sessions;

    response.bb = [];
    response.bo = [];

    if (symbolInfo.bidOfferList != null) {
      symbolInfo.bidOfferList.forEach((bidOfferItem: IBidOfferItem) => {
        response.bb.push({
          p: Utils.round(bidOfferItem.bidPrice),
          v: Utils.round(bidOfferItem.bidVolume),
          c: Utils.round(bidOfferItem.bidVolumeChange),
        });
        response.bo.push({
          p: Utils.round(bidOfferItem.offerPrice),
          v: Utils.round(bidOfferItem.offerVolume),
          c: Utils.round(bidOfferItem.offerVolumeChange),
        });
      });
    }
  } else {
    response.ic = {
      ce: symbolInfo.ceilingCount,
      fl: symbolInfo.floorCount,
      up: symbolInfo.upCount,
      dw: symbolInfo.downCount,
      uc: symbolInfo.unchangedCount,
    };
  }
  response.inav = symbolInfo.iNAV;
  response.iidx = symbolInfo.iIndexValue;

  return response;
};

const toSymbolDailyResponse = (symbolDaily: ISymbolDaily): SymbolPeriodResponse => {
  const response: SymbolPeriodResponse = {};
  response.o = Utils.round(symbolDaily.open === 0 ? symbolDaily.last : symbolDaily.open);
  response.h = Utils.round(symbolDaily.high === 0 ? symbolDaily.last : symbolDaily.high);
  response.l = Utils.round(symbolDaily.low === 0 ? symbolDaily.last : symbolDaily.low);
  response.c = Utils.round(symbolDaily.last);
  response.ch = Utils.round(symbolDaily.change);
  response.ra = Utils.round(symbolDaily.rate);
  response.vo = Utils.round(symbolDaily.tradingVolume);
  response.va = Utils.round(symbolDaily.tradingValue);
  response.d = Utils.formatDateToDisplay(symbolDaily.date);
  return response;
};
const toSymbolWeeklyOrMonthlyResponse = (symbolWeekly: ISymbolWeeklyOrMonthly): SymbolPeriodResponse => {
  const response: SymbolPeriodResponse = {};
  response.o = Utils.round(symbolWeekly.open === 0 ? symbolWeekly.last : symbolWeekly.open);
  response.h = Utils.round(symbolWeekly.high === 0 ? symbolWeekly.last : symbolWeekly.high);
  response.l = Utils.round(symbolWeekly.low === 0 ? symbolWeekly.last : symbolWeekly.low);
  response.c = Utils.round(symbolWeekly.last);
  response.ch = Utils.round(symbolWeekly.change);
  response.ra = Utils.round(symbolWeekly.rate);
  response.vo = Utils.round(symbolWeekly.tradingVolume);
  response.va = Utils.round(symbolWeekly.tradingValue);
  response.d = Utils.formatDateToDisplay(symbolWeekly.date);
  response.dc = symbolWeekly.dayCount;
  return response;
};

const toSymbolQuoteMinutesResponse = (symbolQuoteMinutes: ISymbolQuoteMinutes, minuteUnit: number): SymbolQuoteMinuteResponse => {
  const response: SymbolQuoteMinuteResponse = {};
  const date = symbolQuoteMinutes.date;
  date.setSeconds(0);
  date.setMinutes(Math.trunc(date.getMinutes() / minuteUnit) * minuteUnit);
  response.l = Utils.round(symbolQuoteMinutes.low);
  response.c = Utils.round(symbolQuoteMinutes.last);
  response.h = Utils.round(symbolQuoteMinutes.high);
  response.o = Utils.round(symbolQuoteMinutes.open);
  response.pv = Utils.round(symbolQuoteMinutes.periodTradingVolume);
  response.t = Utils.formatDateToDisplay(date, Utils.DATETIME_DISPLAY_FORMAT);
  return response;
};

const toMinuteChartResponse = (symbolQuoteMinutes: ISymbolQuoteMinutes[]): IMinuteChartResponse => {
  const listClose: number[] = [];
  const listPeriodVolume: number[] = [];
  const listTime: string[] = [];

  symbolQuoteMinutes.forEach((quoteMinutes: ISymbolQuoteMinutes) => {
    const date = quoteMinutes.date;
    date.setSeconds(0);
    listClose.push(Utils.round(quoteMinutes.last));
    listPeriodVolume.push(Utils.round(quoteMinutes.periodTradingVolume));
    listTime.push(Utils.formatDateToDisplay(date, Utils.DATETIME_DISPLAY_FORMAT));
  });
  return {
    l: listClose,
    pv: listPeriodVolume,
    t: listTime,
  };
};

const toPriceBoardResponse = (symbolInfoList: ISymbolInfo[], category: string): IPriceBoardResponse => {
  if (symbolInfoList == null || symbolInfoList.length <= 0) {
    return {};
  }
  const s: string[] = []; // symbol code
  const t: string[] = []; // type: "INDEX" | "STOCK" | "FUTURES" | "CW";
  const o: number[] = []; // open
  const h: number[] = []; // high
  const l: number[] = []; // low
  const c: number[] = []; // close
  const a: number[] = []; // average price
  const ep: number[] = []; // expected price
  const exc: number[] = []; // expected change
  const exr: number[] = []; // expected rate
  const exv: number[] = []; // expected volume
  const ch: number[] = []; // change
  const ra: number[] = []; // rate
  const vo: number[] = []; // trading volume
  const va: number[] = []; // trading value
  const mv: number[] = []; // match volume
  const mb: string[] = []; // match by "CEILING" | "FLOOR" | "";
  const ss: string[] = []; // session
  const tb: number[] = []; // total Bid Volume
  const to: number[] = []; // total Offer Volume
  const bb: IBid[][] = []; // best bid
  const bo: IAsk[][] = []; // best offer
  const ic: IIndexChange[] = []; // index change
  const fr: IForeigner[] = []; //foreigner
  const be: number[] = []; // break even
  const pe: number[] = []; // % premium
  const oi: number[] = []; // open interest
  const ba: number[] = []; // basis
  const exp: number[] = []; // exercise price

  symbolInfoList.forEach((symbolInfo: ISymbolInfo) => {
    s.push(symbolInfo.code);
    t.push(SecuritiesTypeEnum[symbolInfo.type]);
    o.push(Utils.round(symbolInfo.open));
    h.push(Utils.round(symbolInfo.high));
    l.push(Utils.round(symbolInfo.low));
    c.push(Utils.round(symbolInfo.last));
    a.push(Utils.round(symbolInfo.averagePrice));
    ch.push(Utils.round(symbolInfo.change));
    ra.push(Utils.round(symbolInfo.rate));
    vo.push(Utils.round(symbolInfo.tradingVolume));
    va.push(Utils.round(symbolInfo.tradingValue));
    mv.push(Utils.round(symbolInfo.matchingVolume));

    if (PRICE_BOARD_CATEGORY.INDEX === category) {
      ic.push({
        ce: symbolInfo.ceilingCount,
        fl: symbolInfo.floorCount,
        up: symbolInfo.upCount,
        dw: symbolInfo.downCount,
        uc: symbolInfo.unchangedCount,
        tc: symbolInfo.tradeCount,
        utc: symbolInfo.unTradeCount,
      });
    }

    if (PRICE_BOARD_CATEGORY.HNX_FUTURES === category) {
      oi.push(symbolInfo.openInterest);
      ba.push(Utils.round(symbolInfo.basis));
    }

    if (
      PRICE_BOARD_CATEGORY.HNX_STOCK === category ||
      PRICE_BOARD_CATEGORY.UPCOM_STOCK === category ||
      PRICE_BOARD_CATEGORY.FAVORITE_LIST === category
    ) {
      tb.push(Utils.round(symbolInfo.totalBidVolume));
      to.push(Utils.round(symbolInfo.totalOfferVolume));
    }

    if (PRICE_BOARD_CATEGORY.HOSE_CW === category) {
      be.push(Utils.round(symbolInfo.breakEven));
      pe.push(Utils.round(symbolInfo.cwPremium));
      exp.push(Utils.round(symbolInfo.exercisePrice));
    }

    if (
      PRICE_BOARD_CATEGORY.HNX_STOCK === category ||
      PRICE_BOARD_CATEGORY.HOSE_STOCK === category ||
      PRICE_BOARD_CATEGORY.HOSE_CW === category ||
      PRICE_BOARD_CATEGORY.FAVORITE_LIST === category
    ) {
      ep.push(Utils.round(symbolInfo.expectedPrice));
      exc.push(Utils.round(symbolInfo.expectedChange));
      exr.push(Utils.round(symbolInfo.expectedRate));
      exv.push(Utils.round(symbolInfo.expectedVolume));
    }

    if (symbolInfo.type !== SecuritiesTypeEnum.INDEX) {
      mb.push(MatchByTypeEnum[symbolInfo.matchBy]);
      ss.push(symbolInfo.sessions);
      const bbTemp = [];
      const boTemp = [];

      symbolInfo.bidOfferList.forEach((bidOfferItem: IBidOfferItem) => {
        bbTemp.push({
          p: Utils.round(bidOfferItem.bidPrice),
          v: Utils.round(bidOfferItem.bidVolume),
          c: Utils.round(bidOfferItem.bidVolumeChange),
        });
        boTemp.push({
          p: Utils.round(bidOfferItem.offerPrice),
          v: Utils.round(bidOfferItem.offerVolume),
          c: Utils.round(bidOfferItem.offerVolumeChange),
        });
      });
      bo.push(boTemp);
      bb.push(bbTemp);
      fr.push({
        bv: Utils.round(symbolInfo.foreignerBuyVolume),
        sv: Utils.round(symbolInfo.foreignerSellVolume),
        tr: Utils.round(symbolInfo.foreignerTotalRoom),
        cr: Utils.round(symbolInfo.foreignerCurrentRoom),
      });
    }
  });

  return {
    s: s,
    t: t,
    o: o,
    h: h,
    l: l,
    c: c,
    a: a,
    ep: ep,
    exc: exc,
    exr: exr,
    exv: exv,
    ch: ch,
    ra: ra,
    vo: vo,
    va: va,
    mv: mv,
    mb: mb,
    ss: ss,
    tb: tb,
    to: to,
    bb: bb,
    bo: bo,
    ic: ic,
    fr: fr,
    be: be,
    pe: pe,
    oi: oi,
    ba: ba,
    exp: exp,
  };
};

const toSymbolQuoteResponse = (symbolQuote: ISymbolQuote): SymbolQuoteResponse => {
  const response: SymbolQuoteResponse = {};

  response.o = Utils.round(symbolQuote.open);
  response.t = symbolQuote.time;
  response.c = Utils.round(symbolQuote.last);
  response.ch = Utils.round(symbolQuote.change);
  response.h = Utils.round(symbolQuote.high);
  response.l = Utils.round(symbolQuote.low);
  response.mb = symbolQuote.matchedBy;
  response.mv = symbolQuote.matchingVolume;
  response.ra = Utils.round(symbolQuote.rate);
  response.se = Utils.round(symbolQuote.sequence);
  response.va = Utils.round(symbolQuote.tradingValue);
  response.vo = Utils.round(symbolQuote.tradingVolume);
  response.cf = symbolQuote.ceilingFloorEqual;
  response.asv = symbolQuote.activeSellVolume;
  response.abv = symbolQuote.activeBuyVolume;
  return response;
};

export const toSymbolStatisticsResponse = (
  symbolStatistics: ISymbolStatistics,
  pageSize: number,
  pageNumber: number,
  sortBy: string,
): ISymbolStatisticsResponse => {
  const response: ISymbolStatisticsResponse = {};
  if (!symbolStatistics) {
    return response;
  }
  const ps = [];
  response.s = symbolStatistics.code;
  response.t = symbolStatistics.type;
  response.d = symbolStatistics.date;
  response.ti = symbolStatistics.time;
  response.vo = symbolStatistics.tradingVolume;
  response.tbv = symbolStatistics.totalBuyVolume;
  response.tbr = symbolStatistics.totalBuyRaito;
  response.tsv = symbolStatistics.totalSellVolume;
  response.tsr = symbolStatistics.totalSellRaito;
  response.tuv = symbolStatistics.totalUnkownVolume;
  response.tur = symbolStatistics.totalUnkownRaito;

  if (symbolStatistics.prices) {
    symbolStatistics.prices.sort((a: IPriceSteps, b: IPriceSteps) => {
      return sortBy && sortBy === 'price' ? b.price - a.price : b.matchedRaito - a.matchedRaito;
    });
    for (let i = pageNumber * pageSize; i < pageNumber * pageSize + pageSize; i++) {
      if (i >= symbolStatistics.prices.length) {
        break;
      }
      const p: IPriceStepsResponse = {
        p: symbolStatistics.prices[i].price,
        av: symbolStatistics.prices[i].matchedVolume,
        ar: symbolStatistics.prices[i].matchedRaito,
        ab: symbolStatistics.prices[i].matchedBuyVolume,
        br: symbolStatistics.prices[i].buyRaito,
        as: symbolStatistics.prices[i].matchedSellVolume,
        sr: symbolStatistics.prices[i].sellRaito,
        au: symbolStatistics.prices[i].matchedUnknowVolume,
        ur: symbolStatistics.prices[i].unknowRaito,
      };
      ps.push(p);
    }
  }
  response.ps = ps;
  return response;
};

export const toQuoteResponse = (quoteList: ISymbolQuote[], lastSize: number, lastIndex: number): IQuoteResponse => {
  if (quoteList == null) {
    return {
      data: [],
    };
  }
  const data = [];
  quoteList.forEach((symbolQuote: ISymbolQuote) => {
    data.push({
      o: Utils.round(symbolQuote.open),
      t: symbolQuote.time,
      c: Utils.round(symbolQuote.last),
      ch: Utils.round(symbolQuote.change),
      h: Utils.round(symbolQuote.high),
      l: Utils.round(symbolQuote.low),
      mb: symbolQuote.matchedBy,
      mv: symbolQuote.matchingVolume,
      ra: Utils.round(symbolQuote.rate),
      se: Utils.round(symbolQuote.sequence),
      va: Utils.round(symbolQuote.tradingValue),
      vo: Utils.round(symbolQuote.tradingVolume),
    });
  });
  return {
    lastIndex: lastIndex,
    lastSize: lastSize,
    data: data,
  };
};

const toSymbolQuoteTickResponse = (symbolQuoteTick: ISymbolQuote, periodTradingVolume: number): SymbolQuoteTickResponse => {
  const response: SymbolQuoteTickResponse = {};
  const time = symbolQuoteTick.date;
  time.setMilliseconds(0);
  response.t = time.getTime() / 1000;
  response.o = Utils.round(symbolQuoteTick.open);
  response.h = Utils.round(symbolQuoteTick.high);
  response.l = Utils.round(symbolQuoteTick.low);
  response.c = Utils.round(symbolQuoteTick.last);
  response.pv = Utils.round(periodTradingVolume);
  return response;
};
const toMarketSessionStatusResponse = (marketSessionStatus: IMarketSessionStatus): MarketSessionStatusResponse => {
  if (marketSessionStatus != null) {
    const response: MarketSessionStatusResponse = {};
    response.market = MarketTypeEnum[marketSessionStatus.market];
    response.time = Utils.formatDateToDisplay(marketSessionStatus.date, Utils.DATETIME_DISPLAY_FORMAT);
    response.status = MarketSessionStatusEnum[marketSessionStatus.status];
    response.type = MarketSessionTypeEnum[marketSessionStatus.type];
    return response;
  }
  return null;
};

const toEtfIndexDailyResponse = (etfNavDaily: IEtfNavDaily): EtfIndexDailyResponse => {
  return {
    cd: etfNavDaily.code,
    c: Utils.round(etfNavDaily.last),
    o: Utils.round(etfNavDaily.open),
    h: Utils.round(etfNavDaily.high),
    l: Utils.round(etfNavDaily.low),
    ch: Utils.round(etfNavDaily.change),
    r: Utils.round(etfNavDaily.rate),
    d: Utils.formatDateToDisplay(etfNavDaily.date),
  };
};

const toEtfNavDailyResponse = (etfNavDaily: IEtfNavDaily): EtfNavDailyResponse => {
  return {
    cd: etfNavDaily.code,
    c: Utils.round(etfNavDaily.last),
    o: Utils.round(etfNavDaily.open),
    h: Utils.round(etfNavDaily.high),
    l: Utils.round(etfNavDaily.low),
    ch: Utils.round(etfNavDaily.change),
    r: Utils.round(etfNavDaily.rate),
    d: Utils.formatDateToDisplay(etfNavDaily.date),
  };
};

const toForeignerDailyResponse = (symbolForeignerDaily: IForeignerDaily): ForeignerDailyResponse => {
  const response: ForeignerDailyResponse = {};
  response.br = Utils.round(symbolForeignerDaily.foreignerBuyAbleRatio);
  response.bvo = Utils.round(symbolForeignerDaily.foreignerBuyVolume);
  response.svo = Utils.round(symbolForeignerDaily.foreignerSellVolume);
  response.nvo = Utils.round(symbolForeignerDaily.foreignerBuyVolume - symbolForeignerDaily.foreignerSellVolume);
  response.bva = Utils.round(symbolForeignerDaily.foreignerBuyValue);
  response.bva = Utils.round(symbolForeignerDaily.foreignerSellValue);
  response.nva = Utils.round(symbolForeignerDaily.foreignerBuyValue - symbolForeignerDaily.foreignerSellValue);
  response.cr = Utils.round(symbolForeignerDaily.foreignerCurrentRoom);
  response.cv = Utils.round(symbolForeignerDaily.foreignerChangeVolume);
  response.hr = Utils.round(symbolForeignerDaily.foreignerHoldRatio);
  response.hv = Utils.round(symbolForeignerDaily.foreignerHoldVolume);
  response.tr = Utils.round(symbolForeignerDaily.foreignerTotalRoom);
  response.d = Utils.formatDateToDisplay(symbolForeignerDaily.date);
  return response;
};

const toPutthroughAdvertiseResponse = (advertiseData: IAdvertiseData): IPutthroughAdvertiseResponse => {
  return {
    s: advertiseData.code,
    t: advertiseData.time.replace(/:/g, ''),
    sb: advertiseData.sellBuyType,
    p: Utils.round(advertiseData.price),
    v: Utils.round(advertiseData.ptVolume),
  };
};

const toPutthroughDealResponse = (dealNoticeData: IDealNoticeData): IPutthroughDealResponse => {
  return {
    s: dealNoticeData.code,
    t: dealNoticeData.time.replace(/:/g, ''),
    mp: Utils.round(dealNoticeData.matchPrice),
    mvo: Utils.round(dealNoticeData.matchVolume),
    mva: Utils.round(dealNoticeData.matchValue),
    pvo: Utils.round(dealNoticeData.ptVolume),
    pva: Utils.round(dealNoticeData.ptValue),
    m: dealNoticeData.marketType,
  };
};

const toStockRankingTradeResponse = (symbolStockRankingTrade: ISymbolInfo): StockRankingTradeResponse => {
  const response: StockRankingTradeResponse = {};
  response.c = symbolStockRankingTrade.code;
  response.cn = Utils.round(symbolStockRankingTrade.change);
  response.l = Utils.round(symbolStockRankingTrade.last);
  response.r = Utils.round(symbolStockRankingTrade.rate);
  response.to = Utils.round(symbolStockRankingTrade.turnoverRate);
  response.tr = Utils.round(symbolStockRankingTrade.tradingValue);
  response.tv = Utils.round(symbolStockRankingTrade.tradingVolume);
  return response;
};

const toIndexStockListResponse = (indexStockList: IIndexStockList): IndexStockListResponse => {
  if (indexStockList == null) {
    return [];
  }
  return indexStockList.stockList.sort((a: string, b: string) => a.localeCompare(b));
};

const toSymbolStockRightResponse = (symbolStockRight: ISymbolStockRight): ISymbolStockRightResponse => {
  const div: IDividendResponse = {
    bd: checkDate(symbolStockRight.dividend.baseDate),
    br: symbolStockRight.dividend.baseRate,
    sr: symbolStockRight.dividend.stkDividRate,
    cr: symbolStockRight.dividend.cashDividRate,
    cpd: checkDate(symbolStockRight.dividend.cashPayDate),
    fpd: checkDate(symbolStockRight.dividend.frcPayDate),
    fp: symbolStockRight.dividend.frcStkPrice,
    ed: checkDate(symbolStockRight.dividend.rcpDate),
  };
  const bonus: IBonusResponse = {
    bd: checkDate(symbolStockRight.withoutcon.baseDate),
    br: symbolStockRight.withoutcon.baseRate,
    r: symbolStockRight.withoutcon.dividRate,
    p: symbolStockRight.withoutcon.frcStkPrice,
    pd: checkDate(symbolStockRight.withoutcon.frcPayDate),
    ed: checkDate(symbolStockRight.withoutcon.rcpDate),
  };
  const issue: IIssueResponse = {
    bd: checkDate(symbolStockRight.withcon.baseDate),
    br: symbolStockRight.withcon.baseRate,
    r: symbolStockRight.withcon.dividRate,
    p: symbolStockRight.withcon.issuePrice,
    ap: symbolStockRight.withcon.applyPeriod.trim().length < 23 ? null : symbolStockRight.withcon.applyPeriod,
    tp: symbolStockRight.withcon.transferPeriod.trim().length < 23 ? null : symbolStockRight.withcon.transferPeriod,
    ed: checkDate(symbolStockRight.withcon.rcpDate),
  };
  return {
    div: div,
    bonus: bonus,
    issue: issue,
    md: checkDate(symbolStockRight.metaDate),
  };
};

const parseFromSymbolInfoToFixSymbol = (symbolInfo: ISymbolInfo): FixSecurityListQueryResponse => {
  return {
    ic: symbolInfo.code,
    cc: symbolInfo.cfiCode,
    c: symbolInfo.currency,
    se: symbolInfo.securityExchange,
    sd: symbolInfo.name,
    rl: symbolInfo.roundLot,
    mtv: symbolInfo.minTradeVolume,
    cm: symbolInfo.contractMultiplier,
    mmy: symbolInfo.maturityDate != null ? Utils.rightPad(`${symbolInfo.maturityDate}`, 8, '0').substring(0, 4) : null,
    md: symbolInfo.maturityDate != null ? symbolInfo.maturityDate.toString() : null,
    st: symbolInfo.type,
    us: symbolInfo.underlyingSymbol,
    cp: symbolInfo.ceilingPrice,
    fp: symbolInfo.floorPrice,
    exp: symbolInfo.exercisePrice,
    ep: symbolInfo.expectedPrice,
    er: symbolInfo.exerciseRatio,
    bc: symbolInfo.baseCodeSecuritiesType,
  };
};

const parseSymbolQuoteMinuteList = (
  symbolQuoteMinuteList: ISymbolQuoteMinutes[],
  nextTime: number = null,
  noData: boolean = false,
): TradingViewHistoryResponse => {
  const t = [];
  const o = [];
  const h = [];
  const l = [];
  const c = [];
  const v = [];
  let s = StatusResponseEnum.NO_DATA.valueOf();
  if (symbolQuoteMinuteList.length > 0) {
    const sortedList = symbolQuoteMinuteList.sort((a: ISymbolQuoteMinutes, b: ISymbolQuoteMinutes) => {
      return +a.date - +b.date;
    });
    for (const item of sortedList) {
      item.date.setSeconds(0);
      item.date.setMilliseconds(0);
      t.push(item.date.getTime() / 1000);
      o.push(Utils.round(item.open));
      h.push(Utils.round(item.high));
      l.push(Utils.round(item.low));
      c.push(Utils.round(item.last));
      v.push(Utils.round(item.periodTradingVolume));
    }
    s = StatusResponseEnum.OK.valueOf();
  }
  return {
    t: t,
    o: o,
    h: h,
    l: l,
    c: c,
    v: v,
    s: s,
    nextTime: nextTime,
    noData: noData,
  };
};

const parseTradingviewDailyPeriodList = (symbolPeriodResponseList: SymbolPeriodResponse[], noData: boolean = false): TradingViewHistoryResponse => {
  const t = [];
  const o = [];
  const h = [];
  const l = [];
  const c = [];
  const v = [];
  let s = StatusResponseEnum.NO_DATA.valueOf();
  if (symbolPeriodResponseList.length > 0) {
    const sortedList = symbolPeriodResponseList.sort((a: SymbolPeriodResponse, b: SymbolPeriodResponse) => {
      return +a.d - +b.d;
    });
    for (const item of sortedList) {
      const date = Utils.convertStringToDate(item.d, Utils.DATE_DISPLAY_FORMAT);
      t.push(date.getTime() / 1000);
      o.push(item.o > 0 ? item.o : item.c);
      h.push(item.h > 0 ? item.h : item.c);
      l.push(item.l > 0 ? item.l : item.c);
      c.push(item.c);
      v.push(item.vo);
    }
    s = StatusResponseEnum.OK.valueOf();
  }
  return {
    t: t,
    o: o,
    h: h,
    l: l,
    c: c,
    v: v,
    s: s,
    nextTime: null,
    noData: noData,
  };
};

const parseSymbolInfo = (symbolInfo: ISymbolInfo): object => {
  if (symbolInfo == null) {
    return { s: 'error', errmsg: 'unknown_symbol' };
  }
  return {
    name: symbolInfo.code,
    'exchange-traded': symbolInfo.marketType,
    'exchange-listed': symbolInfo.marketType,
    timezone: 'Asia/Bangkok',
    minmov: 1,
    minmov2: 0,
    pointvalue: 1,
    session: '0900-1500',
    has_intraday: true,
    intraday_multipliers: ['1'],
    has_no_volume: false,
    description: symbolInfo.name != null ? symbolInfo.name : symbolInfo.code,
    type: symbolInfo.type,
    pricescale: 100,
    ticker: symbolInfo.code,
    has_empty_bars: false,
  };
};

const toQuerySymbolSearchResponse = (symbolInfo: ISymbolInfo): TradingViewSymbolSearchResponse => {
  let description = '';
  if (symbolInfo.type === SecuritiesTypeEnum.FUTURES) {
    description = symbolInfo.name;
  } else {
    description = symbolInfo.name;
  }
  if (Utils.isEmpty(description)) {
    description = symbolInfo.code;
  }

  return {
    symbol: symbolInfo.code,
    full_name: symbolInfo.code,
    description: description,
    exchange: symbolInfo.marketType,
    type: symbolInfo.type,
  };
};

const convertFromChart = (chart: IChart): object => {
  return {
    id: chart._id,
    name: chart.name,
    symbol: chart.symbol,
    resolution: chart.resolution,
    timestamp: Math.round(chart.lastModified.getTime() / 1000),
  };
};

const covertFromChartToChartLoadInfo = (chart: IChart): object => {
  return {
    id: chart._id,
    name: chart.name,
    content: chart.content,
    timestamp: Math.round(chart.lastModified.getTime() / 1000),
  };
};

const toStockRankingUpdownResponse = (symbolInfo: ISymbolInfo): object => {
  return {
    mt: symbolInfo.marketType,
    cd: symbolInfo.code,
    cl: symbolInfo.ceilingFloorEqual,
    d: Utils.formatDateToDisplay(symbolInfo.updatedAt, Utils.DATE_DISPLAY_FORMAT),
    o: symbolInfo.open,
    h: symbolInfo.high,
    l: symbolInfo.low,
    c: symbolInfo.last,
    ch: symbolInfo.change,
    r: symbolInfo.rate,
    tv: symbolInfo.tradingVolume,
    tr: symbolInfo.tradingValue,
    // "uc": symbolInfo.upDownChange,
    // "ur": symbolInfo.,
    // "sp": symbolInfo.startprice,
    // "ep": symbolInfo.
  };
};

const toStockRankingTopResponse = (symbolInfo: ISymbolInfo, sortType: string, upDownType: string): StockRankingTopResponse => {
  let powerIndicator: number = 0;
  if (sortType === TopSortTypeEnum.POWER) {
    if (upDownType === UpDownTypeEnum.DOWN) {
      //DOWN mean sort POWER stock from best to worse, UP mean WEAK stock from worst to better
      powerIndicator = symbolInfo.bidPrice * symbolInfo.bidVolume;
    } else {
      powerIndicator = symbolInfo.offerPrice * symbolInfo.offerVolume;
    }
  }
  return {
    mt: symbolInfo.marketType,
    s: symbolInfo.code,
    cl: symbolInfo.ceilingFloorEqual,
    d: Utils.formatDateToDisplay(symbolInfo.updatedAt, Utils.DATE_DISPLAY_FORMAT),
    o: symbolInfo.open,
    h: symbolInfo.high,
    l: symbolInfo.low,
    c: symbolInfo.last,
    ch: symbolInfo.change,
    ra: symbolInfo.rate,
    vo: symbolInfo.tradingVolume,
    va: symbolInfo.tradingValue,
    pi: powerIndicator,
  };
};

const toForeignerSummaryResponse = (symbolInfo: ISymbolInfo): ForeignerSummaryResponse => {
  return {
    s: symbolInfo.code,
    c: Utils.round(symbolInfo.last),
    m: symbolInfo.marketType,
    ch: Utils.round(symbolInfo.change),
    ra: Utils.round(symbolInfo.rate),
    bvo: symbolInfo.foreignerBuyVolume,
    svo: symbolInfo.foreignerSellVolume,
    nvo: symbolInfo.foreignerBuyVolume - symbolInfo.foreignerSellVolume,
    bva: symbolInfo.foreignerBuyValue,
    sva: symbolInfo.foreignerSellValue,
    nva: symbolInfo.foreignerBuyValue - symbolInfo.foreignerSellValue,
    tr: symbolInfo.foreignerTotalRoom,
    br: Utils.round((symbolInfo.foreignerCurrentRoom / symbolInfo.foreignerTotalRoom) * 100),
    cr: symbolInfo.foreignerCurrentRoom,
    cv: symbolInfo.tradingValue,
    hv: symbolInfo.foreignerTotalRoom - symbolInfo.foreignerCurrentRoom,
    hr: Utils.round(((symbolInfo.foreignerTotalRoom - symbolInfo.foreignerCurrentRoom) / symbolInfo.foreignerTotalRoom) * 100),
  };
};

const toSymbolTickSizeMatchResponse = (symbolQuoteList: ISymbolQuote[]): SymbolTickSizeMatchResponse => {
  const response: SymbolTickSizeMatchResponse = [];
  const priceList = [];
  const matchingVolumeList = [];
  if (symbolQuoteList.length > 0) {
    for (const symbolQuote of symbolQuoteList) {
      const last: number = symbolQuote.last;
      const matchingVolume: number = symbolQuote.matchingVolume;
      if (priceList.indexOf(last) === -1) {
        priceList.push(last);
        matchingVolumeList.push(matchingVolume);
      } else {
        const indexPrice = priceList.indexOf(last);
        matchingVolumeList[indexPrice] += matchingVolume;
      }
    }

    //sort priceList
    for (let i = 0; i < priceList.length - 1; i++) {
      for (let j = i + 1; j < priceList.length; j++) {
        if (priceList[i] > priceList[j]) {
          const temp_price = priceList[i];
          priceList[i] = priceList[j];
          priceList[j] = temp_price;
          const temp_matching = matchingVolumeList[i];
          matchingVolumeList[i] = matchingVolumeList[j];
          matchingVolumeList[j] = temp_matching;
        }
      }
    }

    for (let i = 0; i < priceList.length; i++) {
      response.push({
        p: priceList[i],
        mv: matchingVolumeList[i],
      });
    }
  }
  return response;
};
const toForeignerRankingResponse = (symbolInfo: ISymbolInfo[]): IForeignerRankingResponse[] => {
  return symbolInfo.map((item: ISymbolInfo) => {
    return {
      s: item.code,
      c: Utils.round(item.last),
      r: item.rate,
      ch: Utils.round(item.change),
      tvo: Utils.round(item.tradingVolume),
      tva: Utils.round(item.tradingValue),
      fb: Utils.round(item.foreignerBuyVolume),
      fs: Utils.round(item.foreignerSellVolume),
    };
  });
};

const toTopForeignerTradingResponse = (symbolInfo: ISymbolInfo): TopForeignerTradingResponse => {
  const response: TopForeignerTradingResponse = {};
  response.s = symbolInfo.code;
  response.o = Utils.round(symbolInfo.open);
  response.h = Utils.round(symbolInfo.high);
  response.l = Utils.round(symbolInfo.low);
  response.c = Utils.round(symbolInfo.last);
  response.ch = Utils.round(symbolInfo.change);
  response.ra = Utils.round(symbolInfo.rate);
  response.vo = Utils.round(symbolInfo.tradingVolume);
  response.mt = symbolInfo.marketType;
  response.fbv = Utils.round(symbolInfo.foreignerBuyVolume * symbolInfo.last);
  response.fsv = Utils.round(symbolInfo.foreignerSellVolume * symbolInfo.last);
  response.fnv = Utils.round(response.fbv - response.fsv);
  return response;
};

const toTopAiRatingResponse = (recordList: ITopAiRating[]): TopAiRatingResponse => {
  return recordList.map((item: ITopAiRating) => {
    return {
      code: item.code,
      date: Utils.formatDateToDisplay(item.date, Utils.DATE_DISPLAY_FORMAT),
      techScore: item.techScore,
      valuationScore: item.valuationScore,
      gsScore: item.gsScore,
      overall: item.overall,
      price: item.price,
      change: item.change,
    };
  });
};

const toPtDealTotalResponse = (dealList: DealNoticeData[]): IPtDealTotalResponse => {
  let tvo = 0;
  let tva = 0;
  if (dealList != null && dealList.length > 0) {
    dealList.forEach((dealNotice: DealNoticeData) => {
      tvo += dealNotice.matchVolume;
      tva += dealNotice.matchValue;
    });
  }
  return {
    tvo: tvo,
    tva: tva,
  };
};

const toStockRankingPeriodResponse = (rankingList: IRedisStockRankingPeriodItemResponse[]): IStockRankingPeriodResponse[] => {
  return rankingList.map((item: IRedisStockRankingPeriodItemResponse) => {
    return {
      sq: item.sequence,
      s: item.stockCode,
      c: item.last,
      ch: item.change,
      ra: item.rate,
      vo: item.volume,
      udra: item.upDownRate,
      udrg: item.upDownRange,
      sp: item.startPrice,
      ep: item.endPrice,
    };
  });
};

export {
  toTopForeignerTradingResponse,
  toSymbolLatestResponse,
  toSymbolDailyResponse,
  toSymbolWeeklyOrMonthlyResponse,
  toSymbolQuoteResponse,
  toMarketSessionStatusResponse,
  toSymbolQuoteMinutesResponse,
  toSymbolQuoteTickResponse,
  toEtfIndexDailyResponse,
  toEtfNavDailyResponse,
  toForeignerDailyResponse,
  toPutthroughAdvertiseResponse,
  toStockRankingTradeResponse,
  toPutthroughDealResponse,
  toSymbolStaticInfoResponse,
  toIndexStockListResponse,
  parseFromSymbolInfoToFixSymbol,
  parseSymbolQuoteMinuteList,
  parseTradingviewDailyPeriodList,
  parseSymbolInfo,
  toQuerySymbolSearchResponse,
  convertFromChart,
  covertFromChartToChartLoadInfo,
  toStockRankingUpdownResponse,
  toStockRankingTopResponse,
  toSymbolTickSizeMatchResponse,
  toTopAiRatingResponse,
  toForeignerSummaryResponse,
  toMinuteChartResponse,
  toPriceBoardResponse,
  toPtDealTotalResponse,
  toStockRankingPeriodResponse,
  toSymbolStockRightResponse,
  toForeignerRankingResponse,
};
