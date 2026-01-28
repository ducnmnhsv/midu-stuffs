package com.techx.tradex.common.model.notification;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.model.kafka.DefaultPartitionBody;
import lombok.Data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
public class NotificationMessage implements DefaultPartitionBody {
    private MethodEnum method;
    private Map<String, Object> template;
    private String locale;
    private String configuration;
    private String domain;
    private String type;
    private String url;

    public void add(String templateKey, Object data) {
        if (template == null) {
            template = new HashMap();
        }
        this.template.put(templateKey, data);
    }

    public void setConfiguration(Configuration configurationData, ObjectMapper objectMapper) throws JsonProcessingException {
        this.configuration = objectMapper.writeValueAsString(configurationData);
        this.method = configurationData.getMethod();
    }

    public <T extends Configuration> T getConfiguration(ObjectMapper objectMapper, Class<T> clazz) throws IOException {
        return objectMapper.readValue(this.configuration, clazz);
    }
}
