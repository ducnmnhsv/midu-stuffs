package com.techx.tradex.common.model.responses;

import com.techx.tradex.common.model.kafka.DefaultPartitionBody;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse implements DefaultPartitionBody {
    private String message;
}
