package com.techx.tradex.notification.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RetryTemplate retryTemplate(AppConf appConf) {
        RetryTemplate retryTemplate = new RetryTemplate();
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(appConf.getRetry().getMaxDelay());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(appConf.getRetry().getMaxAttempts());
        retryTemplate.setRetryPolicy(policy);

        return retryTemplate;
    }
}
