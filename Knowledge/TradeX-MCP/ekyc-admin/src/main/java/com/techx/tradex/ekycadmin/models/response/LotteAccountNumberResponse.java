package com.techx.tradex.ekycadmin.models.response;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LotteAccountNumberResponse implements Serializable {

    private String error_code;
    private String error_desc;
    private Boolean success;
    private String total_record;
    private List<DataList> data_list;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataList {

        private String acnt_no;
        private String cntr_no;
    }
}
