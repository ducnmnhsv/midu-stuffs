package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.CopyPortfolioHistory;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface CopyPortfolioHistoryCustomService extends CopyPortfolioHistoryService {
    Page<CopyPortfolioHistoryDTO> findAllByMlUserIdId(Long cpId, Date fromDate, Date toDate, Pageable pageable);

    CopyPortfolioHistory save(CopyPortfolioHistory entity);
}
