package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.model.exceptions.SubErrorsException;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.request.CopyTradingSubscriberEstPortfolioValueRequest;
import com.difisoft.nhsv.admin.domain.request.InquiryAssetInformationRequest;
import com.difisoft.nhsv.admin.domain.response.CopyTradingSubscriberEstPortfolioValueResponse;
import com.difisoft.nhsv.admin.domain.response.InquiryAssetInformationResponse;
import com.difisoft.nhsv.admin.service.CopyTradingSubscriberEstPortfolioValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CopyTradingSubscriberEstPortfolioValueServiceImpl implements CopyTradingSubscriberEstPortfolioValueService {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Value("${app.rootURL}")
    private String rootURL;

    @Value("${app.nhsvConfig.headers.api-key}")
    private String apiKey;

    @Override
    public CopyTradingSubscriberEstPortfolioValueResponse getEstPortfolioValue(CopyTradingSubscriberEstPortfolioValueRequest request,
                                                                               RequestContext<CopyTradingSubscriberEstPortfolioValueRequest> ctx) {

        log.info("[getEstPortfolioValue] ctxId: {}, CopyTradingSubscriberEstPortfolioValueRequest: {}", ctx.getId(), request);

        if (request.getAllocatedRatio() == null || request.getAllocatedRatio() < 0 || request.getAllocatedRatio() > 1) {
            throw new SubErrorsException(Constants.FIELD_IS_REQUIRED)
                .add(Constants.ALLOCATED_RATIO_IS_REQUIRED, Constants.ALLOCATED_RATIO, Collections.singletonList(Constants.ALLOCATED_RATIO));
        }

        if (request.getSubNumber() == null || request.getSubNumber().isEmpty()) {
            throw new SubErrorsException(Constants.FIELD_IS_REQUIRED)
                .add(Constants.SUB_NUMBER_IS_REQUIRED, Constants.SUB_NUMBER, Collections.singletonList(Constants.SUB_NUMBER));
        }

        List<String> accountNumbers = request.getAccountNumbers();

        String bankCode = request.getBankCode(request.getSubNumber());

        if (bankCode == null) {
            throw new SubErrorsException(Constants.INVALID_PARAMETER)
                .add(Constants.INVALID_SUB_NUMBER, Constants.SUB_NUMBER, Collections.singletonList(request.getSubNumber()));
        }

        log.info("Checking values before calling getInquiryAssetInformationByApiCore:");
        log.info("accountNumbers: {}", accountNumbers);
        log.info("First account number (accountNumbers.get(0)): {}", (accountNumbers != null && !accountNumbers.isEmpty()) ? accountNumbers.get(0) : "null or empty");
        log.info("Sub number: {}", request.getSubNumber());
        log.info("Bank code: {}", bankCode);

        InquiryAssetInformationResponse response = getInquiryAssetInformationByApiCore(
            accountNumbers.get(0), request.getSubNumber(), bankCode);
        if (response == null) {
            throw new SubErrorsException(Constants.INVALID_PARAMETER)
                .add(Constants.INVALID_SUB_NUMBER, Constants.SUB_NUMBER, Collections.singletonList(request.getSubNumber()));
        }

        Double cashBalance = null;
        Double totalAsset = null;
        if (response.getDataList() != null && !response.getDataList().isEmpty()) {
            InquiryAssetInformationResponse.InquiryAssetInformation assetInfo = response.getDataList().get(0);
            if (assetInfo != null) {
                cashBalance = assetInfo.getCashNotHold();
                totalAsset = assetInfo.getTotalAsset();
            }
        }

        double estAvailableBalanceBefore = (Objects.isNull(cashBalance) ? 0 : cashBalance) - (cashBalance * request.getAllocatedRatio());
        double estStockValue = Math.round((Objects.isNull(totalAsset) ? 0 : totalAsset) - estAvailableBalanceBefore);
        double estAvailableBalanceAfter = Math.round(estAvailableBalanceBefore);
        return CopyTradingSubscriberEstPortfolioValueResponse.builder()

            .estAvailableBalance(estAvailableBalanceAfter)
            .estStockValue(estStockValue)
            .build();
    }

    private InquiryAssetInformationResponse getInquiryAssetInformationByApiCore(String acntNo, String subNumber, String bankCode) {

        String url = rootURL + "/tsol/apikey/tuxsvc/account/get-asset-info";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("apiKey", apiKey);

        InquiryAssetInformationRequest request = new InquiryAssetInformationRequest();
        request.setAcntNO(acntNo);
        request.setSubNo(subNumber);
        request.setBankCode(bankCode);

        HttpEntity<InquiryAssetInformationRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<InquiryAssetInformationResponse> response =
            REST_TEMPLATE.exchange(url, HttpMethod.POST, entity, InquiryAssetInformationResponse.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            log.info("Failed to get asset information: Status code {} - Response body: {}",
                response.getStatusCode(), response.getBody());
            return null;
        }
    }
}
