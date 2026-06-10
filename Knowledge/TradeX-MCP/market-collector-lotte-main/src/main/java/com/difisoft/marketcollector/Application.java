package com.difisoft.marketcollector;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.SymbolInfoRepository;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.redis.RedisDao;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({AppConf.class})
@EnableMongoRepositories(basePackageClasses = SymbolInfoRepository.class)
@EnableScheduling
@EnableAsync
@Import({RedisDao.class, MarketRedisDao.class})
public class Application {
    public static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = new SpringApplication(Application.class).run(args);
    }
}
