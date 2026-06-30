package com.difisoft.nhsv.admin.web.rest.controllers;

import com.difisoft.nhsv.admin.service.CopyPortfolioDetailHistoryCustomService;
import com.difisoft.nhsv.admin.service.CopyPortfolioHistoryCustomService;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailHistoryDTO;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailsDTO;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioHistoryDTO;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/copy-trading/history-portfolio")
@RequiredArgsConstructor
public class CopyPortfolioHistoryController {
    private final CopyPortfolioHistoryCustomService copyPortfolioHistoryCustomService;
    private final CopyPortfolioDetailHistoryCustomService copyPortfolioDetailHistoryCustomService;

    @GetMapping
    public ResponseEntity<List<CopyPortfolioHistoryDTO>> getAllByMlId(@RequestParam(value = "mlID") Long mlID,
                                                                     @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
                                                                     @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
                                                                     @ParameterObject Pageable pageable) {
        Page<CopyPortfolioHistoryDTO> pageResult = copyPortfolioHistoryCustomService.findAllByMlUserIdId(mlID, fromDate, toDate, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), pageResult);
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    @GetMapping("/detail")
    public ResponseEntity<List<CopyPortfolioDetailHistoryDTO>> getAllByPortfolioIdId(@RequestParam(value = "id") Long id,
                                                                                     @ParameterObject Pageable pageable) {

        Page<CopyPortfolioDetailHistoryDTO> pageResult = copyPortfolioDetailHistoryCustomService.findAllByCopyPortfolioIdId(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), pageResult);
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }
}
