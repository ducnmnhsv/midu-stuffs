package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class IndexQuoteResponse extends CommonResponse {
    @JsonProperty("total_record")
    private String totalRecord;
    @JsonProperty("data_list")
    private List<ListResponse<Item>> dataList;

    @Data
    public static class Item {
        private String time;
        private Double last;
        private Double change;
        private Double changeRate;
        @JsonProperty("amount")
        private Double value;
        private Long volume;
    }

    @Data
    public static class ListResponse<I> {
        @JsonProperty("baseTime")
        protected String nextKey;
        protected boolean hasNext;
        protected List<I> list;
    }
}
