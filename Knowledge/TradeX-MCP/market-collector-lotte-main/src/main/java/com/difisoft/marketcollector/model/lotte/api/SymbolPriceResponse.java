package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SymbolPriceResponse {
    @JsonProperty("total_record")
    private String totalRecord;
    @JsonProperty("data_list")
    private List<CommonListResponse<Item>> dataList;

    @Data
    // time format is hh:mm:ss (HHmmss)
    public static class Item {
        private String code;
        private String name;
        private String time;
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
        @JsonProperty("volume")
        private String tradingVolume;
        @JsonProperty("ptVol")
        private String ptVolume;
        @JsonProperty("totalVol")
        private String totalTradingVolume;
        @JsonProperty("turnoverRatio")
        private String turnoverRate;
        @JsonProperty("amount")
        private String tradingValue;
        @JsonProperty("ptAmt")
        private String ptValue;
        @JsonProperty("totalAmt")
        private String totalTradingValue;
        @JsonProperty("parAmt")
        private String parValue;
        @JsonProperty("listedStockQty")
        private String listedQuantity;
        private String foreignBuyVol;
        private String foreignSellVol;
        private String foreignTotalRoom;
        private String foreignCurrRoom;
        private String projectOpen;
        private String controlCode;
        private String high52;
        private String low52;
    }
}
