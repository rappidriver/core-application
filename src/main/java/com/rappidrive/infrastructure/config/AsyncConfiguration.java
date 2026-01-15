package com.rappidrive.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Async configuration using Virtual Threads.
 * All @Async annotated methods will run on virtual threads.
 * 
 * @see <a href="https://openjdk.org/jeps/444">JEP 444: Virtual Threads</a>
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {
    
    private static final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);
    
    @Override
    public Executor getAsyncExecutor() {
        // Use virtual threads for @Async methods
        // Virtual threads are lightweight and can handle millions of concurrent tasks
        return Executors.newVirtualThreadPerTaskExecutor();
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("Async execution error in method: {}", method.getName(), ex);
        };
    }
}
