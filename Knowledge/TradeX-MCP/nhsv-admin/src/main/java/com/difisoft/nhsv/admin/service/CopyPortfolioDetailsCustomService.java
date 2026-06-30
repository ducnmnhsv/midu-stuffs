package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.CopyPortfolioDetails;
import com.difisoft.nhsv.admin.domain.request.PortfolioUploadRequest;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolioDetails}.
 */
public interface CopyPortfolioDetailsCustomService extends CopyPortfolioDetailsService {
    Page<CopyPortfolioDetailsDTO> findAllByMlId(Long mlID, Pageable pageable);
    List<CopyPortfolioDetailsDTO> findAllDTOByCopyPortfolioIds(List<Long> copyPortfolioIds);
    List<CopyPortfolioDetails> findAllByCopyPortfolioIds(List<Long> copyPortfolioIds);

    void uploadPortfolio(PortfolioUploadRequest request);
}
