package com.difisoft.nhsv.admin.domain.request;

import com.difisoft.model.requests.DataRequest;

import lombok.Data;

@Data
public class AccountInfoRequest extends DataRequest {
    private String accountNumber;
    private String subNumber;
}
