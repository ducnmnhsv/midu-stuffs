package com.techx.tradex.common.model.kafka.request.configuration;

import lombok.Data;

@Data
public class HolidayResponse {
    private int id;
    private String date;
    private String description;
}
