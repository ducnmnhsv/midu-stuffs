package com.techx.tradex.realtime.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotifiedCeilingOrFloorPriceSymbolStatusDTO {
    private LocalDate currentDate;
    private List<SymbolStatus> symbolStatus;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class SymbolStatus {
        private String symbol;
        private String priceType;
        private boolean isCeiling;
        private boolean isFloor;
    }
}
