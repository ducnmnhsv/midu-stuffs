package com.techx.tradex.ekycadmin.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FptECSignResponse {

    private List<Item> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private String envelopeId;
        private String envelopeStatus;
        private String recipientStatus;
        private ContractInfo contractInfo;
        private WebView webView;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContractInfo {
        private String contractName;
        private String contractNo;
        private String createdDate;
        private String submittedFrom;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WebView {
        private String url;
        private String cookieName;
        private String cookieValue;
        private Integer expireIn;
        private String iframeUrl;
    }
}
