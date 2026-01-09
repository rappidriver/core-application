package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.infrastructure.persistence.entities.OutboxEventJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query("SELECT o FROM OutboxEventJpaEntity o WHERE o.status = 'PENDING' AND (o.nextAttemptAt IS NULL OR o.nextAttemptAt <= :now) ORDER BY o.createdAt")
    List<OutboxEventJpaEntity> findPending(@Param("now") LocalDateTime now, Pageable pageable);

    // Native query for SKIP LOCKED (Postgres only)
    @Query(value = "SELECT * FROM outbox_event WHERE status = 'PENDING' AND (next_attempt_at IS NULL OR next_attempt_at <= now()) ORDER BY created_at FOR UPDATE SKIP LOCKED LIMIT :limit", nativeQuery = true)
    List<OutboxEventJpaEntity> findPendingForUpdate(@Param("limit") int limit);
}
