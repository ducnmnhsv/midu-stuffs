package com.techx.tradex.ekycadmin.config;

import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import tech.jhipster.async.ExceptionHandlingAsyncTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfiguration implements AsyncConfigurer {

    private final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

    private final TaskExecutionProperties taskExecutionProperties;
    private final AppConf appConf;

    public AsyncConfiguration(TaskExecutionProperties taskExecutionProperties, AppConf appConf) {
        this.taskExecutionProperties = taskExecutionProperties;
        this.appConf = appConf;
    }

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        log.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(taskExecutionProperties.getPool().getCoreSize());
        executor.setMaxPoolSize(taskExecutionProperties.getPool().getMaxSize());
        executor.setQueueCapacity(taskExecutionProperties.getPool().getQueueCapacity());
        executor.setThreadNamePrefix(taskExecutionProperties.getThreadNamePrefix());
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Bean(name = "threadPoolTaskExecutor")
    @Primary
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(appConf.getThreadPool().getCorePoolSize());
        executor.setMaxPoolSize(appConf.getThreadPool().getMaxPoolSize());
        executor.setQueueCapacity(appConf.getThreadPool().getQueueCapacity());
        executor.setThreadNamePrefix(appConf.getThreadPool().getThreadNamePrefixSet());
        executor.setKeepAliveSeconds(appConf.getThreadPool().getKeepAliveSeconds());
        executor.setAwaitTerminationSeconds(appConf.getThreadPool().getAwaitTerminationSeconds());
        executor.setWaitForTasksToCompleteOnShutdown(appConf.getThreadPool().getWaitForTasksToCompleteOnShutdown());
        return executor;
    }

    @Bean(name = "threadPoolTaskScheduler")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(appConf.getThreadPool().getSchedulerPoolSize());
        scheduler.setThreadNamePrefix(appConf.getThreadPool().getSchedulerThreadNamePrefixSet());
        scheduler.setAwaitTerminationSeconds(appConf.getThreadPool().getSchedulerAwaitTerminationSeconds());
        scheduler.setWaitForTasksToCompleteOnShutdown(appConf.getThreadPool().getSchedulerWaitForTasksToCompleteOnShutdown());
        return scheduler;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
