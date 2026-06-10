package com.techx.tradex.ekycadmin.models.lotte;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class LotteEKycCreateAccountRes extends LotteRes {

    @JsonProperty("data_list")
    private List<LotteEKycCreateAccountResData> dataList;

    @Data
    public static class LotteEKycCreateAccountResData {

        @JsonProperty("os_seq_no")
        private String osSeqNo;
    }
}
