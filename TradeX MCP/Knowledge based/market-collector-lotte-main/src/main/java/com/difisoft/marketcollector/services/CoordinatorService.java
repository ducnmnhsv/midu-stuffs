package com.difisoft.marketcollector.services;

import org.springframework.stereotype.Service;

@Service
public class CoordinatorService extends com.difisoft.redis.CoordinatorService {
    public CoordinatorService(org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }
}
