package com.techx.tradex.ekycadmin.models.ttl;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperatorLoginRes extends TTLRes {
    private String tokenID;
    private String operatorID;
    private String sessionBO;
    private String sessionFO;
    private String channelID;
    private List<String> listService;
}

