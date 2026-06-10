package com.techx.tradex.common.model;

import com.techx.tradex.common.model.requests.Token;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccessToken extends TokenExtendData {
    private String dm;
    private Long uId;
    private Long cId;
    private Long suId;
    private Long lm;
    private Long rId;
    private String sc;

    public Token to() {
        Token token = new Token();
        token.setDomain(this.dm);
        token.setUserId(this.uId != null ? this.uId.toString() : "");
        token.setClientId(this.cId);
        token.setServiceUserId(this.suId);
        token.setRefreshTokenId(this.rId);
        token.setServiceCode(this.sc);
        token.setConnectionId(this.conId);
        token.setLoginMethod(this.lm);
        token.setScopeGroupIds(this.sgIds);
        token.setUserData(this.ud);
        token.setServiceUsername(this.su);
        return token;
    }
}
