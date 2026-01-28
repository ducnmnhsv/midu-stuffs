package com.difisoft.marketcollector.model.realtime;

import com.difisoft.htsconnection.socket.message.receive.FuturesAutoItem;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.marketcollector.services.CacheService;
import com.difisoft.marketcollector.utils.NumberUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class FuturesUpdateData extends TransformData<FuturesAutoItem> {
    private String refCode;
    private String highTime;
    private String lowTime;
    private double open;
    private double high;
    private double low;
    private double last;
    private double change;
    private double referencePrice;
    private double averagePrice;
    private double basis;
    private double rate;
    private int matchingVolume;
    private long tradingVolume;
    private long tradingValue;
    private double bidPrice;
    private double offerPrice;
    private int bidVolume;
    private int offerVolume;
    private long totalBidVolume;
    private long totalBidCount;
    private long totalOfferVolume;
    private long totalOfferCount;
    private long foreignerBuyVolume;
    private long foreignerSellVolume;
    private String matchedBy;
    private String type = SymbolTypeEnum.FUTURES.name();

    @JsonIgnore
    private long timeInsertToQueue;

    @Override
    public Object toRealObject() {
        return this;
    }

    @Override
    public void validate() {
        if (this.open == 0 || this.high == 0 || this.low == 0 || this.last == 0) {
            throw new IgnoreException("ignore because one of condition is true 'this.open == 0 || this.high == 0 || this.low == 0 || this.last == 0'");
        }
    }

    @Override
    public void parse(FuturesAutoItem item) {
        this.setCode(item.getCode().getValue());
        this.setTime(item.getTime().getValue());
        this.setHighTime(item.getHighTime().getValue());
        this.setLowTime(item.getLowTime().getValue());
        this.setOpen(NumberUtil.round2Decimal(item.getOpen().getValue()));
        this.setHigh(NumberUtil.round2Decimal(item.getHigh().getValue()));
        this.setLow(NumberUtil.round2Decimal(item.getLow().getValue()));
        this.setLast(NumberUtil.round2Decimal(item.getLast().getValue()));
        this.setChange(NumberUtil.round2Decimal(item.getChange().getValue()));
        this.setReferencePrice(NumberUtil.round2Decimal(item.getReferencePrice().getValue()));
        this.setAveragePrice(NumberUtil.round2Decimal(item.getAveragePrice().getValue()));
        this.setBasis(NumberUtil.round2Decimal(item.getBasis().getValue()));
        this.setRate(NumberUtil.round2Decimal(item.getRate().getValue()));
        this.setMatchingVolume(item.getMatchVolume().getValue());
        this.setTradingVolume(item.getTradingVolume().getValue());
        this.setTradingValue(item.getTradingValue().getValue() * 1000000);
        this.setBidPrice(NumberUtil.round2Decimal(item.getBidPrice().getValue()));
        this.setOfferPrice(NumberUtil.round2Decimal(item.getOfferPrice().getValue()));
        this.setBidVolume(item.getBidVolume().getValue());
        this.setOfferVolume(item.getOfferVolume().getValue());
        this.setTotalBidVolume(item.getAccumulateBidVolume().getValue());
        this.setTotalBidCount(item.getAccumulateBidCount().getValue());
        this.setTotalOfferVolume(item.getAccumulateOfferVolume().getValue());
        this.setTotalOfferCount(item.getAccumulateOfferCount().getValue());
        this.setForeignerBuyVolume(item.getForeignerBuyVolume().getValue());
        this.setForeignerSellVolume(item.getForeignerSellVolume().getValue());

        if (item.getMatchVolume().getIndex() == 66) {
            this.matchedBy = "ASK";
        } else if (item.getMatchVolume().getIndex() == 83) {
            this.matchedBy = "BID";
        } else {
            this.matchedBy = null;
        }
    }

    @Override
    public void formatRefCode(CacheService cacheService) {
        this.refCode = cacheService.getFuturesCodeRefMap().get(this.code);
    }
}
