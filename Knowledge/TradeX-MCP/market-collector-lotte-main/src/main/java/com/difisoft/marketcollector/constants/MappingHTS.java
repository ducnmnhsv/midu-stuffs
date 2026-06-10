package com.difisoft.marketcollector.constants;

import com.difisoft.htsconnection.socket.message.receive.MarketCWCurrentPriceRcv;
import com.difisoft.htsconnection.socket.message.receive.MarketFuturesCurrentPriceRcv;
import com.difisoft.htsconnection.socket.message.receive.MarketIndustryCurrentIndexRcv;
import com.difisoft.htsconnection.socket.message.receive.MarketStockCurrentPriceRcv;
import com.difisoft.market.model.common.BidOfferItem;
import com.difisoft.market.model.common.HighLowYearItem;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.marketcollector.utils.NumberUtil;
import com.difisoft.model.utils.DefaultUtils;

import java.text.ParseException;
import java.util.function.Function;

public interface MappingHTS {
    Function<MarketCWCurrentPriceRcv, SymbolInfo> QUERY_CW_INFO = res -> {
        SymbolInfo cwInfo = new SymbolInfo();
        cwInfo.setType(SymbolTypeEnum.CW);
        cwInfo.setCeilingPrice(NumberUtil.round2Decimal(res.getCeilingPrice().getValue()));
        cwInfo.setFloorPrice(NumberUtil.round2Decimal(res.getFloorPrice().getValue()));
        cwInfo.setReferencePrice(NumberUtil.round2Decimal(res.getReferencePrice().getValue()));
        cwInfo.setAveragePrice(NumberUtil.round2Decimal(res.getAveragePrice().getValue()));
        cwInfo.setOpen(NumberUtil.round2Decimal(res.getOpen().getValue()));
        cwInfo.setHigh(NumberUtil.round2Decimal(res.getHigh().getValue()));
        cwInfo.setHighTime(res.getHighTime().getValue());
        cwInfo.setLow(NumberUtil.round2Decimal(res.getLow().getValue()));
        cwInfo.setLowTime(res.getLowTime().getValue());
        cwInfo.setLast(NumberUtil.round2Decimal(res.getLast().getValue()));
        cwInfo.setChange(NumberUtil.round2Decimal(res.getChange().getValue()));
        cwInfo.setRate(NumberUtil.round2Decimal(res.getRate().getValue()));
        cwInfo.setTradingVolume(res.getTradingVolume().getValue());
        cwInfo.setPtTradingVolume(res.getPtVolume().getValue());
        cwInfo.setPriorTradingVolume(res.getPriorVolume().getValue());
        cwInfo.setTurnoverRate(NumberUtil.round2Decimal(res.getTurnoverRate().getValue()));
        cwInfo.setTradingValue(NumberUtil.round2Decimal(res.getTradingValue().getValue() * 1000000));
        cwInfo.setListedQuantity(Double.valueOf(res.getListedQuantity().getValue()).longValue());
        cwInfo.setExpectedPrice(NumberUtil.round2Decimal(res.getProjectOpen().getValue()));
        cwInfo.setControlCode(res.getControlCode().getValue());
        cwInfo.setBidPrice(NumberUtil.round2Decimal(res.getBidPrice().getValue()));
        cwInfo.setOfferPrice(NumberUtil.round2Decimal(res.getOfferPrice().getValue()));
        cwInfo.setTotalBidVolume((long) res.getTotalBidVolume().getValue());
        cwInfo.setTotalOfferVolume((long) res.getTotalOfferVolume().getValue());
        cwInfo.setChangeOfTotalBidVolume(res.getChangeOfTotalBidVolume().getValue());
        cwInfo.setChangeOfTotalOfferVolume(res.getChangeOfTotalOfferVolume().getValue());
        cwInfo.setDiffBidOffer(res.getDiffBidOffer().getValue());
        cwInfo.setTotalBidCount(res.getAccumulateBidVolume().getValue());
        cwInfo.setTotalOfferCount(res.getAccumulateOfferCount().getValue());
        cwInfo.setIndustry(res.getIndustry().getValue());
        cwInfo.setIssuerName(res.getIssuerName().getValue());
        cwInfo.setExercisePrice(NumberUtil.round2Decimal(res.getExercisePrice().getValue() / 10));
        cwInfo.setExerciseRatio(res.getExerciseRatio().getValue());
        cwInfo.setBreakEven(NumberUtil.round4Decimal(res.getBreakEven().getValue()));
        cwInfo.setImpliedVolatility(res.getImpliedVolatility().getValue());
        cwInfo.setParity(res.getParity().getValue());
        cwInfo.setCode(res.getStockCode().getValue());
        try {
            cwInfo.setLastTradingDate(DefaultUtils.DATE_FORMAT().parse("" + res.getLastTradingDate().getValue()));
        } catch (ParseException ignore) {
        }
        try {
            cwInfo.setMaturityDate(DefaultUtils.DATE_FORMAT().parse("" + res.getMaturityDate().getValue()));
        } catch (ParseException ignore) {
        }
        cwInfo.setTPrice(NumberUtil.round2Decimal(res.getTPrice().getValue()));
        cwInfo.setDelta(NumberUtil.round2Decimal(res.getDelta().getValue()));
        cwInfo.setGearingRt(NumberUtil.round2Decimal(res.getGearingRt().getValue()));
        cwInfo.setCapitalFulcrumPoint(NumberUtil.round2Decimal(res.getCapitalFulcrumPoint().getValue()));
        cwInfo.setUnderlyingSymbol(res.getUnderlyingSymbol().getValue());
        cwInfo.setUnderlyingPrice(res.getUnderlyingPrice().getValue());
        cwInfo.setUnderlyingChange(res.getUnderlyingChange().getValue());
        cwInfo.setUnderlyingRate(NumberUtil.round2Decimal(res.getUnderlyingRate().getValue()));
        if (res.getMarketName().getValue().equals("HSX")) {
            cwInfo.setMarketType("HOSE");
        } else {
            cwInfo.setMarketType(res.getMarketName().getValue());
        }
        if (res.getChange().getIndex() == 1) {
            cwInfo.setCeilingFloorEqual("CEILING");
        } else if (res.getChange().getIndex() == 4) {
            cwInfo.setCeilingFloorEqual("FLOOR");
        }
        if (res.getHighLowYearData() != null) {
            res.getHighLowYearData().forEach((item) -> {
                HighLowYearItem highLowYearItem = new HighLowYearItem();
                highLowYearItem.setHighPrice(NumberUtil.round2Decimal(item.getHighPrice().getValue()));
                highLowYearItem.setDateOfHighPrice(item.getDateOfHighPrice().getValue());
                highLowYearItem.setLowPrice(NumberUtil.round2Decimal(item.getLowPrice().getValue()));
                highLowYearItem.setDateOfLowPrice(item.getDateOfLowPrice().getValue());
                DefaultUtils.getOrSetArray(cwInfo::getHighLowYearData, cwInfo::setHighLowYearData).add(0, highLowYearItem);
            });
        } else {
            HighLowYearItem highLowYearItem = new HighLowYearItem();
            highLowYearItem.setHighPrice(NumberUtil.round2Decimal(res.getHighPrice52Week().getValue()));
            highLowYearItem.setLowPrice(NumberUtil.round2Decimal(res.getLowPrice52Week().getValue()));
            DefaultUtils.getOrSetArray(cwInfo::getHighLowYearData, cwInfo::setHighLowYearData).add(highLowYearItem);
        }
        if (res.getBidOfferData() != null) {
            res.getBidOfferData().forEach(item -> {
                double bidPrice = item.getBidPrice().getValue();
                int bidVolume = item.getBidVolume().getValue();
                int bidVolumeChange = item.getBidVolumeChange().getValue();
                double offerPrice = item.getOfferPrice().getValue();
                int offerVolume = item.getOfferVolume().getValue();
                int offerVolumeChange = item.getOfferVolumeChange().getValue();
                if (bidVolume > 0 || offerVolume > 0) {
                    BidOfferItem bidOfferItem = new BidOfferItem();
                    bidOfferItem.setBidPrice(bidPrice);
                    bidOfferItem.setBidVolume((long) bidVolume);
                    bidOfferItem.setBidVolumeChange((long) bidVolumeChange);
                    bidOfferItem.setOfferPrice(offerPrice);
                    bidOfferItem.setOfferVolume((long) offerVolume);
                    bidOfferItem.setOfferVolumeChange((long) offerVolumeChange);
                    DefaultUtils.getOrSetArray(cwInfo::getBidOfferList, cwInfo::setBidOfferList).add(bidOfferItem);
                }
            });
        }

        double a = Double.parseDouble(cwInfo.getExerciseRatio().split(":")[0]);
        double b = Double.parseDouble(cwInfo.getExerciseRatio().split(":")[1]);
        cwInfo.setExerciseRatioValue(a / b);
        //break even = giá hiện tại * conversion ration + exercise price
        cwInfo.setBreakEven(cwInfo.getLast() * cwInfo.getExerciseRatioValue() + cwInfo.getExercisePrice());
        return cwInfo;
    };

