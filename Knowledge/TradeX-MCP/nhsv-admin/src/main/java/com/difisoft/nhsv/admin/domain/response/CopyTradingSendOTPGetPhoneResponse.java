package com.difisoft.nhsv.admin.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CopyTradingSendOTPGetPhoneResponse {
    @JsonProperty("data_list")
    private List<Phone> dataList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Phone {
        private String phone;
    }
}
