package com.difisoft.marketcollector.model.lotte.api;

import com.difisoft.marketcollector.constants.LotteExchange;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class IndexListResponse extends CommonResponse {
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
        private String type;
    }
}