    Function<MarketStockCurrentPriceRcv, SymbolInfo> QUERY_STOCK_INFO = res -> {
        SymbolInfo stockInfo = new SymbolInfo();
        stockInfo.setType(SymbolTypeEnum.STOCK);
        stockInfo.setOpen(NumberUtil.round2Decimal(res.getOpen().getValue()));
        stockInfo.setLast(NumberUtil.round2Decimal(res.getLast().getValue()));
        if (res.getLast().getValue() == 0) {
            stockInfo.setLast(NumberUtil.round2Decimal(res.getReferencePrice().getValue()));
        }
        stockInfo.setHigh(NumberUtil.round2Decimal(res.getHigh().getValue()));
        stockInfo.setLow(NumberUtil.round2Decimal(res.getLow().getValue()));
        stockInfo.setChange(NumberUtil.round2Decimal(res.getChange().getValue()));
        stockInfo.setRate(NumberUtil.round2Decimal(res.getRate().getValue()));
        stockInfo.setTradingVolume(res.getTradingVolume().getValue());
        stockInfo.setTradingValue(NumberUtil.round2Decimal(res.getTradingValue().getValue() * 1000000));
        stockInfo.setCode(res.getStockCode().getValue());
        stockInfo.setExpectedPrice(NumberUtil.round2Decimal(res.getProjectOpen().getValue()));
        if (res.getMarketName().getValue().equals("HSX")) {
            stockInfo.setMarketType("HOSE");
        } else {
            stockInfo.setMarketType(res.getMarketName().getValue());
        }
        stockInfo.setTotalOfferVolume((long) res.getTotalOfferVolume().getValue());
        stockInfo.setTotalBidVolume((long) res.getTotalBidVolume().getValue());
        stockInfo.setIndustry(trim(res.getIndustry().getValue()));
        stockInfo.setCeilingPrice(NumberUtil.round2Decimal(res.getCeilingPrice().getValue()));
        stockInfo.setFloorPrice(NumberUtil.round2Decimal(res.getFloorPrice().getValue()));
        stockInfo.setReferencePrice(NumberUtil.round2Decimal(res.getReferencePrice().getValue()));
        stockInfo.setAveragePrice(NumberUtil.round2Decimal(res.getAveragePrice().getValue()));
        stockInfo.setPtTradingVolume(res.getPtVolume().getValue());
        stockInfo.setPtTradingValue(NumberUtil.round2Decimal(res.getPtTradingValue().getValue()));
        stockInfo.setPriorTradingVolume(res.getPriorVolume().getValue());
        stockInfo.setTurnoverRate(NumberUtil.round2Decimal(res.getTurnoverRate().getValue()));
        stockInfo.setParValue((int) res.getParValue().getValue());
        stockInfo.setListedQuantity(Double.valueOf(res.getListedQuantity().getValue()).longValue());
        stockInfo.setForeignerBuyVolume(res.getForeignerBuyVolume().getValue());
        stockInfo.setForeignerSellVolume(res.getForeignerSellVolume().getValue());
        stockInfo.setForeignerTotalRoom(res.getForeignerTotalRoom().getValue());
        stockInfo.setForeignerCurrentRoom(res.getForeignerCurrentRoom().getValue());
        stockInfo.setTotalOfferCount(res.getAccumulateOfferCount().getValue());
        stockInfo.setTotalBidCount(res.getAccumulateBidCount().getValue());
        stockInfo.setRights(res.getRights().getValue());
        if (res.getHighLowYearData() != null) {
            res.getHighLowYearData().forEach((item) -> {
                HighLowYearItem highLowYearItem = new HighLowYearItem();
                highLowYearItem.setHighPrice(NumberUtil.round2Decimal(item.getHighPrice().getValue()));
                highLowYearItem.setDateOfHighPrice(item.getDateOfHighPrice().getValue());
                highLowYearItem.setLowPrice(NumberUtil.round2Decimal(item.getLowPrice().getValue()));
                highLowYearItem.setDateOfLowPrice(item.getDateOfLowPrice().getValue());
                DefaultUtils.getOrSetArray(stockInfo::getHighLowYearData, stockInfo::setHighLowYearData).add(0, highLowYearItem);
            });
        } else {
            HighLowYearItem highLowYearItem = new HighLowYearItem();
            highLowYearItem.setHighPrice(NumberUtil.round2Decimal(res.getHighPrice52Week().getValue()));
            highLowYearItem.setLowPrice(NumberUtil.round2Decimal(res.getLowPrice52Week().getValue()));
            DefaultUtils.getOrSetArray(stockInfo::getHighLowYearData, stockInfo::setHighLowYearData).add(highLowYearItem);
        }
        if (res.getBidOfferData() != null) {
            res.getBidOfferData().forEach(item -> {
                double bidPrice = item.getBidPrice().getValue();
                int bidVolume = item.getBidVolume().getValue();
                int bidVolumeChange = item.getBidVolumeChange().getValue();
                double offerPrice = item.getOfferPrice().getValue();
                int offerVolume = item.getOfferVolume().getValue();
                int offerVolumeChange = item.getOfferVolumeChange().getValue();
                if (bidVolume > 0 || offerVolume > 0) {
                    BidOfferItem bidOfferItem = new BidOfferItem();
                    bidOfferItem.setBidPrice(bidPrice);
                    bidOfferItem.setBidVolume((long) bidVolume);
                    bidOfferItem.setBidVolumeChange((long) bidVolumeChange);
                    bidOfferItem.setOfferPrice(offerPrice);
                    bidOfferItem.setOfferVolume((long) offerVolume);
                    bidOfferItem.setOfferVolumeChange((long) offerVolumeChange);
                    DefaultUtils.getOrSetArray(stockInfo::getBidOfferList, stockInfo::setBidOfferList).add(bidOfferItem);
                }
            });
        }
        return stockInfo;
    };

