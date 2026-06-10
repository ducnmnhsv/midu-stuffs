package com.techx.tradex.ekycadmin.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class FptECExCallResponse {
    private String id;
    private Object refId;
    private String code;
    private String message;
    private Object result;
    private Response response;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        private String contractStatus;
        public String envelopeId;
        private List<Contact> contacts;
        private String contractFileName;
        private String contractFileContent;

        @Override
        public String toString() {
            try {
                return "Response{" +
                        "contractStatus='" + contractStatus + '\'' +
                        ", envelopeId='" + envelopeId + '\'' +
                        ", contacts=" + (CollectionUtils.isEmpty(contacts) ? contacts : contacts.stream().map(Contact::toString).collect(Collectors.joining("===="))) +
                        ", contractFileName='" + contractFileName + '\'' +
                        ", contractFileContent='" + MessageFormat.format("contractFileContent data status: {0}, (ignore log base64 file content)", StringUtils.isBlank(contractFileContent)) + '\'' +
                        '}';
            } catch (Exception e) {
                log.error("FptECExCallResponse.Response toString error: ", e);
                return StringUtils.EMPTY;
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Contact {
        private String contactId;
        private String signStatus;
        private String signFileName;
        private String signFileContent;

        @Override
        public String toString() {
            return "Contact{" +
                    "contactId='" + contactId + '\'' +
                    ", signStatus='" + signStatus + '\'' +
                    ", signFileName='" + signFileName + '\'' +
                    ", signFileContent='" + MessageFormat.format("signFileContent data status: {0}, (ignore log base64 file content)", StringUtils.isBlank(signFileContent)) + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "FptECExCallResponse{" +
                "id='" + id + '\'' +
                ", refId=" + refId +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", result=" + result +
                ", response=" + (Objects.isNull(response) ? null : response.toString()) +
                '}';
    }
}
