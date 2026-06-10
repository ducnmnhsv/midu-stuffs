package com.techx.tradex.ekycadmin.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtTraDexDTO {
    private String dm;
    private Integer cId;
    private List<Integer> sgIds;
    private Integer lm;
    private Integer rId;
    private Ud ud;
    private List<Object> rls;
    private String gt;
    private String sId;
    private Integer iat;
    private Integer exp;

    @Data
    public static class Ud {
        public String username;
        public String identifierNumber;
        public String deptCode;
        public String branchCode;
        public String mngDeptCode;
        public String agencyNumber;
        public String userType;
        public String name;
        public List<String> accountNumbers;
        public String userLevel;
        public String mfaData;
    }
}
