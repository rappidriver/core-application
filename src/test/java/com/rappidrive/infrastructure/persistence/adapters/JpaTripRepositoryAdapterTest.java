package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.exceptions.TripConcurrencyException;
import com.rappidrive.infrastructure.persistence.entities.TripJpaEntity;
import com.rappidrive.infrastructure.persistence.mappers.TripMapper;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataTripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaTripRepositoryAdapterTest {

    private SpringDataTripRepository jpaRepository;
    private TripMapper mapper;
    private com.rappidrive.application.ports.output.OutboxRepositoryPort outboxRepository;
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private JpaTripRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(SpringDataTripRepository.class);
        mapper = mock(TripMapper.class);
        outboxRepository = mock(com.rappidrive.application.ports.output.OutboxRepositoryPort.class);
        objectMapper = mock(com.fasterxml.jackson.databind.ObjectMapper.class);
        adapter = new JpaTripRepositoryAdapter(jpaRepository, mapper, outboxRepository, objectMapper);
    }

    @Test
    void save_translatesOptimisticLockingToDomainException() {
        Trip trip = mock(Trip.class);
        TripJpaEntity entity = new TripJpaEntity();

        when(mapper.toJpaEntity(trip)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenThrow(new OptimisticLockingFailureException("conflict"));

        assertThrows(TripConcurrencyException.class, () -> adapter.save(trip));
    }
}
