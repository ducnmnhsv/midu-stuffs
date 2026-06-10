package com.techx.tradex.ekycadmin.models.ttl;

import lombok.Data;


@Data
public class OperatorLoginReq {
    private String operatorID;
    private String password;
    private String channelID;
}

