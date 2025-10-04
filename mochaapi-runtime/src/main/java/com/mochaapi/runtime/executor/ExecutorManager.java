package com.mochaapi.runtime.executor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

/**
 * Manages different types of executors for MochaAPI.
 * Provides virtual thread executor for I/O operations and CPU-bound executor for compute tasks.
 */
public class ExecutorManager {
    
    private final Executor virtualThreadExecutor;
    private final Executor cpuBoundExecutor;
    
    public ExecutorManager() {
        // Virtual thread executor for I/O operations
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        
        // CPU-bound executor using work-stealing pool
        this.cpuBoundExecutor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * Execute a task on virtual threads (for I/O operations).
     * 
     * @param task the task to execute
     * @return a CompletableFuture that completes with the result
     */
    public <T> CompletableFuture<T> executeVirtual(java.util.function.Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, virtualThreadExecutor);
    }
    
    /**
     * Execute a task on CPU-bound threads (for compute operations).
     * 
     * @param task the task to execute
     * @return a CompletableFuture that completes with the result
     */
    public <T> CompletableFuture<T> executeCpuBound(java.util.function.Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, cpuBoundExecutor);
    }
    
    /**
     * Execute a runnable task on virtual threads.
     * 
     * @param task the task to execute
     * @return a CompletableFuture that completes when the task is done
     */
    public CompletableFuture<Void> executeVirtual(Runnable task) {
        return CompletableFuture.runAsync(task, virtualThreadExecutor);
    }
    
    /**
     * Execute a runnable task on CPU-bound threads.
     * 
     * @param task the task to execute
     * @return a CompletableFuture that completes when the task is done
     */
    public CompletableFuture<Void> executeCpuBound(Runnable task) {
        return CompletableFuture.runAsync(task, cpuBoundExecutor);
    }
    
    /**
     * Shutdown all executors.
     */
    public void shutdown() {
        if (cpuBoundExecutor instanceof ForkJoinPool) {
            ((ForkJoinPool) cpuBoundExecutor).shutdown();
        }
    }
}
