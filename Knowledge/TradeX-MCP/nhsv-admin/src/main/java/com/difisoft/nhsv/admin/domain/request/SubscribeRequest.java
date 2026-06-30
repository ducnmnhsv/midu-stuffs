package com.difisoft.nhsv.admin.domain.request;

import com.difisoft.model.requests.DataRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@Data
public class SubscribeRequest extends DataRequest{
    @JsonProperty("username")
    private String username;
    private String accountNumber;
    private String subNumber;
    private Long marketLeaderId;
    private String orderSetType;
    private Double allocatedRatio;
    private String deviceUniqueId;

    public List<String> getAccountNumbers() {
        if (headers != null) {
            if(headers.getToken() != null)
                if(headers.getToken().getUserData() != null)
                    return headers.getToken().getUserData().getAccountNumbers();
        }
        return null;
    }

    public String getTokenUsername() {
        return this.headers != null && this.headers.getToken() != null && this.headers.getToken().getUserData() != null && this.headers.getToken().getUserData().getUsername() != null ? this.getHeaders().getToken().getUserData().getUsername().toLowerCase() : null;
    }
}
