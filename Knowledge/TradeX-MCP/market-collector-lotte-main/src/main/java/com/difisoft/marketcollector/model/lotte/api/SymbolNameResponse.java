package com.difisoft.marketcollector.model.lotte.api;

import com.difisoft.market.model.constant.MarketTypeEnum;
import com.difisoft.marketcollector.constants.LotteExchange;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SymbolNameResponse {
    @JsonProperty("total_record")
    private String totalRecord;
    @JsonProperty("data_list")
    private List<CommonListResponse<Item>> dataList;

    @Data
    public static class Item {
        private String symbol;
        private String code;
        private LotteExchange exchange;
        private String englishName;
        private String vietnameseName;
        private SymbolType type;

        public MarketTypeEnum getExchange() {
            return this.exchange.toMarketType();
        }
    }
}
