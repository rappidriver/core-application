package com.rappidrive.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configuration for parallel task execution using virtual threads.
 * Provides ExecutorService bean for ParallelExecutor and other concurrent operations.
 */
@Configuration
public class ParallelExecutorConfiguration {
    
    /**
     * Creates an ExecutorService that uses virtual threads for all tasks.
     * Virtual threads are lightweight and scale to millions without significant overhead.
     * 
     * <p>Used by ParallelExecutor for parallel operations like driver search.</p>
     * 
     * @return ExecutorService using virtual threads
     */
    @Bean(name = "virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
