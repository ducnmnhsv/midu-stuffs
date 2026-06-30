package com.difisoft.nhsv.admin.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfitLossPeriodDTO {
    private ZonedDateTime startDate;
    private ZonedDateTime currentDate;
}
