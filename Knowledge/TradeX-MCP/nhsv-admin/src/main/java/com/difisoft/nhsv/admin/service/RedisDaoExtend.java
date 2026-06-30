package com.difisoft.nhsv.admin.service;

import com.difisoft.redis.RedisDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Primary
public class RedisDaoExtend extends RedisDao {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisDaoExtend(
        RedisTemplate<String, String> redisTemplate
        , ObjectMapper objectMapper
    ) {
        super(redisTemplate, objectMapper);
        this.redisTemplate = redisTemplate;
    }

    public List<String> keys(String pattern) {
        Set<String> rs = redisTemplate.keys(pattern);
        return CollectionUtils.isEmpty(rs) ? new ArrayList<>() : new ArrayList<>(rs);
    }

    public boolean isExists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


}
