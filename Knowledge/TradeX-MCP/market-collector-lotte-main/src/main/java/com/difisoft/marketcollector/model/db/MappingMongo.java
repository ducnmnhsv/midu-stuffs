package com.difisoft.marketcollector.model.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public interface MappingMongo {
    Logger log = LoggerFactory.getLogger(MappingMongo.class);

    String getId();

    default Map<String, Object> toMapUpdate() {
        Map<String, Object> map = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(this));
            } catch (Exception e) {
                log.info("error while convert {} to map: {0}", this.getClass().getSimpleName(), e);
            }
        }
        return map;
    }
}
