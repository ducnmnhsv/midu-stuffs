package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CopyPortfolioDetailHistoryCustomService extends CopyPortfolioDetailHistoryService {
    Page<CopyPortfolioDetailHistoryDTO> findAllByCopyPortfolioIdId(Long cpId, Pageable pageable);

    void saveAll(List<CopyPortfolioDetailHistory> entities);
}
