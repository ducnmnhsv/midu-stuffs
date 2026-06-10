package com.difisoft.marketcollector.model.realtime;

import com.difisoft.htsconnection.socket.message.receive.BidOfferAutoItem;
import com.difisoft.model.utils.NumberUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BidOfferData extends TransformData<BidOfferAutoItem> {
    private int bidPrice;
    private int offerPrice;
    private int bidVolume;
    private int offerVolume;
    private List<PriceItem> bidOfferList;
    private long totalBidVolume;
    private long totalOfferVolume;
    private long totalBidCount;
    private long totalOfferCount;
    private int diffBidOffer;
    private Double expectedPrice;
    private Double expectedChange;
    private Double expectedRate;
    private String session;

    @Override
    public void parse(BidOfferAutoItem item) {
        this.setCode(item.getStockCode().getValue());
        this.setTime(item.getTime().getValue());
        this.setBidPrice(item.getBidPrice().getValue());
        this.setOfferPrice(item.getOfferPrice().getValue());
        this.setBidVolume(item.getBidVolume().getValue());
        this.setOfferVolume(item.getOfferVolume().getValue());
        this.setTotalBidVolume(item.getTotalBidVolume().getValue());
        this.setTotalOfferVolume(item.getTotalOfferVolume().getValue());
        this.setTotalBidCount(item.getAccumulativeBidCount().getValue());
        this.setTotalOfferCount(item.getAccumulativeOfferCount().getValue());
        this.setDiffBidOffer(item.getDiffBidOffer().getValue());
        this.setBidOfferList(item.getBidOfferPrices().stream().map(bo -> new PriceItem().parse(bo)).collect(Collectors.toList()));
        this.setSession(FuturesBidOfferData.sessionControlMap.get(item.getControlCode().getValue()));
        double expectedPrice = item.getProjectOpen().getValue();

        if (!("ATO".equalsIgnoreCase(session) || "ATC".equalsIgnoreCase(session))
                || expectedPrice <= 0) {
            this.expectedPrice = null;
            this.expectedChange = null;
            this.expectedRate = null;
        } else {
            this.expectedPrice = NumberUtils.round1Decimal(expectedPrice);
        }
    }

    @Data
    public static class PriceItem {
        private int bidPrice;
        private int offerPrice;
        private int bidVolume;
        private int offerVolume;
        private int bidVolumeChange;
        private int offerVolumeChange;

        public PriceItem parse(BidOfferAutoItem.SubItem item) {
            this.setBidPrice(item.getBidPrice().getValue());
            this.setOfferPrice(item.getOfferPrice().getValue());
            this.setBidVolume(item.getBidVolume().getValue());
            this.setOfferVolume(item.getOfferVolume().getValue());
            this.setBidVolumeChange(item.getBidVolumeChange().getValue());
            this.setOfferVolumeChange(item.getOfferVolumeChange().getValue());
            return this;
        }
    }

    @Override
    public Object toRealObject() {
        return this;
    }

    public void setExpectedChange(Double expectedChange) {
        this.expectedChange = expectedChange;
    }

    public void setExpectedRate(Double expectedRate) {
        this.expectedRate = expectedRate;
    }
}
