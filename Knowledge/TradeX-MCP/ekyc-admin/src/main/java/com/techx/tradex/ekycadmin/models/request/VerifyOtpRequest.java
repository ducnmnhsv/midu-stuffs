package com.techx.tradex.ekycadmin.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.StringValidator;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyOtpRequest {

    private String otpValue;
    private String otpId;

    public void validate() {
        new CombineValidator()
            .add(new StringValidator("otpId", this.otpValue).empty())
            .add(new StringValidator("otpValue", this.otpValue).empty())
            .check();
    }
}
