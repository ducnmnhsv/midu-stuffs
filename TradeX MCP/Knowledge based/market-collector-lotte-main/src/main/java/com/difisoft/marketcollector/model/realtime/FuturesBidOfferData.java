package com.difisoft.marketcollector.model.realtime;

import com.difisoft.htsconnection.socket.message.receive.FuturesBidOfferItem;
import com.difisoft.model.utils.NumberUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableMap;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FuturesBidOfferData extends TransformData<FuturesBidOfferItem> {
    static Map<String, String> sessionControlMap = ImmutableMap.of(
            "P", "ATO",
            "A", "ATC",
            "O", "CONTINUOUS"
    );
    private double bidPrice;
    private double offerPrice;
    private int bidVolume;
    private int offerVolume;
    private List<PriceItem> bidOfferList;
    private long totalOfferVolume;
    private long totalOfferCount;
    private long totalBidVolume;
    private long totalBidCount;
    private Double expectedPrice;
    private Double expectedChange;
    private Double expectedRate;
    private String session;

    @Override
    public void parse(FuturesBidOfferItem item) {
        this.setCode(item.getCode().getValue());
        this.setTime(item.getTime().getValue());
        this.setBidPrice(NumberUtils.round2DecimalFloatToDouble(item.getBidPrice().getValue()));
        this.setOfferPrice(NumberUtils.round2DecimalFloatToDouble(item.getOfferPrice().getValue()));
        this.setBidVolume(item.getBidVolume().getValue());
        this.setOfferVolume(item.getOfferVolume().getValue());
        this.setBidOfferList(item.getBidOfferList().stream().map(bo -> new PriceItem().parse(bo)).collect(Collectors.toList()));
        this.setTotalOfferVolume(item.getTotalOfferVolume().getValue());
        this.setTotalOfferCount(item.getAccumulateOfferCount().getValue());
        this.setTotalBidVolume(item.getTotalBidVolume().getValue());
        this.setTotalBidCount(item.getAccumulateBidCount().getValue());
        this.setSession(sessionControlMap.get(item.getControlCode().getValue()));
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
        private double bidPrice;
        private double offerPrice;
        private int bidVolume;
        private int offerVolume;
        private int bidVolumeChange;
        private int offerVolumeChange;

        public PriceItem parse(FuturesBidOfferItem.SubItem item) {
            this.setOfferPrice(NumberUtils.round2DecimalFloatToDouble(item.getOfferPrice().getValue()));
            this.setBidPrice(NumberUtils.round2DecimalFloatToDouble(item.getBidPrice().getValue()));
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
}
