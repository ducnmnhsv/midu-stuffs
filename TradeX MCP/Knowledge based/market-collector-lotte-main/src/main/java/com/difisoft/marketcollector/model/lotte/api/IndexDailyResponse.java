package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class IndexDailyResponse extends CommonResponse {
    @JsonProperty("total_record")
    private String totalRecord;
    @JsonProperty("data_list")
    private List<CommonListResponse<Item>> dataList;

    @Data
    public static class Item {
        private String date;
        private Double code;
        private Double close;
        private Double change;
        private Double changeRate;
        @JsonProperty("amount")
        private Double value;
        @JsonProperty("value")
        private Long volume;
        private Double open;
        private Double high;
        private Double low;
    }
}
