package com.difisoft.marketcollector.model.db;

import lombok.Data;

@Data
public class DividendRate {
    private String code;
    private double basicPrice;
    private double totalAdjustRate;
}
