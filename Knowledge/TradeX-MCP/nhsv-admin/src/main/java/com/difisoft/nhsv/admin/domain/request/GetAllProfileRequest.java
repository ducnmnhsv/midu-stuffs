package com.difisoft.nhsv.admin.domain.request;


import lombok.Data;

@Data
public class GetAllProfileRequest {
    private String keyword;
    private String type;
    private Integer pageNumber = 0;
    private Integer pageSize = 20;
}
