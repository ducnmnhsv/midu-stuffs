package com.techx.tradex.realtime.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FloorOrCeilingRequest {
    private String symbol;
    private boolean isCeilPrice;
    private Double lastPrice;
    private Double rate;
}
