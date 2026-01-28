package com.techx.tradex.common.model.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Configuration {
    @JsonIgnore
    MethodEnum getMethod();
}
