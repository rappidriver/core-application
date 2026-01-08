package com.rappidrive.infrastructure.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ConnectionPoolHealthIndicatorTest {
    
    @Mock
    private HikariDataSource dataSource;
    
    @Mock
    private HikariPoolMXBean poolMXBean;
    
    private ConnectionPoolHealthIndicator healthIndicator;
    
    @BeforeEach
    void setUp() {
        lenient().when(dataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
        lenient().when(dataSource.getMaximumPoolSize()).thenReturn(50);
        lenient().when(dataSource.getMinimumIdle()).thenReturn(10);
        
        healthIndicator = new ConnectionPoolHealthIndicator(dataSource);
    }
    
    @Test
    void shouldReportHealthyWhenUtilizationIsLow() {
        // Given - 20% utilization (10 active / 50 max)
        when(poolMXBean.getActiveConnections()).thenReturn(10);
        when(poolMXBean.getIdleConnections()).thenReturn(5);
        when(poolMXBean.getTotalConnections()).thenReturn(15);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);
        
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
            .containsEntry("active", 10)
            .containsEntry("idle", 5)
            .containsEntry("total", 15)
            .containsEntry("waiting", 0)
            .containsEntry("max", 50)
            .containsEntry("min", 10)
            .containsEntry("status", "HEALTHY");
    }
    
    @Test
    void shouldReportWarningWhenUtilizationIsHigh() {
        // Given - 85% utilization (42 active / 50 max)
        when(poolMXBean.getActiveConnections()).thenReturn(42);
        when(poolMXBean.getIdleConnections()).thenReturn(3);
        when(poolMXBean.getTotalConnections()).thenReturn(45);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);
        
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(health.getDetails())
            .containsEntry("status", "WARNING")
            .containsEntry("message", "Connection pool running hot");
    }
    
    @Test
    void shouldReportDownWhenUtilizationIsCritical() {
        // Given - 96% utilization (48 active / 50 max)
        when(poolMXBean.getActiveConnections()).thenReturn(48);
        when(poolMXBean.getIdleConnections()).thenReturn(0);
        when(poolMXBean.getTotalConnections()).thenReturn(48);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);
        
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
            .containsEntry("status", "CRITICAL")
            .containsEntry("message", "Connection pool exhausted or near capacity");
    }
    
    @Test
    void shouldReportDownWhenThreadsAreWaiting() {
        // Given - threads waiting for connections
        when(poolMXBean.getActiveConnections()).thenReturn(50);
        when(poolMXBean.getIdleConnections()).thenReturn(0);
        when(poolMXBean.getTotalConnections()).thenReturn(50);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(10);
        
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
            .containsEntry("waiting", 10)
            .containsEntry("status", "CRITICAL");
    }
    
    @Test
    void shouldReportDownOnException() {
        // Given
        when(dataSource.getHikariPoolMXBean()).thenThrow(new RuntimeException("Pool error"));
        
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
            .containsEntry("error", "Failed to retrieve pool metrics")
            .containsKey("exception");
    }
}
