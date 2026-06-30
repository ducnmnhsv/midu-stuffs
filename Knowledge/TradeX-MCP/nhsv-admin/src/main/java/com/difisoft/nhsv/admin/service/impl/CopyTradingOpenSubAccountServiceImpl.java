package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.CopyTradingRegister;
import com.difisoft.nhsv.admin.domain.request.*;
import com.difisoft.nhsv.admin.domain.response.CopyTradingCheckSubStatusGetValResponse;
import com.difisoft.nhsv.admin.domain.response.CopyTradingCheckSubStatusResponse;
import com.difisoft.nhsv.admin.domain.response.CopyTradingOpenSubAccountCallApiLotteResponse;
import com.difisoft.nhsv.admin.domain.response.CopyTradingOpenSubAccountResponse;
import com.difisoft.nhsv.admin.repository.CopyTradingRegisterRepository;
import com.difisoft.nhsv.admin.service.CopyTradingOpenSubAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CopyTradingOpenSubAccountServiceImpl implements CopyTradingOpenSubAccountService {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private final StringRedisTemplate redisTemplate;
    private final CopyTradingRegisterRepository copyTradingRegisterRepository;
    @Value("${app.rootURL}")
    private String rootURL;
    @Value("${app.nhsvConfig.headers.api-key}")
    private String apiKey;

    @Override
    public CopyTradingOpenSubAccountResponse openSubAccount(CopyTradingOpenSubAccountRequest request,
                                                            RequestContext<CopyTradingOpenSubAccountRequest> ctx) {

        log.info("[openSubAccount] ctxId: {}, copyTradingOpenSubAccountRequest: {}", ctx.getId(), request);

        List<String> validSubNumbers = Arrays.asList("81", "82", "83");
        if (!validSubNumbers.contains(request.getSubNumber())) {
            throw new GeneralException(Constants.INPUT_INVALID);
        }

        String customName = request.getName();

        String otpKey = request.getOtpKey();
        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        String expiredTimeStr = redisTemplate.opsForValue().get(otpKey + "_time");

        if (storedOtp == null) {
            throw new GeneralException(Constants.INCORRECT_OTP);
        }

        LocalDateTime expiredTime = LocalDateTime.parse(expiredTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (expiredTime.isBefore(LocalDateTime.now())) {
            throw new GeneralException(Constants.OTP_EXPIRED);
        }

        List<String> accountNumbers = request.getAccountNumbers();
        String identifierNumber = request.getIdentifierNumber();

        String val = getValByApiCore(accountNumbers.get(0), request.getSubNumber());

        Boolean status = "Y".equals(val) ? Boolean.TRUE : "N".equals(val) ? Boolean.FALSE : null;

        CopyTradingOpenSubAccountCallApiLotteResponse responseData =
            getResponseByApiCore(accountNumbers.get(0), request.getSubNumber(), identifierNumber);

        if (responseData != null) {
            if (responseData.isSuccess()) {

                CopyTradingRegister result = new CopyTradingRegister();
                result.setAccountNumber(accountNumbers.get(0));
                result.setSubAccount(request.getSubNumber());
                result.setCustomerName(customName);
                result.setStatus(status);
                result.setCreateAt(ZonedDateTime.now());
                copyTradingRegisterRepository.save(result);
                return new CopyTradingOpenSubAccountResponse(responseData.getErrorDesc());
            } else {
                throw new GeneralException(responseData.getErrorDesc());
            }
        }
        throw new GeneralException("Unknown error occurred while processing the sub-account request.");
    }

    @Override
    public CopyTradingCheckSubStatusResponse checkSubStatus(CopyTradingCheckSubStatusRequest request,
                                                            RequestContext<CopyTradingCheckSubStatusRequest> ctx) {

        log.info("[checkSubStatus] ctxId: {}, copyTradingCheckSubStatusRequest: {}", ctx.getId(), request);

        List<String> accountNumbers = request.getAccountNumbers();

        String val = getValByApiCore(accountNumbers.get(0), request.getSubNumber());

        if ("Y".equals(val)) {
            return new CopyTradingCheckSubStatusResponse(Boolean.TRUE);
        } else if ("N".equals(val)) {
            return new CopyTradingCheckSubStatusResponse(Boolean.FALSE);
        } else {
            throw new GeneralException(val);
        }
    }

    @Override
    @Scheduled(cron = "${app.cron.daily-update-status}")
    public void updateStatusJob() {

        List<CopyTradingRegister > accounts = copyTradingRegisterRepository.findAccountNumberAndSubAccountForFalseOrNull();

        if (accounts.isEmpty()) {
            log.info("[copyTradingRegister] No records found with status FALSE or NULL. Job terminated.");
            return;
        }

        for (CopyTradingRegister  account : accounts) {
            String val = getValByApiCore(account.getAccountNumber(), account.getSubAccount());

            Boolean status = "Y".equals(val) ? Boolean.TRUE : "N".equals(val) ? Boolean.FALSE : null;
            log.info("[copyTradingRegisterStatus] ctxId: {}",  status);
            if (status != null) {
                ZonedDateTime now = ZonedDateTime.now();
                copyTradingRegisterRepository.updateStatusForAccount(account.getAccountNumber(), status, now);
            }
        }
        log.info("[copyTradingRegister] Status update completed.");
    }

    private CopyTradingOpenSubAccountCallApiLotteResponse getResponseByApiCore(String acntNo, String subAccount, String idno) {

        String url = rootURL + "/tsol/apikey/tuxsvc/account/open-sub";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("apiKey", apiKey);

        CopyTradingOpenSubAccountCallApiLotteRequest requestBody = new CopyTradingOpenSubAccountCallApiLotteRequest();
        requestBody.setAcntNo(acntNo);
        requestBody.setSubNo(subAccount);
        requestBody.setFeeBk("Y");
        requestBody.setIdno(idno);

        HttpEntity<CopyTradingOpenSubAccountCallApiLotteRequest> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<CopyTradingOpenSubAccountCallApiLotteResponse> response =
            REST_TEMPLATE.exchange(url, HttpMethod.POST, entity, CopyTradingOpenSubAccountCallApiLotteResponse.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return null;
        }

        return response.getBody();
    }

    private String getValByApiCore(String acnt_no, String subNumber) {

        String url = rootURL + "/tsol/apikey/tuxsvc/account/sub-stt";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("apiKey", apiKey);

        CopyTradingCheckSubStatusCallApiLotteRequest request = new CopyTradingCheckSubStatusCallApiLotteRequest();
        request.setAcntNo(acnt_no);
        request.setSubNo(subNumber);

        HttpEntity<CopyTradingCheckSubStatusCallApiLotteRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<CopyTradingCheckSubStatusGetValResponse> response =
            REST_TEMPLATE.exchange(url, HttpMethod.POST, entity, CopyTradingCheckSubStatusGetValResponse.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return null;
        }

        CopyTradingCheckSubStatusGetValResponse responseBody = response.getBody();
        if (responseBody != null) {
            if (responseBody.isSuccess()) {
                return Objects.requireNonNull(responseBody).getDataList().get(0).getVal();
            } else {
                return Objects.requireNonNull(responseBody).getErrorDesc();
            }
        }
        return null;
    }
}
