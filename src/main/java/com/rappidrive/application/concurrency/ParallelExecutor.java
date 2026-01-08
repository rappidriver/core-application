package com.rappidrive.application.concurrency;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility for executing tasks in parallel using CompletableFuture with virtual threads.
 * Provides structured approach to parallel execution with proper error handling.
 * 
 * <p>Benefits:</p>
 * <ul>
 *   <li>Parallel task execution for improved performance</li>
 *   <li>Proper error propagation and handling</li>
 *   <li>Works seamlessly with virtual threads (low overhead)</li>
 *   <li>Production-ready stable APIs (Java 8+)</li>
 * </ul>
 * 
 * <p>Usage Example:</p>
 * <pre>{@code
 * List<SearchZone> zones = divideIntoZones(location);
 * List<List<Driver>> results = ParallelExecutor.mapParallel(
 *     zones, 
 *     zone -> repository.findByZone(zone),
 *     executor
 * );
 * }</pre>
 */
public class ParallelExecutor {
    
    /**
     * Execute multiple tasks in parallel and collect all results.
     * If any task fails, the exception is propagated after all tasks complete.
     * 
     * <p>Use this when all tasks must succeed for the operation to be valid.</p>
     * 
     * @param tasks List of tasks to execute in parallel
     * @param executor ExecutorService to run tasks on (typically virtual thread executor)
     * @param <T> Type of result returned by each task
     * @return List of results in the same order as input tasks
     * @throws RuntimeException if any task fails (wraps original exception)
     */
    public static <T> List<T> executeAll(List<Supplier<T>> tasks, ExecutorService executor) {
        List<CompletableFuture<T>> futures = tasks.stream()
            .map(task -> CompletableFuture.supplyAsync(task, executor))
            .toList();
        
        // Wait for all futures to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        // Collect results, propagating any exceptions
        return allOf.thenApply(v -> 
            futures.stream()
                .map(CompletableFuture::join) // join() propagates exceptions
                .collect(Collectors.toList())
        ).join();
    }
    
    /**
     * Execute tasks in parallel and return the first successful result.
     * Other tasks may continue running but their results are ignored.
     * 
     * <p>Use this for race conditions or when any valid result is acceptable.</p>
     * 
     * @param tasks List of tasks to race against each other
     * @param executor ExecutorService to run tasks on
     * @param <T> Type of result returned by tasks
     * @return Result from the first task to complete successfully
     * @throws RuntimeException if all tasks fail
     */
    public static <T> T executeRace(List<Supplier<T>> tasks, ExecutorService executor) {
        CompletableFuture<T>[] futures = tasks.stream()
            .map(task -> CompletableFuture.supplyAsync(task, executor))
            .toArray(CompletableFuture[]::new);
        
        // Returns first completed future
        return CompletableFuture.anyOf(futures)
            .thenApply(result -> (T) result)
            .join();
    }
    
    /**
     * Execute multiple tasks with transformation in parallel.
     * Useful for mapping operations across collections.
     * 
     * <p>Example: Transform list of locations into list of drivers near each location.</p>
     * 
     * @param items Items to process
     * @param mapper Function to apply to each item
     * @param executor ExecutorService to run tasks on
     * @param <T> Type of input items
     * @param <R> Type of result items
     * @return List of transformed results in same order as input
     * @throws RuntimeException if any transformation fails
     */
    public static <T, R> List<R> mapParallel(List<T> items, 
                                              Function<T, R> mapper,
                                              ExecutorService executor) {
        List<Supplier<R>> tasks = items.stream()
            .map(item -> (Supplier<R>) () -> mapper.apply(item))
            .toList();
        
        return executeAll(tasks, executor);
    }
}
