package com.techx.tradex.ekycadmin.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FptECEnvelopesRecipientResponse {

    private String envId;
    private String envStatus;
    private String recipientStatus;
    private String owner;
    private RecipientContactInfo recipientContactInfo;
    private List<HeaderField> headerFields;
    private Date createdDate;
    private Integer custId;
    private String orgIn;
    private WebView webView;

    @Data
    public static class Contact {
        private String id;
        private String name;
        private String email;
        private String phone;
    }

    @Data
    public static class HeaderField {
        private String id;
        private String name;
        private String type;
        private String value;
        private Object formula;
        private Object isList;
        private Object listValue;
        private Object assign;
        private Object variables;
    }

    @Data
    public static class RecipientContactInfo {
        private String id;
        private String partyId;
        private Contact contact;
        private String role;
        private String status;
        private Object fromDate;
        private Object dueDate;
        private Object processDate;
        private Object signDate;
        private Object reason;
        private String username;
        private List<Object> recipientsHistory;
        private Object message;
        private List<String> signTypes;
    }

    @Data
    public static class WebView {
        private String url;
        private String cookieName;
        private String cookieValue;
        private Integer expireTime;
        private String username;
        private String password;
        private String iframeUrl;
    }

}
