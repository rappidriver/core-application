package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.TripRepositoryPort;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.enums.TripStatus;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.TripJpaEntity;
import com.rappidrive.infrastructure.persistence.mappers.TripMapper;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataTripRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaTripRepositoryAdapter implements TripRepositoryPort {
    
    private final SpringDataTripRepository jpaRepository;
    private final TripMapper mapper;
    private final com.rappidrive.application.ports.output.OutboxRepositoryPort outboxRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final Tracer tracer;
    
    @Override
    public Trip save(Trip trip) {
        TripJpaEntity entity;
        
        if (trip.getId() != null) {
            entity = jpaRepository.findById(trip.getId().getValue())
                .orElseGet(() -> mapper.toJpaEntity(trip));
            mapper.updateJpaEntity(entity, trip);
        } else {
            entity = mapper.toJpaEntity(trip);
        }
        
        try {
            TripJpaEntity saved = jpaRepository.save(entity);

            try {
                java.util.List<com.rappidrive.domain.events.DomainEvent> events = com.rappidrive.domain.events.DomainEventsCollector.instance().drain();
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                String traceId = currentTraceId();
                String spanId = currentSpanId();
                for (com.rappidrive.domain.events.DomainEvent event : events) {
                    java.util.UUID aggregateId = extractAggregateId(event, saved);
                    String payload = objectMapper.writeValueAsString(event);
                    com.rappidrive.domain.outbox.OutboxEvent outboxEvent = new com.rappidrive.domain.outbox.OutboxEvent(
                        java.util.UUID.randomUUID(),
                        aggregateId,
                        event.getClass().getSimpleName(),
                        payload,
                        "PENDING",
                        0,
                        now,
                        now,
                        traceId,
                        spanId
                    );
                    outboxRepository.save(outboxEvent);
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize domain event for outbox", e);
            }

            return mapper.toDomain(saved);
        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            throw new com.rappidrive.domain.exceptions.TripConcurrencyException(
                "Concurrent modification detected for trip: " + (trip.getId() != null ? trip.getId().getValue() : "unknown"),
                e
            );
        }
    }
    
    @Override
    public Optional<Trip> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Trip> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<Trip> findByDriver(UUID driverId) {
        return jpaRepository.findByDriverId(driverId).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<Trip> findByPassenger(UUID passengerId) {
        return jpaRepository.findByPassengerId(passengerId).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<Trip> findByStatus(TripStatus status, TenantId tenantId) {
        return jpaRepository.findByStatusAndTenantId(status, tenantId).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<Trip> findActiveTrips(TenantId tenantId) {
        return jpaRepository.findActiveTrips(tenantId).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    /**
     * Try to extract aggregate id from event using common conventions (tripId, aggregateId, getTripId, etc.).
     * Falls back to the saved Trip id when not found.
     */
    private java.util.UUID extractAggregateId(com.rappidrive.domain.events.DomainEvent event, TripJpaEntity saved) {
        try {
            // Common method names to search
            String[] methodNames = {"tripId", "getTripId", "aggregateId", "getAggregateId", "getId", "id"};
            for (String name : methodNames) {
                try {
                    java.lang.reflect.Method m = event.getClass().getMethod(name);
                    Object result = m.invoke(event);
                    if (result == null) continue;

                    // If it's a UUID directly
                    if (result instanceof java.util.UUID) return (java.util.UUID) result;

                    // If it's a value object with getValue() returning UUID
                    try {
                        java.lang.reflect.Method getValue = result.getClass().getMethod("getValue");
                        Object val = getValue.invoke(result);
                        if (val instanceof java.util.UUID) return (java.util.UUID) val;
                    } catch (NoSuchMethodException ignored) {}

                    // If it's a string that can be parsed as UUID
                    if (result instanceof String) {
                        try {
                            return java.util.UUID.fromString((String) result);
                        } catch (IllegalArgumentException ignored) {}
                    }
                } catch (NoSuchMethodException ignored) {
                    // try next
                }
            }
        } catch (Exception e) {
            // ignore and fallback
        }

        return saved.getId();
    }

    private String currentTraceId() {
        Span span = tracer != null ? tracer.currentSpan() : null;
        return span != null ? span.context().traceId() : null;
    }

    private String currentSpanId() {
        Span span = tracer != null ? tracer.currentSpan() : null;
        return span != null ? span.context().spanId() : null;
    }
}

