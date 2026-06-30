package com.difisoft.nhsv.admin.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TriggerCrawlEventStock {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private Date fromDate = Date.from(LocalDateTime.of(2025, 1, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private Date toDate = new Date(); // now
}
