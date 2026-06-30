package com.techx.tradex.realtime.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EodSnapshotEvent(
        @JsonProperty("schema_version") String schemaVersion,
        String symbol,
        @JsonProperty("trading_date") String tradingDate,
        @JsonProperty("snapshot_at") String snapshotAt,
        @JsonProperty("snapshot_run_id") String snapshotRunId,
        PriceInfo price,
        VolumeInfo volume,
        ForeignInfo foreign,
        MetadataInfo metadata,
        OrderInfo order
) {
    public record PriceInfo(
            Double open,
            Double high,
            Double low,
            Double close,
            Double ceiling,
            Double floor,
            Double reference,
            Double average
    ) {}

    public record VolumeInfo(
            Long total,
            Double value
    ) {}

    public record ForeignInfo(
            @JsonProperty("buy_volume") Long buyVolume,
            @JsonProperty("sell_volume") Long sellVolume,
            @JsonProperty("hold_volume") Long holdVolume,
            @JsonProperty("hold_ratio") Double holdRatio,
            @JsonProperty("buy_able_ratio") Double buyAbleRatio,
            @JsonProperty("current_room") Long currentRoom,
            @JsonProperty("total_room") Long totalRoom
    ) {}

    public record MetadataInfo(
            @JsonProperty("listed_quantity") Long listedQuantity,
            String exchange,
            @JsonProperty("security_type") String securityType
    ) {}

    public record OrderInfo(
            @JsonProperty("buy_vol") Long buyVol,
            @JsonProperty("sell_vol") Long sellVol,
            @JsonProperty("best_bid_vol") Long bestBidVol,
            @JsonProperty("best_sell_vol") Long bestSellVol
    ) {}
}
