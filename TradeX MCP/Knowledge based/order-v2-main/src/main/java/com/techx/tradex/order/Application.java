package com.techx.tradex.order;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.redis.RedisDao;
import com.techx.tradex.order.configurations.AppConf;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.UUID;

@SpringBootApplication
@EnableConfigurationProperties({AppConf.class})
@EnableScheduling
@EnableAsync
@Import({RedisDao.class, MarketRedisDao.class})
public class Application {
    public static final String instanceId = UUID.randomUUID().toString();

    public static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = new SpringApplication(Application.class).run(args);
    }
}
