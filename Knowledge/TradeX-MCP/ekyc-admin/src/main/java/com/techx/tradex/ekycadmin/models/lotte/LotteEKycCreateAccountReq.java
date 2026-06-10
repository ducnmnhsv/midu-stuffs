package com.techx.tradex.ekycadmin.models.lotte;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.ekycadmin.models.enums.LotteLangCode;
import lombok.Data;
import org.apache.commons.lang3.EnumUtils;

@Data
public class LotteEKycCreateAccountReq extends LotteReq {

    @JsonProperty("grp_tp")
    private String grpTp;
    private String mobile;
    private String email;
    @JsonProperty("cli_mac_addr")
    private String cliMacAddr;

    public LotteEKycCreateAccountReq update(String phoneNo, String email, String grpTp, String langCode, String deviceUniqueId) {
        this.setMobile(phoneNo);
        this.setEmail(email);
        if (grpTp.equals("idv")) {
            this.setGrpTp("1");
        } else if (grpTp.equals("org")) {
            this.setGrpTp("2");
        } else {
            throw new GeneralException("INVALID_LOTTE_TYPE_VALUE");
        }
        this.setLangCode(EnumUtils.isValidEnum(LotteLangCode.class, langCode) ? LotteLangCode.valueOf(langCode).getCode() : LotteLangCode.vi.getCode());
        this.setCliMacAddr(deviceUniqueId);
        return this;
    }
}
