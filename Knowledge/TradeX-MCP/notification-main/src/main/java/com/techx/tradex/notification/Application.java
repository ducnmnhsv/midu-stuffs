package com.techx.tradex.notification;

import com.difisoft.redis.RedisDao;
import com.techx.tradex.notification.configurations.AppConf;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.UUID;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableConfigurationProperties({AppConf.class})
@EnableScheduling
@EnableAsync
@EnableRetry
@Import({RedisDao.class})
public class Application {
    public static final String instanceId = UUID.randomUUID().toString();
    public static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = new SpringApplication(Application.class).run(args);
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("noti-");
        executor.initialize();
        return executor;
    }
}
