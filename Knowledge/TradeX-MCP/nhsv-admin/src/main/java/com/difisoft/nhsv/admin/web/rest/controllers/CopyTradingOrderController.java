package com.difisoft.nhsv.admin.web.rest.controllers;

import com.difisoft.nhsv.admin.domain.request.CopyTradingOrderRequest;
import com.difisoft.nhsv.admin.repository.CopyTradingOrderRepository;
import com.difisoft.nhsv.admin.service.CopyTradingOrderCustomService;
import com.difisoft.nhsv.admin.service.CopyTradingOrderQueryService;
import com.difisoft.nhsv.admin.service.CopyTradingOrderService;
import com.difisoft.nhsv.admin.service.dto.CopyTradingOrderDTO;
import com.difisoft.nhsv.admin.web.rest.CopyTradingOrderResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.util.List;


@RestController
@RequestMapping("/api/copy-trading")
public class CopyTradingOrderController extends CopyTradingOrderResource {

    private final CopyTradingOrderCustomService copyTradingOrderCustomService;

    @Autowired
    public CopyTradingOrderController(CopyTradingOrderService copyTradingOrderService, CopyTradingOrderRepository copyTradingOrderRepository, CopyTradingOrderQueryService copyTradingOrderQueryService, CopyTradingOrderCustomService copyTradingOrderCustomService) {
        super(copyTradingOrderService, copyTradingOrderRepository, copyTradingOrderQueryService);
        this.copyTradingOrderCustomService = copyTradingOrderCustomService;
    }

    @GetMapping("/ml-copy-trading-orders")
    public ResponseEntity<List<CopyTradingOrderDTO>> getAllCopyTradingOrders(CopyTradingOrderRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<CopyTradingOrderDTO> page = copyTradingOrderCustomService.findAllByCopyPortfolioIdAndCopySubscriberIdAndOthers(request, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
