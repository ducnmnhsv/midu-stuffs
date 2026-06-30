function convertDataPublishV2Theme(data) {
  const result = {};
  if (data.themeName != null) result.n = data.themeName;
  if (data.time != null) result.t = data.time;
  const list = data.themeData;
  const td = [];
  for (let i in list) {
    const value = list[i];
    const stockDataList = value.stockData;
    const dt = [];
    for (let j in stockDataList) {
      const stockData = stockDataList[j];
      dt.push({
        s: stockData.stockCode,
        r: stockData.changeRate,
      });
    }
    td.push({
      p: value.period,
      c: value.themeChange,
      i: value.increases,
      d: value.decreases,
      u: value.unchanges,
      dt: dt.length == 0 ? undefined : dt,
    });
  }
  result.td = td;
  return result;
}

function convertDataPublishV2Static(data) {
  let result = {};
  if (data.code != null) result.s = data.code;
  if (data.type != null) result.t = data.type;
  if (data.time != null) result.ti = data.time;
  if (data.tradingVolume != null) result.vo = data.tradingVolume;
  if (data.totalBuyVolume != null) result.tbv = data.totalBuyVolume;
  if (data.totalBuyRaito != null) result.tbr = data.totalBuyRaito;
  if (data.totalSellVolume != null) result.tsv = data.totalSellVolume;
  if (data.totalSellRaito != null) result.tsr = data.totalSellRaito;
  if (data.totalUnkownVolume != null) result.tuv = data.totalUnkownVolume;
  if (data.totalUnkownRaito != null) result.tur = data.totalUnkownRaito;
  const prices = [];
  if (data.prices != null) {
    data.prices.forEach(it => {
      prices.push({
        p: it.price,
        av: it.matchedVolume || null,
        ar: it.matchedRaito || null,
        ab: it.matchedBuyVolume || null,
        br: it.buyRaito || null,
        as: it.matchedSellVolume || null,
        sr: it.sellRaito || null,
        au: it.matchedUnknowVolume || null,
        ur: it.unknowRaito || null
      });
    });
  }
  result.ps = prices;
  return result;
}

function convertDataPublishV2Advertise(data) {
  let result = {};
  if (data.code != null) result.s = data.code;
  if (data.time != null) result.t = data.time;
  if (data.sellBuyType != null) result.sb = data.sellBuyType;
  if (data.price != null) result.p = data.price;
  if (data.quantity != null) result.v = data.quantity;
  if (data.marketType != null) result.m = data.marketType;
  return result;
}

function convertDataPublishV2Deal(data) {
  let result = {};
  if (data.code != null) result.s = data.code;
  if (data.time != null) result.t = data.time;
  if (data.matchPrice != null) result.mp = data.matchPrice;
  if (data.matchVolume != null) result.mvo = data.matchVolume;
  if (data.matchValue != null) result.mva = data.matchValue;
  if (data.ptVolume != null) result.pvo = data.ptVolume;
  if (data.ptValue != null) result.pva = data.ptValue;
  if (data.marketType != null) result.m = data.marketType;
  return result;
}

function convertDataPublishV2BidOfferOddLot(data) {
  let result = {};
  if (data.code != null) result.s = data.code;
  if (data.type != null) result.t = data.type;
  if (data.time != null) result.ti = data.time;
  let list = data.bidOfferList;
  let bestBids = [];
  let bestOffers = [];
  for (let i in list) {
    let value = list[i];
    bestBids.push({
      p: value.bidPrice,
      v: value.bidVolume,
    });
    bestOffers.push({
      p: value.offerPrice,
      v: value.offerVolume,
    });
  }
  result.bb = bestBids;
  result.bo = bestOffers;
  return result;
}

function convertDataPublishV2BidOffer(data) {
  let result = {};
  if (data.code != null) result.s = data.code;
  if (data.type != null) result.t = data.type;
  if (data.time != null) {
    result.ti = data.time;
    result.bot = data.time; 
  }
  if (data.session != null) result.ss = data.session;
  if (data.totalBidVolume != null) result.tb = data.totalBidVolume;
  if (data.totalOfferVolume != null) result.to = data.totalOfferVolume;
  let list = data.bidOfferList;
  let bestBids = [];
  let bestOffers = [];
  for (let i in list) {
    let value = list[i];
    bestBids.push({
      p: value.bidPrice,
      v: value.bidVolume,
      c: value.bidVolumeChange
    });
    bestOffers.push({
      p: value.offerPrice,
      v: value.offerVolume,
      c: value.offerVolumeChange
    });
  }
  if (bestBids != null) result.bb = bestBids;
  if (bestOffers != null) result.bo = bestOffers;
  if (data.projectOpen != null) {
    result.ep = data.projectOpen;
  } else if (data.expectedPrice != null) {
    result.ep = data.expectedPrice;
  }
  if (data.expectedVolume != null) {
    result.exv = data.expectedVolume;
  }
  if (data.expectedChange != null) {
    result.exc = data.expectedChange;
  }
  if (data.expectedRate != null) {
    result.exr = data.expectedRate;
  }
  return result;
}

