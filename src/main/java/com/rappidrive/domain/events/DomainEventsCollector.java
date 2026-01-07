package com.rappidrive.domain.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Collector that temporarily stores published domain events for the current thread.
 * Use case can drain collected events and persist them (e.g., to outbox) within the same transaction.
 */
public class DomainEventsCollector implements DomainEventHandler<DomainEvent> {

    private static final ThreadLocal<List<DomainEvent>> EVENTS = ThreadLocal.withInitial(ArrayList::new);
    private static final DomainEventsCollector INSTANCE = new DomainEventsCollector();

    private DomainEventsCollector() {}

    public static DomainEventsCollector instance() {
        return INSTANCE;
    }

    @Override
    public boolean canHandle(DomainEvent event) {
        return true; // collect all domain events
    }

    @Override
    public void handle(DomainEvent event) {
        EVENTS.get().add(event);
    }

    /**
     * Drain and clear collected events for current thread.
     */
    public List<DomainEvent> drain() {
        List<DomainEvent> list = new ArrayList<>(EVENTS.get());
        EVENTS.get().clear();
        return list;
    }

    /**
     * Clear events (testing convenience)
     */
    public void clear() {
        EVENTS.get().clear();
    }
}
