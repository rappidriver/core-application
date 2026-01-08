package com.rappidrive.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify Virtual Threads configuration is present.
 */
@SpringBootTest
@DisplayName("Virtual Threads Configuration Tests")
class VirtualThreadsConfigTest {
    
    @Autowired
    private ApplicationContext context;
    
    @Test
    @DisplayName("AsyncConfiguration should be loaded")
    void asyncConfigurationShouldBeLoaded() {
        assertNotNull(context.getBean(AsyncConfiguration.class), 
            "AsyncConfiguration should be loaded");
    }
    
    @Test
    @DisplayName("WebConfiguration should be loaded")
    void webConfigurationShouldBeLoaded() {
        assertNotNull(context.getBean(WebConfiguration.class), 
            "WebConfiguration should be loaded");
    }
    
    @Test
    @DisplayName("Virtual thread executor works correctly")
    void virtualThreadExecutorWorks() {
        Executor executor = Executors.newVirtualThreadPerTaskExecutor();
        
        CompletableFuture<Boolean> result = CompletableFuture.supplyAsync(() -> {
            return Thread.currentThread().isVirtual();
        }, executor);
        
        assertTrue(result.join(), "Virtual thread executor should create virtual threads");
    }
}
