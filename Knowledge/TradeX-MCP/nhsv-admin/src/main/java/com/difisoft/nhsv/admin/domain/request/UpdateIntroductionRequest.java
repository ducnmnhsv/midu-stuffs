package com.difisoft.nhsv.admin.domain.request;

import lombok.Data;

@Data
public class UpdateIntroductionRequest {
    private String login;
    private String introduction;
}
