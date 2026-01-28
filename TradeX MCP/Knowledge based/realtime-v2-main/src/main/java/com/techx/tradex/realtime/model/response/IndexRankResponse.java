package com.techx.tradex.realtime.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndexRankResponse {
    private List<IndexRank> indexRanks;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class IndexRank {
        private Integer rank;
        private String stockCode;
        private Double rate;
    }
}
