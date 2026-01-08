package com.rappidrive.infrastructure.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for HikariCP connection pool.
 * Exposes pool metrics via Spring Boot Actuator health endpoint.
 * 
 * Accessible at: GET /actuator/health/connectionPool
 */
@Component("connectionPool")
@RequiredArgsConstructor
public class ConnectionPoolHealthIndicator implements HealthIndicator {
    
    private final HikariDataSource dataSource;
    
    private static final double CRITICAL_UTILIZATION = 0.95; // 95%
    private static final double WARNING_UTILIZATION = 0.80;  // 80%
    
    @Override
    public Health health() {
        try {
            HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
            
            int activeConnections = pool.getActiveConnections();
            int idleConnections = pool.getIdleConnections();
            int totalConnections = pool.getTotalConnections();
            int waitingThreads = pool.getThreadsAwaitingConnection();
            int maxPoolSize = dataSource.getMaximumPoolSize();
            int minIdle = dataSource.getMinimumIdle();
            
            double utilization = (double) activeConnections / maxPoolSize;
            
            Health.Builder healthBuilder = Health.up()
                .withDetail("active", activeConnections)
                .withDetail("idle", idleConnections)
                .withDetail("total", totalConnections)
                .withDetail("waiting", waitingThreads)
                .withDetail("min", minIdle)
                .withDetail("max", maxPoolSize)
                .withDetail("utilization", String.format("%.1f%%", utilization * 100));
            
            // Determine health status based on utilization and waiting threads
            if (waitingThreads > 0 || utilization >= CRITICAL_UTILIZATION) {
                return healthBuilder
                    .down()
                    .withDetail("status", "CRITICAL")
                    .withDetail("message", "Connection pool exhausted or near capacity")
                    .build();
            } else if (utilization >= WARNING_UTILIZATION) {
                return healthBuilder
                    .unknown()
                    .withDetail("status", "WARNING")
                    .withDetail("message", "Connection pool running hot")
                    .build();
            } else {
                return healthBuilder
                    .withDetail("status", "HEALTHY")
                    .build();
            }
            
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Failed to retrieve pool metrics")
                .withDetail("exception", e.getMessage())
                .build();
        }
    }
}
