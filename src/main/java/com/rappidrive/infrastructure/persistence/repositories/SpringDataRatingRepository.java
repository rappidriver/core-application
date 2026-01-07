package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.infrastructure.persistence.entities.RatingJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository para avaliações.
 */
public interface SpringDataRatingRepository extends JpaRepository<RatingJpaEntity, UUID> {
    
    Optional<RatingJpaEntity> findByTripIdAndRaterIdAndType(UUID tripId, UUID raterId, String type);
    
    List<RatingJpaEntity> findByTripId(UUID tripId);
    
    List<RatingJpaEntity> findByRateeIdAndTypeAndStatus(UUID rateeId, String type, String status);
    
    boolean existsByTripIdAndRaterIdAndType(UUID tripId, UUID raterId, String type);
    
    @Query("SELECT AVG(r.score) FROM RatingJpaEntity r WHERE r.rateeId = :rateeId AND r.type = :type AND r.status = :status")
    Double calculateAverageByRateeIdAndTypeAndStatus(
        @Param("rateeId") UUID rateeId, 
        @Param("type") String type, 
        @Param("status") String status
    );
    
    @Query("SELECT COUNT(r) FROM RatingJpaEntity r WHERE r.rateeId = :rateeId AND r.type = :type AND r.status = :status")
    Long countByRateeIdAndTypeAndStatus(
        @Param("rateeId") UUID rateeId, 
        @Param("type") String type, 
        @Param("status") String status
    );
    
    @Query("SELECT r FROM RatingJpaEntity r WHERE r.rateeId = :rateeId AND r.type = :type AND r.status = 'ACTIVE' ORDER BY r.createdAt DESC")
    List<RatingJpaEntity> findRecentByRateeIdAndType(
        @Param("rateeId") UUID rateeId, 
        @Param("type") String type, 
        Pageable pageable
    );
}
