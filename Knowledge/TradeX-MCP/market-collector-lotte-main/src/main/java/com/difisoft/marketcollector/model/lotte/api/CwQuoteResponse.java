package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CwQuoteResponse extends CommonResponse {
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
        @JsonProperty("matchVol")
        private Long volume;
    }

    @Data
    public static class ListResponse<I> {
        @JsonProperty("seq")
        protected String nextKey;
        protected boolean hasNext;
        protected List<I> list;
    }
}
