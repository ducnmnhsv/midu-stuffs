package com.difisoft.nhsv.admin.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.mongodb.MongoMetricsCommandListener;
import io.micrometer.core.instrument.binder.mongodb.MongoMetricsConnectionPoolListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoUri));
    }

    @Bean
    public MongoClientFactoryBean mongoClientFactoryBean(MongoProperties properties, MeterRegistry meterRegistry) {
        MongoClientFactoryBean mongoClientFactoryBean = new MongoClientFactoryBean();

        mongoClientFactoryBean.setConnectionString(new ConnectionString(properties.getUri()));

        MongoClientSettings settings = MongoClientSettings.builder()
                .addCommandListener(new MongoMetricsCommandListener(meterRegistry))
                .applyToConnectionPoolSettings(builder ->
                        builder.addConnectionPoolListener(new MongoMetricsConnectionPoolListener(meterRegistry)))
                .build();
        mongoClientFactoryBean.setMongoClientSettings(settings);

        return mongoClientFactoryBean;
    }
}
