package com.difisoft.nhsv.admin.domain.request;

import com.difisoft.model.requests.DataRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubscriberInformationRequest extends DataRequest implements BaseRequest {
    private String accountNumber;
    private String subNumber;
    private String copyTradingStatus;
    private Integer pageNumber;
    private Integer pageSize;
    private Boolean sortAsc;

    public Boolean getSortAsc() {
        if (Objects.isNull(this.sortAsc)) {
            this.sortAsc = false;
        }
        return buildDefaultSortAsc(this.sortAsc);
    }

    public List<String> getAccountNumbers() {
        if (headers != null) {
            if (headers.getToken() != null)
                if (headers.getToken().getUserData() != null)
                    return headers.getToken().getUserData().getAccountNumbers();
        }
        return null;
    }
}
