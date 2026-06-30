package com.difisoft.nhsv.admin.domain.request;

import com.difisoft.model.requests.DataRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketLeaderProfileRequest extends DataRequest {
    private Long marketLeaderId;
    public String objToString() {
        return MessageFormat.format("{0}", marketLeaderId);
    }
}
