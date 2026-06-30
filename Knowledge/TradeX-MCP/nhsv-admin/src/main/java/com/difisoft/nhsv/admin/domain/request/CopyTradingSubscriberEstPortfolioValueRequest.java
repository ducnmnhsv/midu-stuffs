package com.difisoft.nhsv.admin.domain.request;

import com.difisoft.nhsv.admin.domain.customrequest.CustomBaseRequest;
import com.difisoft.nhsv.admin.domain.customrequest.CustomDataRequest;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CopyTradingSubscriberEstPortfolioValueRequest extends CustomDataRequest implements CustomBaseRequest {
    private Double allocatedRatio;
    private String subNumber;

    public String getBankCode(String subNumber) {
        if (headers != null && headers.getToken() != null && headers.getToken().getUserData() != null) {
            Map<String, List<String>> bankInfo = headers.getToken().getUserData().getBankInfo();
            if (bankInfo != null) {
                List<String> bankCodes = bankInfo.get(subNumber);
                if (bankCodes != null && !bankCodes.isEmpty()) {
                    return bankCodes.get(0);
                }
            }
        }
        return null;
    }

    public List<String> getAccountNumbers() {
        if (headers != null) {
            if (headers.getToken() != null)
                if (headers.getToken().getUserData() != null)
                    return headers.getToken().getUserData().getAccountNumbers();
        }
        return null;
    }
}
