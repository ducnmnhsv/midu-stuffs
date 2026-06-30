package com.difisoft.nhsv.admin.domain.request;

import com.difisoft.model.requests.DataRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CopyTradingSendOTPRequest extends DataRequest implements BaseRequest {
    private String id;
    private String idType;
    private String txType;

    public String getAcceptLanguage() {
        return this.headers != null ? this.headers.getAcceptLanguage() : "en";
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
