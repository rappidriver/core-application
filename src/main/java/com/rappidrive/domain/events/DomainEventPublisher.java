package com.rappidrive.domain.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Domain Event Publisher using Observer pattern.
 * Thread-safe implementation for publishing domain events.
 */
public class DomainEventPublisher {
    
    private static final ThreadLocal<DomainEventPublisher> INSTANCE = ThreadLocal.withInitial(DomainEventPublisher::new);
    private final List<DomainEventHandler<?>> handlers = new CopyOnWriteArrayList<>();
    private boolean publishing = false;
    
    public static DomainEventPublisher instance() {
        return INSTANCE.get();
    }
    
    /**
     * Publishes a domain event to all registered handlers.
     * 
     * @param event the domain event to publish
     * @param <T> event type
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void publish(T event) {
        if (publishing) {
            return; // Prevent recursive publishing
        }
        
        try {
            publishing = true;
            handlers.stream()
                    .filter(handler -> handler.canHandle(event))
                    .forEach(handler -> ((DomainEventHandler<T>) handler).handle(event));
        } finally {
            publishing = false;
        }
    }
    
    /**
     * Registers a domain event handler.
     * 
     * @param handler the handler to register
     */
    public void register(DomainEventHandler<?> handler) {
        handlers.add(handler);
    }
    
    /**
     * Clears all registered handlers.
     * Useful for testing or cleanup.
     */
    public void clearHandlers() {
        handlers.clear();
    }
    
    /**
     * Resets the publisher state.
     * Clears handlers and resets publishing flag.
     */
    public void reset() {
        publishing = false;
        clearHandlers();
    }
}