package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.ZonedDateTime;
import java.util.List;

public interface CopyMarketLeaderDetailsCustomService extends CopyMarketLeaderDetailsService {
    Page<CopyMarketLeaderDetails> findAllByMlIdsAndDateRangeAndConditions(List<Long> mlUserIds, ZonedDateTime fromDate, ZonedDateTime toDate, String type, String label, String key, Pageable pageable);

    List<CopyMarketLeaderDetailsDTO> findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(List<Long> mlUserIds, String type, String label, String key, Sort sort);

    void totalSubscribersJob();
}