    Function<MarketIndustryCurrentIndexRcv, SymbolInfo> QUERY_INDEX_INFO = res -> {
        SymbolInfo indexInfo = new SymbolInfo();
        indexInfo.setType(SymbolTypeEnum.INDEX);
        indexInfo.setOpen(NumberUtil.round2Decimal(res.getOpenIndex().getValue()));
        indexInfo.setLast(NumberUtil.round2Decimal(res.getLast().getValue()));
        indexInfo.setHigh(NumberUtil.round2Decimal(res.getHighIndex().getValue()));
        indexInfo.setLow(NumberUtil.round2Decimal(res.getLowIndex().getValue()));
        indexInfo.setChange(NumberUtil.round2Decimal(res.getChange().getValue()));
        indexInfo.setRate(NumberUtil.round2Decimal(res.getRate().getValue()));
        indexInfo.setTradingVolume(res.getTradingVolume().getValue());
        indexInfo.setTradingValue(NumberUtil.round2Decimal(res.getTradingValue().getValue() * 1000000));
        indexInfo.setPriorTradingVolume(res.getPriorVolume().getValue());
        indexInfo.setUpCount(res.getUpCount().getValue());
        indexInfo.setCeilingCount(res.getCeilingCount().getValue());
        indexInfo.setDownCount(res.getDownCount().getValue());
        indexInfo.setFloorCount(res.getFloorCount().getValue());
        indexInfo.setUnchangedCount(res.getUnchangedCount().getValue());
        indexInfo.setReferencePrice(indexInfo.getLast() - indexInfo.getChange());
        return indexInfo;
    };

