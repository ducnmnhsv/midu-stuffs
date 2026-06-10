package com.difisoft.marketcollector.model.realtime;

import com.difisoft.htsconnection.socket.message.receive.StockAutoItem;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.model.utils.NumberUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class StockUpdateData extends TransformData<StockAutoItem> {
    private String highTime;
    private String lowTime;
    private int ceilingPrice;
    private int floorPrice;
    private int referencePrice;
    private int averagePrice;
    private int open;
    private int high;
    private int low;
    private int last;
    private int change;
    private double rate;
    private double turnoverRate;
    private int matchingVolume;
    private long tradingVolume;
    private long tradingValue;
    private double breakEven;
    private int bidPrice;
    private int offerPrice;
    private int bidVolume;
    private int offerVolume;
    private long totalBidVolume; //accumulateBidVolume
    private long totalBidCount; //accumulateBidCount
    private long totalOfferVolume; //accumulateOfferVolume
    private long totalOfferCount; //accumulateOfferCount

    private long foreignerBuyVolume;
    private long foreignerSellVolume;
    private long foreignerTotalRoom;
    private long foreignerCurrentRoom;

    private String matchedBy;
    private String type = SymbolTypeEnum.STOCK.name();

    @JsonIgnore
    private long timeInsertToQueue;

    @Override
    public Object toRealObject() {
        return this;
    }

    @Override
    public void validate() {
        if (this.open == 0 || this.high == 0 || this.low == 0 || this.last == 0) {
            throw new IgnoreException("ignore since one of these is true 'this.open == 0 || this.high == 0 || this.low == 0 || this.last == 0'");
        }
    }

    @Override
    public void parse(StockAutoItem stockAutoItem) {
        this.setCode(stockAutoItem.getStockCode().getValue());
        this.setTime(stockAutoItem.getTime().getValue());
        this.setHighTime(stockAutoItem.getHighTime().getValue());
        this.setLowTime(stockAutoItem.getLowTime().getValue());
        this.setCeilingPrice(stockAutoItem.getCeiling().getValue());
        this.setFloorPrice(stockAutoItem.getFloor().getValue());
        this.setReferencePrice(stockAutoItem.getRefPrice().getValue());
        this.setAveragePrice(stockAutoItem.getAvgPrice().getValue());
        this.setOpen(stockAutoItem.getOpen().getValue());
        this.setHigh(stockAutoItem.getHigh().getValue());
        this.setLow(stockAutoItem.getLow().getValue());
        this.setLast(stockAutoItem.getLast().getValue());
        this.setChange(stockAutoItem.getChange().getValue());
        this.setRate(NumberUtils.round2DecimalFloatToDouble(stockAutoItem.getRate().getValue()));
        this.setTurnoverRate(NumberUtils.round2DecimalFloatToDouble(stockAutoItem.getTurnOverRate().getValue()));
        this.setMatchingVolume(stockAutoItem.getMatchVolume().getValue());
        this.setTradingVolume(stockAutoItem.getVolume().getValue());
        this.setTradingValue(stockAutoItem.getValue().getValue() * 1000000);
        this.setBidPrice(stockAutoItem.getBid().getValue());
        this.setOfferPrice(stockAutoItem.getOffer().getValue());
        this.setBidVolume(stockAutoItem.getBidVolume().getValue());
        this.setOfferVolume(stockAutoItem.getOfferVolume().getValue());
        this.setTotalBidVolume(stockAutoItem.getTotalBidVolume().getValue());
        this.setTotalBidCount(stockAutoItem.getTotalBidCount().getValue());
        this.setTotalOfferVolume(stockAutoItem.getTotalOfferVolume().getValue());
        this.setTotalOfferCount(stockAutoItem.getTotalOfferCount().getValue());
        this.setForeignerBuyVolume(stockAutoItem.getForeignerBuyVolume().getValue());
        this.setForeignerSellVolume(stockAutoItem.getForeignerSellVolume().getValue());
        this.setForeignerTotalRoom(stockAutoItem.getForeignerTotalRoom().getValue());
        this.setForeignerCurrentRoom(stockAutoItem.getForeignerCurrentRoom().getValue());

        if (stockAutoItem.getMatchVolume().getIndex() == 66) {
            this.matchedBy = "ASK";
        } else if (stockAutoItem.getMatchVolume().getIndex() == 83) {
            this.matchedBy = "BID";
        } else {
            this.matchedBy = null;
        }
    }
}
