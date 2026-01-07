package com.rappidrive.infrastructure.messaging;

import com.rappidrive.application.ports.output.EventDispatcherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * No-op implementation of EventDispatcherPort for local development and testing.
 * In production, this would be replaced by a real implementation (Kafka, RabbitMQ, etc.)
 */
@Component
public class NoOpEventDispatcher implements EventDispatcherPort {
    
    private static final Logger logger = LoggerFactory.getLogger(NoOpEventDispatcher.class);
    
    @Override
    public void dispatch(UUID eventId, String eventType, String payload) throws Exception {
        logger.debug("NoOpEventDispatcher: Would dispatch eventId={}, type={}, payload={}", 
            eventId, eventType, payload);
        // No-op: does nothing in local/test environment
    }
}
