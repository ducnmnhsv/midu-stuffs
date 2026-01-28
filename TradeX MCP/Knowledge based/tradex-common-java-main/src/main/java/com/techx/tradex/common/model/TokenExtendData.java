package com.techx.tradex.common.model;

import com.techx.tradex.common.model.kafka.BaseAfterLoginRequest;
import com.techx.tradex.common.model.requests.Token;
import lombok.Data;

@Data
public class TokenExtendData {
    protected Long[] sgIds;
    protected BaseAfterLoginRequest.ConnectionId conId;
    protected String sc;
    protected String su;
    protected Token.UserData ud;
}
