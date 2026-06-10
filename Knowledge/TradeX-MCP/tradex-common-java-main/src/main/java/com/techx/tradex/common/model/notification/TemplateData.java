package com.techx.tradex.common.model.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface TemplateData {
    @JsonIgnore
    String getTemplate();
}
