package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.infrastructure.persistence.entities.NotificationJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório Spring Data JPA para notificações.
 */
public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {
    
    /**
     * Busca notificação por chave de idempotência
     */
    Optional<NotificationJpaEntity> findByIdempotencyKeyAndTenantIdAndDeletedAtIsNull(
        String idempotencyKey, 
        UUID tenantId
    );
    
    /**
     * Busca notificações de um usuário (paginado)
     */
    List<NotificationJpaEntity> findByUserIdAndTenantIdAndDeletedAtIsNullOrderByCreatedAtDesc(
        UUID userId,
        UUID tenantId,
        Pageable pageable
    );
    
    /**
     * Busca notificações de um usuário filtradas por status
     */
    List<NotificationJpaEntity> findByUserIdAndStatusAndTenantIdAndDeletedAtIsNullOrderByCreatedAtDesc(
        UUID userId,
        String status,
        UUID tenantId,
        Pageable pageable
    );
    
    /**
     * Conta notificações não lidas (status = PENDING ou SENT)
     */
    @Query("""
        SELECT COUNT(n) 
        FROM NotificationJpaEntity n 
        WHERE n.userId = :userId 
          AND n.tenantId = :tenantId 
          AND n.status IN ('PENDING', 'SENT') 
          AND n.deletedAt IS NULL
    """)
    Long countUnreadByUserId(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);
    
    /**
     * Soft-delete de notificações antigas
     */
    @Modifying
    @Query("""
        UPDATE NotificationJpaEntity n 
        SET n.deletedAt = CURRENT_TIMESTAMP 
        WHERE n.deletedAt IS NULL 
          AND (
              (n.status = 'READ' AND n.readAt < :cutoffDate) OR
              (n.status IN ('PENDING', 'SENT') AND n.createdAt < :oldCutoffDate) OR
              (n.status = 'FAILED' AND n.failedAt < :failedCutoffDate)
          )
    """)
    void softDeleteOldNotifications(
        @Param("cutoffDate") LocalDateTime cutoffDate,
        @Param("oldCutoffDate") LocalDateTime oldCutoffDate,
        @Param("failedCutoffDate") LocalDateTime failedCutoffDate
    );
    
    /**
     * Verifica se existe notificação com idempotency key
     */
    boolean existsByIdempotencyKeyAndTenantIdAndDeletedAtIsNull(String idempotencyKey, UUID tenantId);
}
