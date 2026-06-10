package com.techx.tradex.order.model.request;

import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.requests.DataRequest;
import com.techx.tradex.order.constants.Constants;
import lombok.Data;

import java.util.List;

@Data
public class StopOrderCancelMultiRequest extends DataRequest {
    private List<Long> idList;

    public void validate() {
        if (this.idList == null || this.idList.size() == 0) {
            throw new GeneralException(Constants.INVALID_PARAMETER);
        }
    }
}
