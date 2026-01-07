package com.rappidrive.application.ports.output;

import java.util.UUID;

public interface EventDispatcherPort {
    /**
     * Dispatches an event payload to external transport (e.g., Kafka, HTTP).
     * Implementations must handle serialization and transport-specific headers.
     * @param eventId unique event id
     * @param eventType event type
     * @param payload JSON payload
     * @throws Exception on failure
     */
    void dispatch(UUID eventId, String eventType, String payload) throws Exception;
}
