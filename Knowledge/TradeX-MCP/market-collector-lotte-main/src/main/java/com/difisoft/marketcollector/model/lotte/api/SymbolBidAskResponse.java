package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SymbolBidAskResponse {
    @JsonProperty("total_record")
    private String totalRecord;
    @JsonProperty("data_list")
    private List<Item> dataList;

    @Data
    // time format is hh:mm:ss (HHmmss)
    public static class Item {
        private String code;
        private String marketName;
        private String ceiling;
        private String floor;
        private String refPrice;
        private String avgPrice;
        private String open;
        private String high;
        private String highTime;
        private String low;
        private String lowTime;
        private String last;
        private String change;
        private String changeRate;
        @JsonProperty("matchedVol")
        private String matchVolume;
        @JsonProperty("volume")
        private String tradingVolume;
        @JsonProperty("totalVol")
        private String totalTradingVolume;
        @JsonProperty("projectOpen")
        private String expectedPrice;
        private String controlCode;
        @JsonProperty("totalBidSize")
        private String totalBidVolume;
        @JsonProperty("totalOfferSize")
        private String totalOfferVolume;
        @JsonProperty("ptVol")
        private String ptVolume;
        @JsonProperty("bidOfferList")
        private List<BidAskItem> bidAsks;
    }

    @Data
    public static class BidAskItem {
        @JsonProperty("bid")
        private String bidPrice;
        @JsonProperty("bidSize")
        private String bidVolume;
        @JsonProperty("offer")
        private String offerPrice;
        @JsonProperty("offerSize")
        private String offerVolume;
    }
}