    Function<MarketFuturesCurrentPriceRcv, SymbolInfo> QUERY_FUTURES_INFO = res -> {
        SymbolInfo futuresInfo = new SymbolInfo();
        futuresInfo.setType(SymbolTypeEnum.FUTURES);
        futuresInfo.setCode(res.getStockCode().getValue());
        futuresInfo.setName(res.getStockName().getValue());
        futuresInfo.setLast(NumberUtil.round2Decimal(res.getLast().getValue()));
        futuresInfo.setOpen(NumberUtil.round2Decimal(res.getOpen().getValue()));
        futuresInfo.setHigh(NumberUtil.round2Decimal(res.getHigh().getValue()));
        futuresInfo.setLow(NumberUtil.round2Decimal(res.getLow().getValue()));
        futuresInfo.setChange(NumberUtil.round2Decimal(res.getChange().getValue()));
        futuresInfo.setRate(NumberUtil.round2Decimal(res.getRate().getValue()));
        futuresInfo.setTradingValue(NumberUtil.round2Decimal(res.getTradingValue().getValue() * 1000000));
        futuresInfo.setTradingVolume(res.getTradingVolume().getValue());
        if (res.getChange().getIndex() == 1) {
            futuresInfo.setCeilingFloorEqual("CEILING");
        } else if (res.getChange().getIndex() == 4) {
            futuresInfo.setCeilingFloorEqual("FLOOR");
        }
        futuresInfo.setTime(res.getTime().getValue());
        futuresInfo.setCeilingPrice(NumberUtil.round2Decimal(res.getCeilingPrice().getValue()));
        futuresInfo.setFloorPrice(NumberUtil.round2Decimal(res.getFloorPrice().getValue()));
        futuresInfo.setAveragePrice(NumberUtil.round2Decimal(res.getAveragePrice().getValue()));
        futuresInfo.setReferencePrice(NumberUtil.round2Decimal(res.getReferencePrice().getValue()));
        futuresInfo.setHighTime(res.getHighTime().getValue());
        futuresInfo.setLowTime(res.getLowTime().getValue());
        futuresInfo.setExpectedPrice(NumberUtil.round2Decimal(res.getProjectOpen().getValue()));

        futuresInfo.setPtTradingVolume(res.getPtVolume().getValue());
        futuresInfo.setPriorVolume(res.getPriorVolume().getValue());
        futuresInfo.setTurnoverRate(NumberUtil.round2Decimal(res.getTurnoverRate().getValue()));
        futuresInfo.setPtTradingValue(NumberUtil.round2Decimal(res.getPtTradingValue().getValue()));
        futuresInfo.setParValue((int) res.getParValue().getValue());
        futuresInfo.setListedQuantity(Double.valueOf(res.getListedQuantity().getValue()).longValue());
        futuresInfo.setForeignerBuyVolume(res.getForeignerBuyVolume().getValue());
        futuresInfo.setForeignerSellVolume(res.getForeignerSellVolume().getValue());
        futuresInfo.setForeignerCurrentRoom(res.getForeignerCurrentRoom().getValue());
        futuresInfo.setForeignerTotalRoom(res.getForeignerTotalRoom().getValue());
        futuresInfo.setControlCode(res.getControlCode().getValue());
        if (res.getHighLowYearData() != null) {
            res.getHighLowYearData().forEach((item) -> {
                HighLowYearItem highLowYearItem = new HighLowYearItem();
                highLowYearItem.setHighPrice(NumberUtil.round2Decimal(item.getHighPrice().getValue()));
                highLowYearItem.setDateOfHighPrice(item.getDateOfHighPrice().getValue());
                highLowYearItem.setLowPrice(NumberUtil.round2Decimal(item.getLowPrice().getValue()));
                highLowYearItem.setDateOfLowPrice(item.getDateOfLowPrice().getValue());
                DefaultUtils.getOrSetArray(futuresInfo::getHighLowYearData, futuresInfo::setHighLowYearData).add(0, highLowYearItem);
            });
        } else {
            HighLowYearItem highLowYearItem = new HighLowYearItem();
            highLowYearItem.setHighPrice(NumberUtil.round2Decimal(res.getHighPrice52Week().getValue()));
            highLowYearItem.setLowPrice(NumberUtil.round2Decimal(res.getLowPrice52Week().getValue()));
            DefaultUtils.getOrSetArray(futuresInfo::getHighLowYearData, futuresInfo::setHighLowYearData).add(highLowYearItem);
        }

        futuresInfo.setBidPrice(NumberUtil.round2Decimal(res.getBidPrice().getValue()));
        futuresInfo.setOfferPrice(NumberUtil.round2Decimal(res.getOfferPrice().getValue()));

        if (res.getBidOfferData() != null) {
            res.getBidOfferData().forEach(item -> {
                double bidPrice = item.getBidPrice().getValue();
                int bidVolume = item.getBidVolume().getValue();
                int bidVolumeChange = item.getBidVolumeChange().getValue();
                double offerPrice = item.getOfferPrice().getValue();
                int offerVolume = item.getOfferVolume().getValue();
                int offerVolumeChange = item.getOfferVolumeChange().getValue();
                if (bidVolume > 0 || offerVolume > 0) {
                    BidOfferItem bidOfferItem = new BidOfferItem();
                    bidOfferItem.setBidPrice(bidPrice);
                    bidOfferItem.setBidVolume((long) bidVolume);
                    bidOfferItem.setBidVolumeChange((long) bidVolumeChange);
                    bidOfferItem.setOfferPrice(offerPrice);
                    bidOfferItem.setOfferVolume((long) offerVolume);
                    bidOfferItem.setOfferVolumeChange((long) offerVolumeChange);
                    DefaultUtils.getOrSetArray(futuresInfo::getBidOfferList, futuresInfo::setBidOfferList).add(bidOfferItem);
                }
            });
        }

        futuresInfo.setTotalBidVolume((long) res.getTotalBidVolume().getValue());
        futuresInfo.setTotalOfferVolume((long) res.getTotalOfferVolume().getValue());
        futuresInfo.setChangeOfTotalBidVolume(res.getChangeOfTotalBidVolume().getValue());
        futuresInfo.setChangeOfTotalOfferVolume(res.getChangeOfTotalOfferVolume().getValue());
        futuresInfo.setDiffBidOffer(res.getDiffBidOffer().getValue());
        futuresInfo.setTotalBidCount(res.getAccumulateBidCount().getValue());
        futuresInfo.setTotalBidVolume(res.getAccumulateBidVolume().getValue());
        futuresInfo.setTotalOfferCount(res.getAccumulateOfferCount().getValue());
        futuresInfo.setTotalOfferVolume(res.getAccumulateOfferVolume().getValue());
        futuresInfo.setIndustry(res.getIndustry().getValue());
        futuresInfo.setRights(res.getRights().getValue());
        futuresInfo.setBaseCode(res.getBaseCode().getValue());
        futuresInfo.setOpenInterest(NumberUtil.round2Decimal(res.getOpenInterest().getValue()));
        futuresInfo.setOpenInterestChange(NumberUtil.round2Decimal(Math.max(res.getOpenInterestChange().getValue(), 0)));
        futuresInfo.setNormalForeignerBuyValue(NumberUtil.round2Decimal(res.getNormalForeignerBuyValue().getValue()));
        futuresInfo.setNormalForeignerBuyVolume(res.getNormalForeignerBuyVolume().getValue());
        futuresInfo.setNormalForeignerSellValue(NumberUtil.round2Decimal(res.getNormalForeignerSellValue().getValue()));
        futuresInfo.setNormalForeignerSellVolume(res.getNormalForeignerSellVolume().getValue());
        futuresInfo.setPtForeignerTotalBuyValue(NumberUtil.round2Decimal(res.getPtForeignerTotalBuyValue().getValue()));
        futuresInfo.setPtForeignerTotalBuyVolume(res.getPtForeignerTotalBuyVolume().getValue());
        futuresInfo.setPtForeignerTotalSellValue(NumberUtil.round2Decimal(res.getPtForeignerTotalSellValue().getValue()));
        futuresInfo.setPtForeignerTotalSellVolume(res.getPtForeignerTotalSellVolume().getValue());
        futuresInfo.setRemainDate((long) res.getRemainDate().getValue());
        futuresInfo.setTheoryPrice(NumberUtil.round2Decimal(res.getTheoryPrice().getValue()));
        futuresInfo.setBasis(NumberUtil.round2Decimal(res.getBasis().getValue()));
        futuresInfo.setTheoryBasis(NumberUtil.round2Decimal(res.getTheoryBasis().getValue()));
        futuresInfo.setMarketBasis(NumberUtil.round2Decimal(res.getMarketBasis().getValue()));
        futuresInfo.setDisparate(NumberUtil.round2Decimal(res.getDisparate().getValue()));
        futuresInfo.setDisparateRate(NumberUtil.round2Decimal(res.getDisparateRate().getValue()));
        try {
            futuresInfo.setFirstTradingDate(DefaultUtils.DATE_FORMAT().parse(res.getStartDate().getValue()));
        } catch (ParseException ignore) {
        }
        try {
            futuresInfo.setMaturityDate(DefaultUtils.DATE_FORMAT().parse(res.getEndDate().getValue()));
        } catch (ParseException ignore) {
        }
        return futuresInfo;
    };

    private static String trim(String s) {
        return s == null ? s : s.trim();
    }
}
