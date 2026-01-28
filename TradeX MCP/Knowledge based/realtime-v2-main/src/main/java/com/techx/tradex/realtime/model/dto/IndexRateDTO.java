package com.techx.tradex.realtime.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class IndexRateDTO extends IndexDTO {
    private Double rate;
    private Double tradingValue;
}
