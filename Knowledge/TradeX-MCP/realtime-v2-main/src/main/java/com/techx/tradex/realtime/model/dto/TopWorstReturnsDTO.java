package com.techx.tradex.realtime.model.dto;

import com.techx.tradex.realtime.model.response.IndexRankResponse;
import com.techx.tradex.realtime.utils.NumberUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopWorstReturnsDTO {

    private String symbol;
    private Double rate;

    public static String makeMessageData(List<IndexRankResponse.IndexRank> indexRanks) {
        return indexRanks.stream().map(item ->
                MessageFormat.format("{0} ({1}%)", item.getStockCode(), NumberUtils.round(2, item.getRate()))
        ).collect(Collectors.joining(", "));
    }
}
