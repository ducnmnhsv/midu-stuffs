package com.difisoft.nhsv.admin.web.rest.controllers;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.nhsv.admin.domain.request.RecalculateProfitLossByPeriodRequest;
import com.difisoft.nhsv.admin.domain.response.GenericResponse;
import com.difisoft.nhsv.admin.service.CopyUserService;
import com.difisoft.nhsv.admin.service.dto.AdminUserDTO;
import com.difisoft.nhsv.admin.service.impl.CopyMarketLeaderProfitLossCustomServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/copy-trading")
@RequiredArgsConstructor
@Slf4j
public class CopyUserController {

    private final CopyUserService copyUserService;
    private final CopyMarketLeaderProfitLossCustomServiceImpl copyMarketLeaderProfitLossCustomService;

    @GetMapping("/ml-account")
    public AdminUserDTO getAccount(
        @RequestParam(value = "mlUserId", required = false) Long mlUserId
    ) {

        return copyUserService.findMLAccountInfo(mlUserId);
    }

    @PostMapping("/recalculate-profit-loss-by-period")
    public ResponseEntity<GenericResponse<String>> recalculateProfitLossByPeriod(
        @RequestBody RecalculateProfitLossByPeriodRequest request
    ) {
        GenericResponse<String> response = copyMarketLeaderProfitLossCustomService
            .recalculateProfitLossByPeriod(request, new RequestContext<>("admin_recalculateProfitLossByPeriod", null));
        return ResponseEntity.ok().body(response);
    }
}
