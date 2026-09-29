package com.sims.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * AOP配置类
 * 启用AspectJ自动代理和异步处理
 */
@Configuration
@EnableAspectJAutoProxy
@EnableAsync
public class AopConfig {
    
    /**
     * 配置异步任务执行器
     * 用于AOP统计等异步操作
     */
    @Bean("statisticsTaskExecutor")
    public Executor statisticsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("stats-async-");
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }
}