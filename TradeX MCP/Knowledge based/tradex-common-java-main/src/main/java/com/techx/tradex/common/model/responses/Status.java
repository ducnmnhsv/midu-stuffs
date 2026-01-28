package com.techx.tradex.common.model.responses;

import com.techx.tradex.common.exceptions.FieldError;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.exceptions.SubErrorsException;
import com.techx.tradex.common.model.kafka.DefaultPartitionBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status implements DefaultPartitionBody {
    private String code;
    private List<String> messageParams;
    private List<Error> params = new ArrayList<>();

    public Status(String code, List<String> messageParams) {
        this.code = code;
        this.messageParams = messageParams;
    }

    public Status add(Error error) {
        this.params.add(error);
        return this;
    }

    public GeneralException create() {
        if (CollectionUtils.isEmpty(this.params)) {
            return new GeneralException(
                    this.code,
                    messageParams
            );
        }
        SubErrorsException err = new SubErrorsException(this.code, this.messageParams);
        err.getErrors().addAll(
                this.params.stream().map(error
                        -> new FieldError(error.getCode(), error.getParam(), error.getMessageParams())
                ).collect(Collectors.toList())
        );
        return err;
    }
}
