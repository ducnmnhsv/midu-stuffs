package com.techx.tradex.common.model.kafka.request.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.common.constants.ResolutionType;
import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.utils.validator.CombineValidator;
import com.techx.tradex.common.utils.validator.StringValidator;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UDFHistoryRequest extends BaseAfterLoginRequest {
    private String symbol;
    private String resolution = ResolutionType.D.name();
    private long from;
    private long to;

    public void validate() {
        new CombineValidator()
                .add(new StringValidator("symbol", this.getSymbol()).empty())
                .add(new StringValidator("resolution", this.getResolution()).empty())
                .check();
    }
}
