package com.rappidrive.domain.events;

/**
 * Handler interface for domain events.
 * Implements the Observer pattern for handling domain events.
 * 
 * @param <T> the type of domain event this handler processes
 */
public interface DomainEventHandler<T extends DomainEvent> {
    
    /**
     * Handles the domain event.
     * 
     * @param event the domain event to handle
     */
    void handle(T event);
    
    /**
     * Checks if this handler can process the given event.
     * 
     * @param event the event to check
     * @return true if this handler can process the event
     */
    boolean canHandle(DomainEvent event);
}