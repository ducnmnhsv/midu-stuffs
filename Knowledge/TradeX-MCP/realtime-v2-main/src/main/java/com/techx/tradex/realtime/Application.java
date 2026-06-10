package com.techx.tradex.realtime;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.SymbolInfoRepository;
import com.difisoft.redis.RedisDao;
import com.techx.tradex.realtime.configurations.AppConf;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.UUID;

@SpringBootApplication
@EnableConfigurationProperties({AppConf.class})
@EnableScheduling
@EnableAsync
@EnableMongoRepositories(basePackageClasses = SymbolInfoRepository.class)
@Import({RedisDao.class, MarketRedisDao.class})
@ComponentScan(basePackages = {"com.difisoft.market", "com.techx.tradex.realtime"})
public class Application {
    public static final String instanceId = UUID.randomUUID().toString();

    public static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = new SpringApplication(Application.class).run(args);
    }
}
