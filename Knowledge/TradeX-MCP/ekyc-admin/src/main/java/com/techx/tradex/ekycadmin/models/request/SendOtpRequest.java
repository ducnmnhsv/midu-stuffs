package com.techx.tradex.ekycadmin.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techx.tradex.common.model.requests.DataRequest;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.EnumValidator;
import com.techx.tradex.common.utils.validator.StringValidator;
import com.techx.tradex.ekycadmin.models.enums.OtpIdType;
import com.techx.tradex.ekycadmin.models.enums.OtpTxType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendOtpRequest extends DataRequest {

    private String id;
    private String idType;
    private String txType;

    public void validate() {
        new CombineValidator()
            .add(new StringValidator("id", this.id).empty())
            .add(new EnumValidator("idType", this.idType, OtpIdType.class).validate())
            .add(new EnumValidator("txType", this.txType, OtpTxType.class).validate())
            .check();
    }
}