function convertDataPublishV2Extra(data) {
  let result = convertDataPublishV2Quote(data);
  if (data.projectOpen != null) {
    result.ep = data.projectOpen;
  } else if (data.expectedPrice != null) {
    result.ep = data.expectedPrice;
  }
  if (data.expectedVolume != null) {
    result.exv = data.expectedVolume;
  }
  if (data.expectedChange != null) {
    result.exc = data.expectedChange;
  }
  if (data.expectedRate != null) {
    result.exr = data.expectedRate;
  }
  if (data.ceilingPrice != null) {
    result.ce = data.ceilingPrice;
  }
  if (data.floorPrice != null) {
    result.fl = data.floorPrice;
  }
  if (data.referencePrice != null) {
    result.re = data.referencePrice;
  }
  if (data.exercisePrice != null) {
    result.exp = data.exercisePrice;
  }
  if (data.exerciseRatio != null) {
    result.er = data.exerciseRatio;
  }
  if (data.delta != null) {
    result.de = data.delta;
  }
  if (data.listedQuantity != null) {
    result.lq = data.listedQuantity;
  }
  if (data.maturityDate != null) {
    result.md = data.maturityDate;
  }
  if (data.firstTradingDate != null) {
    result.ftd = data.firstTradingDate;
  }
  if (data.lastTradingDate != null) {
    result.ltd = data.lastTradingDate;
  }
  if (data.issuerName != null) {
    result.is = data.issuerName;
  }
  if (data.openInterest != null) {
    result.oi = data.openInterest;
  }
  if (data.refCode != null) {
    result.r = data.refCode;
  }
  if (data.marketType != null) {
    result.m = data.marketType;
  }
  if (data.name != null) {
    result.n1 = data.name;
  }
  if (data.nameEn != null) {
    result.n2 = data.nameEn;
  }
  if (data.nameEn != null) {
    result.n2 = data.nameEn;
  }
  if (data.avgTradingVol10 != null) {
    result.av = data.avgTradingVol10;
  }
  if (data.indexType != null) {
    result.it = data.indexType;
  }
  if (data.baseCode != null) {
    result.b = data.baseCode;
  }
  if (data.baseCodeSecuritiesType != null) {
    result.bs = data.baseCodeSecuritiesType;
  }
  if (data.hightTime != null) {
    result.ht = data.hightTime;
  }
  if (data.lowTime != null) {
    result.lt = data.lowTime;
  }
  if (data.breakEven != null) {
    result.be = data.breakEven;
  }
  if (data.impliedVolatility != null) {
    result.iv = data.impliedVolatility;
  }
  if (data.gearingRt != null) {
    result.gr = data.gearingRt;
  }
  if (data.cwPremium != null) {
    result.pe = data.cwPremium;
  }
  if (data.basis != null) {
    result.ba = data.basis;
  }
  if (data.ptVolume != null) {
    result.pvo = data.ptVolume
  }
  if (data.ptValue != null) {
    result.pva = data.ptValue
  }

  if (data.totalBidVolume != null) result.tb = data.totalBidVolume;
  if (data.totalOfferVolume != null) result.to = data.totalOfferVolume;
  if (data.iNAV != null) {
    result.inav = data.iNAV;
  }
  if (data.iIndexValue != null) {
    result.iidx = data.iIndexValue;
  }
  return result;
}

