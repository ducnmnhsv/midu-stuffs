package com.techx.tradex.ekycadmin.models.lotte;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LotteEKycUpdateAccountRes extends LotteRes {

    @JsonProperty("data_list")
    private List<LotteEKycUpdateAccountResData> dataList;

    @Data
    public static class LotteEKycUpdateAccountResData {

        @JsonProperty("scrt_err_msg")
        private String scrtErrMsg;
    }
}
