package com.techx.tradex.common.kafka;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.techx.tradex.common.model.kafka.Message;
import lombok.Data;
import lombok.ToString;

@JsonDeserialize(using = RequestHandlerMessageDeserializer.class)
@Data
@ToString(callSuper = true)
public class RequestHandlerMessage<T> extends Message<T> {
}