function convertDataPublishV2Quote(data) {
  let result = {};
  if (data.code != null) result.s = data.code;
  if (data.type != null) result.t = data.type;
  if (data.time != null) result.ti = data.time;
  if (data.open != null) result.o = data.open;
  if (data.high != null) result.h = data.high;
  if (data.low != null) result.l = data.low;
  if (data.last != null) result.c = data.last;
  if (data.change != null) result.ch = data.change;
  if (data.rate != null) result.ra = data.rate;
  if (data.tradingVolume != null) result.vo = data.tradingVolume;
  if (data.tradingValue != null) result.va = data.tradingValue;
  if (data.matchingVolume != null) result.mv = data.matchingVolume;
  if (data.averagePrice != null) result.a = data.averagePrice;
  if (data.matchedBy != null) result.mb = data.matchedBy;
  if (data.totalBidVolume != null) result.tb = data.totalBidVolume;
  if (data.totalOfferVolume != null) result.to = data.totalOfferVolume;
  if (data.turnoverRate != null) result.tor = data.turnoverRate;
  const fr = {};
  if (data.foreignerBuyVolume != null) fr.bv = data.foreignerBuyVolume;
  if (data.foreignerSellVolume != null) fr.sv = data.foreignerSellVolume;
  if (data.foreignerCurrentRoom != null) fr.cr = data.foreignerCurrentRoom;
  if (data.foreignerTotalRoom != null) fr.tr = data.foreignerTotalRoom;

  if (data.activeBuyVolume != null) result.abv = data.activeBuyVolume;
  if (data.activeSellVolume != null) result.asv = data.activeSellVolume;

  if (Object.keys(fr).length > 0) {
    result.fr = fr;
  }
  if (data.type === 'INDEX') {
    const ic = {};
    if (data.ceilingCount != null) ic.ce = data.ceilingCount;
    if (data.floorCount != null) ic.fl = data.floorCount;
    if (data.upCount != null) ic.up = data.upCount;
    if (data.downCount != null) ic.dw = data.downCount;
    if (data.unchangedCount != null) ic.uc = data.unchangedCount;
    if (Object.keys(ic).length > 0) {
      result.ic = ic;
    }
  }
  if (data.type === 'FUTURES') {
    if (data.averagePrice != null) result.a = data.averagePrice;
    if (data.matchedBy != null) result.mb = data.matchedBy;
    if (data.totalBidVolume != null) result.tb = data.totalBidVolume;
    if (data.totalOfferVolume != null) result.to = data.totalOfferVolume;
    if (data.basis != null) result.ba = data.basis;
  }
  const hly = [];
  if (data.highLowYearData != null) {
    data.highLowYearData.forEach(it => {
      hly.push({
        h: it.highPrice,
        l: it.lowPrice,
        hd: it.dateOfHighPrice || undefined,
        ld: it.dateOfLowPrice || undefined,
      });
    });
  }
  return result;
}


