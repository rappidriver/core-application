package com.rappidrive.infrastructure.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Monitors HikariCP connection pool metrics and logs warnings when pool is under stress.
 * Helps identify connection pool bottlenecks in high concurrency scenarios with virtual threads.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectionPoolMonitor {
    
    private final HikariDataSource dataSource;
    
    private static final double HIGH_UTILIZATION_THRESHOLD = 0.8; // 80%
    private static final int SLOW_ACQUISITION_THRESHOLD_MS = 500;
    
    /**
     * Logs pool metrics every 30 seconds during development/debug.
     * In production, these metrics should be exported to monitoring system (Prometheus, Datadog, etc.)
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void logPoolMetrics() {
        try {
            HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
            
            int activeConnections = pool.getActiveConnections();
            int idleConnections = pool.getIdleConnections();
            int totalConnections = pool.getTotalConnections();
            int waitingThreads = pool.getThreadsAwaitingConnection();
            int maxPoolSize = dataSource.getMaximumPoolSize();
            
            log.debug("HikariCP Pool Metrics - Active: {}, Idle: {}, Total: {}, Waiting: {}, Max: {}", 
                     activeConnections, idleConnections, totalConnections, waitingThreads, maxPoolSize);
            
            // Calculate pool utilization percentage
            double utilization = (double) activeConnections / maxPoolSize;
            
            // Warn if pool is running hot (>80% capacity)
            if (utilization > HIGH_UTILIZATION_THRESHOLD) {
                log.warn("⚠️  Connection pool at {:.1f}% capacity! Active: {}/{}, Waiting: {}. " +
                        "Consider increasing maximum-pool-size if this persists.",
                        utilization * 100, activeConnections, maxPoolSize, waitingThreads);
            }
            
            // Critical warning if threads are waiting for connections
            if (waitingThreads > 0) {
                log.warn("⚠️  {} thread(s) waiting for database connections! Pool exhausted.", 
                        waitingThreads);
            }
            
        } catch (Exception e) {
            log.error("Failed to retrieve HikariCP pool metrics", e);
        }
    }
    
    /**
     * Get current pool utilization as a percentage (0.0 to 1.0)
     */
    public double getPoolUtilization() {
        try {
            HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
            return (double) pool.getActiveConnections() / dataSource.getMaximumPoolSize();
        } catch (Exception e) {
            log.error("Failed to calculate pool utilization", e);
            return 0.0;
        }
    }
    
    /**
     * Get number of threads currently waiting for a connection
     */
    public int getWaitingThreads() {
        try {
            return dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();
        } catch (Exception e) {
            log.error("Failed to get waiting threads count", e);
            return 0;
        }
    }
}
