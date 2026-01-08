package com.rappidrive.infrastructure.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ConnectionPoolMonitorTest {
    
    @Mock
    private HikariDataSource dataSource;
    
    @Mock
    private HikariPoolMXBean poolMXBean;
    
    private ConnectionPoolMonitor monitor;
    
    @BeforeEach
    void setUp() {
        lenient().when(dataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
        lenient().when(dataSource.getMaximumPoolSize()).thenReturn(50);
        
        monitor = new ConnectionPoolMonitor(dataSource);
    }
    
    @Test
    void shouldCalculatePoolUtilization() {
        // Given
        when(poolMXBean.getActiveConnections()).thenReturn(25);
        
        // When
        double utilization = monitor.getPoolUtilization();
        
        // Then
        assertThat(utilization).isEqualTo(0.5); // 25/50 = 50%
    }
    
    @Test
    void shouldReturnZeroUtilizationOnError() {
        // Given
        when(dataSource.getHikariPoolMXBean()).thenThrow(new RuntimeException("Pool not initialized"));
        
        // When
        double utilization = monitor.getPoolUtilization();
        
        // Then
        assertThat(utilization).isEqualTo(0.0);
    }
    
    @Test
    void shouldGetWaitingThreadsCount() {
        // Given
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(5);
        
        // When
        int waitingThreads = monitor.getWaitingThreads();
        
        // Then
        assertThat(waitingThreads).isEqualTo(5);
    }
    
    @Test
    void shouldReturnZeroWaitingThreadsOnError() {
        // Given
        when(dataSource.getHikariPoolMXBean()).thenThrow(new RuntimeException("Error"));
        
        // When
        int waitingThreads = monitor.getWaitingThreads();
        
        // Then
        assertThat(waitingThreads).isEqualTo(0);
    }
    
    @Test
    void shouldLogPoolMetricsWithoutError() {
        // Given
        when(poolMXBean.getActiveConnections()).thenReturn(10);
        when(poolMXBean.getIdleConnections()).thenReturn(5);
        when(poolMXBean.getTotalConnections()).thenReturn(15);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);
        
        // When & Then - should not throw exception
        monitor.logPoolMetrics();
        
        verify(poolMXBean).getActiveConnections();
        verify(poolMXBean).getIdleConnections();
        verify(poolMXBean).getTotalConnections();
    }
    
    @Test
    void shouldHandleExceptionDuringMetricsLogging() {
        // Given
        when(dataSource.getHikariPoolMXBean()).thenThrow(new RuntimeException("Metrics unavailable"));
        
        // When & Then - should not propagate exception
        monitor.logPoolMetrics();
    }
}
