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
public class CopyTradingOpenSubAccountRequest extends DataRequest implements BaseRequest{
    private String otpKey;
    private String subNumber;

    public String getName() {
        if (headers != null) {
            if (headers.getToken() != null)
                if (headers.getToken().getUserData() != null)
                    return headers.getToken().getUserData().getName();
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

    public String getIdentifierNumber() {
        if (headers != null) {
            if (headers.getToken() != null)
                if (headers.getToken().getUserData() != null)
                    return headers.getToken().getUserData().getIdentifierNumber();
        }
        return null;
    }
}
