package com.rappidrive.application.concurrency;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ParallelExecutor using CompletableFuture with virtual threads.
 * Validates parallel execution and error handling.
 */
class ParallelExecutorTest {
    
    private ExecutorService executor;
    
    @BeforeEach
    void setUp() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }
    
    @AfterEach
    void tearDown() {
        executor.close();
    }
    
    @Test
    @Timeout(2)
    void executeAll_shouldRunAllTasksInParallel() {
        // Arrange: 3 tasks that each take 100ms
        List<Supplier<Integer>> tasks = List.of(
            () -> { sleep(100); return 1; },
            () -> { sleep(100); return 2; },
            () -> { sleep(100); return 3; }
        );
        
        // Act: Execute in parallel
        long start = System.currentTimeMillis();
        List<Integer> results = ParallelExecutor.executeAll(tasks, executor);
        long duration = System.currentTimeMillis() - start;
        
        // Assert: Should take ~100ms (parallel), not 300ms (sequential)
        assertThat(results).containsExactly(1, 2, 3);
        assertThat(duration).isLessThan(250); // Allow some overhead
    }
    
    @Test
    void executeAll_shouldMaintainTaskOrder() {
        // Arrange: Tasks complete in different order than submitted
        List<Supplier<String>> tasks = List.of(
            () -> { sleep(50); return "first"; },
            () -> { sleep(10); return "second"; },
            () -> { sleep(30); return "third"; }
        );
        
        // Act
        List<String> results = ParallelExecutor.executeAll(tasks, executor);
        
        // Assert: Results maintain submission order despite completion order
        assertThat(results).containsExactly("first", "second", "third");
    }
    
    @Test
    void executeAll_shouldPropagateExceptionFromFailedTask() {
        // Arrange: Task throws specific exception
        List<Supplier<Integer>> tasks = List.of(
            () -> { throw new IllegalStateException("Invalid state"); }
        );
        
        // Act & Assert: Should propagate exception
        assertThatThrownBy(() -> ParallelExecutor.executeAll(tasks, executor))
            .hasCauseInstanceOf(IllegalStateException.class)
            .hasStackTraceContaining("Invalid state");
    }
    
    @Test
    void executeAll_shouldHandleEmptyTaskList() {
        // Arrange
        List<Supplier<Integer>> tasks = List.of();
        
        // Act
        List<Integer> results = ParallelExecutor.executeAll(tasks, executor);
        
        // Assert
        assertThat(results).isEmpty();
    }
    
    @Test
    @Timeout(2)
    void executeRace_shouldReturnFirstSuccessfulResult() {
        // Arrange: Tasks with different delays
        List<Supplier<String>> tasks = List.of(
            () -> { sleep(300); return "slow"; },
            () -> { sleep(50); return "fast"; },
            () -> { sleep(150); return "medium"; }
        );
        
        // Act: Execute race
        long start = System.currentTimeMillis();
        String result = ParallelExecutor.executeRace(tasks, executor);
        long duration = System.currentTimeMillis() - start;
        
        // Assert: Should return fastest result
        assertThat(result).isEqualTo("fast");
        assertThat(duration).isLessThan(200); // Should finish around 50ms
    }
    
    @Test
    void mapParallel_shouldTransformAllItems() {
        // Arrange: Numbers to square
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        
        // Act: Map in parallel
        List<Integer> squares = ParallelExecutor.mapParallel(
            numbers, 
            n -> n * n,
            executor
        );
        
        // Assert
        assertThat(squares).containsExactly(1, 4, 9, 16, 25);
    }
    
    @Test
    @Timeout(2)
    void mapParallel_shouldExecuteInParallel() {
        // Arrange: Items that each take 100ms to process
        List<String> items = List.of("a", "b", "c");
        
        // Act: Transform in parallel
        long start = System.currentTimeMillis();
        List<String> results = ParallelExecutor.mapParallel(
            items,
            s -> {
                sleep(100);
                return s.toUpperCase();
            },
            executor
        );
        long duration = System.currentTimeMillis() - start;
        
        // Assert: Should take ~100ms (parallel), not 300ms
        assertThat(results).containsExactly("A", "B", "C");
        assertThat(duration).isLessThan(250);
    }
    
    @Test
    void mapParallel_shouldHandleTransformationFailure() {
        // Arrange: One transformation fails
        List<Integer> numbers = List.of(1, 2, 0, 4); // Division by zero
        
        // Act & Assert: Should propagate exception
        assertThatThrownBy(() -> 
            ParallelExecutor.mapParallel(numbers, n -> 10 / n, executor)
        )
            .hasCauseInstanceOf(ArithmeticException.class);
    }
    
    @Test
    void mapParallel_shouldHandleEmptyList() {
        // Arrange
        List<Integer> numbers = List.of();
        
        // Act
        List<Integer> results = ParallelExecutor.mapParallel(numbers, n -> n * 2, executor);
        
        // Assert
        assertThat(results).isEmpty();
    }
    
    @Test
    @Timeout(2)
    void executeAll_shouldHandleLargeNumberOfTasks() {
        // Arrange: 100 tasks (demonstrates virtual thread efficiency)
        List<Supplier<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int value = i;
            tasks.add(() -> {
                sleep(10);
                return value;
            });
        }
        
        // Act: Execute all in parallel
        long start = System.currentTimeMillis();
        List<Integer> results = ParallelExecutor.executeAll(tasks, executor);
        long duration = System.currentTimeMillis() - start;
        
        // Assert: Should complete quickly despite many tasks
        assertThat(results).hasSize(100);
        assertThat(duration).isLessThan(500); // With platform threads this would be much slower
    }
    
    /**
     * Helper to avoid checked exception in lambdas
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
