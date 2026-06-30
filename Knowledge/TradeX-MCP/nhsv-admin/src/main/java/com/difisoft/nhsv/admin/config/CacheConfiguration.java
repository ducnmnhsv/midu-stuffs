//package com.difisoft.nhsv.admin.config;
//
//import com.difisoft.nhsv.admin.constant.Constants;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.MapperFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.databind.json.JsonMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
//import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
//import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
//import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
//import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
//import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
//import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
//import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//
//import java.time.*;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableCaching
//@ImportAutoConfiguration(classes = {
//    CacheAutoConfiguration.class,
//    RedisAutoConfiguration.class
//})
//public class CacheConfiguration {
//    @Value("${spring.redis.host}")
//    private String redisHost;
//
//    @Value("${spring.redis.port}")
//    private int redisPort;
//
//    @Bean
//    public LettuceConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
//        return new LettuceConnectionFactory(configuration);
//    }
//
//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
//        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
//        cacheConfigurations.put(Constants.CacheNames.EXPIRED_IN1_DAY, cacheConfigTtl(Duration.ofDays(1), Boolean.TRUE));
//        cacheConfigurations.put(Constants.CacheNames.EXPIRED_IN15_MINUTES, cacheConfigTtl(Duration.ofMinutes(15), Boolean.TRUE));
//        cacheConfigurations.put(Constants.CacheNames.EXPIRED_IN_JOB_CLEAR, cacheConfigTtl(null, Boolean.FALSE));
//        return RedisCacheManager.RedisCacheManagerBuilder
//            .fromConnectionFactory(redisConnectionFactory)
//            .withInitialCacheConfigurations(cacheConfigurations)
//            .build();
//    }
//
//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
//        return builder -> builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//    }
//
//    private RedisCacheConfiguration cacheConfigTtl(Duration duration, boolean isSetTtl) {
//
//        RedisCacheConfiguration configuration = RedisCacheConfiguration
//            .defaultCacheConfig()
//            .serializeValuesWith(
//                RedisSerializationContext
//                    .SerializationPair
//                    .fromSerializer(
//                        new GenericJackson2JsonRedisSerializer(objectMapper())
//                    )
//            );
//        if (isSetTtl) {
//            configuration.entryTtl(duration);
//        }
//        return configuration;
//    }
//
//    @Bean("cacheObjectMapper")
//    public ObjectMapper objectMapper() {
//        return JsonMapper.builder()
//            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
//            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
//            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
//            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
//            .addModule(
//                new JavaTimeModule()
//                    .addSerializer(ZonedDateTime.class, ZonedDateTimeSerializer.INSTANCE)
//                    .addDeserializer(ZonedDateTime.class, InstantDeserializer.ZONED_DATE_TIME)
//            )
//            .addModule(
//                new JavaTimeModule()
//                    .addSerializer(LocalDateTime.class, LocalDateTimeSerializer.INSTANCE)
//                    .addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE)
//            )
//            .addModule(
//                new JavaTimeModule()
//                    .addSerializer(LocalDate.class, LocalDateSerializer.INSTANCE)
//                    .addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE)
//            )
//            .addModule(
//                new JavaTimeModule()
//                    .addSerializer(LocalTime.class, LocalTimeSerializer.INSTANCE)
//                    .addDeserializer(LocalTime.class, LocalTimeDeserializer.INSTANCE)
//            )
//            .findAndAddModules()
////            .activateDefaultTyping(
////                new ObjectMapper().getPolymorphicTypeValidator()
////                , ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY
////            )
//            .build();
//    }
//}
