package com.techx.tradex.ekycadmin.models.request;

import com.techx.tradex.ekycadmin.models.dto.EContractField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FptECExCallRequest {
    private String id;
    private String refId;
    private String selector;
    private String lookup;
    private Object attrs;
    private String payload;
    private Body body;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Body {
        private List<Act> actList;
        private List<CustomData> customData;
        private InputData inputData;

        @Override
        public String toString() {
            return "Body{" +
                    "customData=" + (CollectionUtils.isEmpty(customData) ? customData : customData.get(0).toString()) +
                    ", inputData=" + inputData +
                    '}';
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CustomData {
        private String recipientId;
        private String email;
        private String telephoneNumber;
        private String contactId;
        private String personalName;
        private String location;
        private String stateOrProvince;
        private String country;
        private String personalID;
        private String passportID;
        private String type;
        private String photoIDCard;
        private String photoIDCardContentType;
        private String photoFrontSideIDCard;
        private String photoFrontSideIDCardContentType;
        private String photoBackSideIDCard;
        private String photoBackSideIDCardContentType;
        private String statusCode;
        private String resourceType;
        private String provideAddress;
        private String provideDate;
        private String commune;
        private String refId;

        @Override
        public String toString() {
            return "CustomData{" +
                    "recipientId='" + recipientId + '\'' +
                    ", email='" + email + '\'' +
                    ", telephoneNumber='" + telephoneNumber + '\'' +
                    ", contactId='" + contactId + '\'' +
                    ", personalName='" + personalName + '\'' +
                    ", location='" + location + '\'' +
                    ", stateOrProvince='" + stateOrProvince + '\'' +
                    ", country='" + country + '\'' +
                    ", personalID='" + personalID + '\'' +
                    ", passportID='" + passportID + '\'' +
                    ", type='" + type + '\'' +
                    ", photoIDCard='" + photoIDCard + '\'' +
                    ", photoIDCardContentType='" + photoIDCardContentType + '\'' +
                    ", photoFrontSideIDCardContentType='" + photoFrontSideIDCardContentType + '\'' +
                    ", photoBackSideIDCardContentType='" + photoBackSideIDCardContentType + '\'' +
                    ", statusCode='" + statusCode + '\'' +
                    ", resourceType='" + resourceType + '\'' +
                    ", provideAddress='" + provideAddress + '\'' +
                    ", provideDate='" + provideDate + '\'' +
                    ", commune='" + commune + '\'' +
                    ", refId='" + refId + '\'' +
                    '}';
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InputData {
        private String templateId;
        private String alias;
        private String syncType;
        private List<List<EContractField>> datas;

        @Override
        public String toString() {
            return "InputData{" +
                    "templateId='" + templateId + '\'' +
                    ", alias='" + alias + '\'' +
                    ", syncType='" + syncType + '\'' +
                    ", datas=" + datas +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "FptECExCallRequest{" +
                "id='" + id + '\'' +
                ", refId='" + refId + '\'' +
                ", selector='" + selector + '\'' +
                ", lookup='" + lookup + '\'' +
                ", attrs=" + attrs +
                ", payload='" + payload + '\'' +
                ", body=" + (Objects.isNull(body) ? null : body.toString()) +
                '}';
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Act {
        private String refId;
        private String envelopeId;
    }
}
