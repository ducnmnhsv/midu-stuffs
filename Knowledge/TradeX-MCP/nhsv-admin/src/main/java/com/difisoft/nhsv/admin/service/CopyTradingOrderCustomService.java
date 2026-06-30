package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.request.CopyTradingOrderRequest;
import com.difisoft.nhsv.admin.service.dto.CopyTradingOrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyTradingOrder}.
 */
public interface CopyTradingOrderCustomService extends CopyTradingOrderService {
    Page<CopyTradingOrderDTO> findAllByCopyPortfolioIdAndCopySubscriberIdAndOthers(
        CopyTradingOrderRequest request,
        Pageable pageable
    );
}
