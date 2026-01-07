package com.rappidrive.domain.events;

import java.time.LocalDateTime;

/**
 * Base interface for all domain events.
 * Events represent something that has happened in the domain.
 */
public interface DomainEvent {
    
    /**
     * When the event occurred.
     */
    LocalDateTime occurredOn();
    
    /**
     * Unique identifier for this event instance.
     */
    String eventId();
    
    /**
     * Version of the event schema for evolution compatibility.
     */
    default int version() {
        return 1;
    }
}