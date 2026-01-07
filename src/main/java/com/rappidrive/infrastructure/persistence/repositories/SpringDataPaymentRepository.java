package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.domain.enums.PaymentStatus;
import com.rappidrive.infrastructure.persistence.entities.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for PaymentJpaEntity.
 */
@Repository
public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, UUID> {
    
    /**
     * Finds payment by trip ID.
     * Each trip should have at most one payment.
     */
    Optional<PaymentJpaEntity> findByTripId(UUID tripId);
    
    /**
     * Finds payments by tenant within a date range.
     * Useful for reports and analytics.
     */
    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.tenantId = :tenantId " +
           "AND p.createdAt >= :startDate AND p.createdAt <= :endDate " +
           "ORDER BY p.createdAt DESC")
    List<PaymentJpaEntity> findByTenantIdAndCreatedAtBetween(
        @Param("tenantId") UUID tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Finds all payments for a tenant.
     */
    List<PaymentJpaEntity> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    
    /**
     * Finds payments by status.
     */
    List<PaymentJpaEntity> findByStatus(PaymentStatus status);
    
    /**
     * Checks if payment exists for trip.
     */
    boolean existsByTripId(UUID tripId);
    
    /**
     * Finds payment by gateway transaction ID.
     * Useful for reconciliation with payment gateway.
     */
    Optional<PaymentJpaEntity> findByGatewayTransactionId(String gatewayTransactionId);
}