function convertSymbolInfoV2(data) {
  let result = {};
  if (data.code != null) result.s = data.code;
  if (data.type != null) result.t = data.type;
  if (data.time != null) {
    result.ti = data.time;
    result.bot = data.time;
  }
  if (data.open != null) result.o = data.open;
  if (data.high != null) result.h = data.high;
  if (data.low != null) result.l = data.low;
  if (data.last != null) result.c = data.last;
  if (data.change != null) result.ch = data.change;
  if (data.rate != null) result.ra = data.rate;
  if (data.tradingVolume != null) result.vo = data.tradingVolume;
  if (data.tradingValue != null) result.va = data.tradingValue;
  if (data.matchingVolume != null) result.mv = data.matchingVolume;
  if (data.averagePrice != null) result.a = data.averagePrice;
  if (data.matchedBy != null) result.mb = data.matchedBy;
  if (data.totalBidVolume != null) result.tb = data.totalBidVolume;
  if (data.totalOfferVolume != null) result.to = data.totalOfferVolume;
  if (data.turnoverRate != null) result.tor = data.turnoverRate;
  const fr = {};
  if (data.foreignerBuyVolume != null) fr.bv = data.foreignerBuyVolume;
  if (data.foreignerSellVolume != null) fr.sv = data.foreignerSellVolume;
  if (data.foreignerCurrentRoom != null) fr.cr = data.foreignerCurrentRoom;
  if (data.foreignerTotalRoom != null) fr.tr = data.foreignerTotalRoom;

  if (Object.keys(fr).length > 0) {
    result.fr = fr;
  }
  if (data.type === 'INDEX') {
    const ic = {};
    if (data.ceilingCount != null) ic.ce = data.ceilingCount;
    if (data.floorCount != null) ic.fl = data.floorCount;
    if (data.upCount != null) ic.up = data.upCount;
    if (data.downCount != null) ic.dw = data.downCount;
    if (data.unchangedCount != null) ic.uc = data.unchangedCount;
    if (Object.keys(ic).length > 0) {
      result.ic = ic;
    }
  }
  if (data.type === 'FUTURES') {
    if (data.basis != null) result.ba = data.basis;
  }
  const hly = [];
  if (data.highLowYearData != null) {
    data.highLowYearData.forEach(it => {
      hly.push({
        h: it.highPrice,
        l: it.lowPrice,
        hd: it.dateOfHighPrice || undefined,
        ld: it.dateOfLowPrice || undefined,
      });
    });
  }
  result.hly = hly;
  if (data.projectOpen != null) {
    result.ep = data.projectOpen;
  } else if (data.expectedPrice != null) {
    result.ep = data.expectedPrice;
  }
  if (data.expectedVolume != null) {
    result.exv = data.expectedVolume;
  }
  if (data.expectedChange != null) {
    result.exc = data.expectedChange;
  }
  if (data.expectedRate != null) {
    result.exr = data.expectedRate;
  }
  if (data.ceilingPrice != null) {
    result.ce = data.ceilingPrice;
  }
  if (data.floorPrice != null) {
    result.fl = data.floorPrice;
  }
  if (data.referencePrice != null) {
    result.re = data.referencePrice;
  }
  if (data.exercisePrice != null) {
    result.exp = data.exercisePrice;
  }
  if (data.exerciseRatio != null) {
    result.er = data.exerciseRatio;
  }
  if (data.delta != null) {
    result.de = data.delta;
  }
  if (data.listedQuantity != null) {
    result.lq = data.listedQuantity;
  }
  if (data.maturityDate != null) {
    result.md = data.maturityDate;
  }
  if (data.firstTradingDate != null) {
    result.ftd = data.firstTradingDate;
  }
  if (data.lastTradingDate != null) {
    result.ltd = data.lastTradingDate;
  }
  if (data.issuerName != null) {
    result.is = data.issuerName;
  }
  if (data.openInterest != null) {
    result.oi = data.openInterest;
  }
  if (data.refCode != null) {
    result.r = data.refCode;
  }
  if (data.marketType != null) {
    result.m = data.marketType;
  }
  if (data.name != null) {
    result.n1 = data.name;
  }
  if (data.nameEn != null) {
    result.n2 = data.nameEn;
  }
  if (data.nameEn != null) {
    result.n2 = data.nameEn;
  }
  if (data.avgTradingVol10 != null) {
    result.av = data.avgTradingVol10;
  }
  if (data.indexType != null) {
    result.it = data.indexType;
  }
  if (data.baseCode != null) {
    result.b = data.baseCode;
  }
  if (data.baseCodeSecuritiesType != null) {
    result.bs = data.baseCodeSecuritiesType;
  }
  if (data.hightTime != null) {
    result.ht = data.hightTime;
  }
  if (data.lowTime != null) {
    result.lt = data.lowTime;
  }
  if (data.breakEven != null) {
    result.be = data.breakEven;
  }
  if (data.impliedVolatility != null) {
    result.iv = data.impliedVolatility;
  }
  if (data.gearingRt != null) {
    result.gr = data.gearingRt;
  }
  if (data.cwPremium != null) {
    result.pe = data.cwPremium;
  }
  if (data.ptVolume != null) {
    result.pvo = data.ptVolume
  }
  if (data.ptValue != null) {
    result.pva = data.ptValue
  }

  if (data.totalBidVolume != null) result.tb = data.totalBidVolume;
  if (data.totalOfferVolume != null) result.to = data.totalOfferVolume;
  if (data.iNAV != null) {
    result.inav = data.iNAV;
  }
  if (data.iIndexValue != null) {
    result.iidx = data.iIndexValue;
  }
  if (data.session != null) result.ss = data.session;
  if (data.totalBidVolume != null) result.tb = data.totalBidVolume;
  if (data.totalOfferVolume != null) result.to = data.totalOfferVolume;
  let list = data.bidOfferList;
  let bestBids = [];
  let bestOffers = [];
  for (let i in list) {
    let value = list[i];
    bestBids.push({
      p: value.bidPrice,
      v: value.bidVolume,
      c: value.bidVolumeChange
    });
    bestOffers.push({
      p: value.offerPrice,
      v: value.offerVolume,
      c: value.offerVolumeChange
    });
  }
  if (bestBids != null) result.bb = bestBids;
  if (bestOffers != null) result.bo = bestOffers;
  if (data.projectOpen != null) {
    result.ep = data.projectOpen;
  } else if (data.expectedPrice != null) {
    result.ep = data.expectedPrice;
  }
  if (data.last != null && data.listedQuantity != null) result.mc = data.listedQuantity * data.last;
  return result;
}

module.exports = {
  convertDataPublishV2Theme,
  convertDataPublishV2Static,
  convertDataPublishV2Advertise,
  convertDataPublishV2Deal,
  convertDataPublishV2BidOfferOddLot,
  convertDataPublishV2BidOffer,
  convertDataPublishV2Extra,
  convertDataPublishV2Quote,
  convertSymbolInfoV2,
};
