package com.difisoft.nhsv.admin.web.rest.controllers;

import com.difisoft.nhsv.admin.domain.request.MarketHistoryRequest;
import com.difisoft.nhsv.admin.domain.response.MarketHistoryJobResultResponse;
import com.difisoft.nhsv.admin.domain.response.primary.MarketHistoryJobResultPrimaryResponse;
import com.difisoft.nhsv.admin.service.MarketHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;



@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class MarketHistoryController {

    private final MarketHistoryService marketHistoryService;

    @PostMapping("/create-market-history")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'SUPER_ADMINISTRATOR')")
    public ResponseEntity<Void> createMarketHistory(@RequestBody MarketHistoryRequest request) {

        marketHistoryService.uploadMarketHistory(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/latest-job-result")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'SUPER_ADMINISTRATOR')")
    public ResponseEntity<MarketHistoryJobResultResponse> getLatestJobResultByUserId() {

        MarketHistoryJobResultResponse latestJobResult = marketHistoryService.getLatestJobResult();
        if (latestJobResult == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(latestJobResult);
    }

    @GetMapping("/job-result")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'SUPER_ADMINISTRATOR')")
    public ResponseEntity<Page<MarketHistoryJobResultPrimaryResponse>> getJobResultByUserId(@ParameterObject Pageable pageable) {
        Page<MarketHistoryJobResultPrimaryResponse> result = marketHistoryService.getJobResult(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), result);
        log.info("content {}", result.getContent());
        return ResponseEntity
            .ok()
            .headers(headers)
            .body(result);
    }
}
