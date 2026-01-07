package com.rappidrive.infrastructure.persistence.mappers;

import com.rappidrive.domain.entities.Rating;
import com.rappidrive.domain.enums.RatingStatus;
import com.rappidrive.domain.enums.RatingType;
import com.rappidrive.domain.valueobjects.RatingComment;
import com.rappidrive.domain.valueobjects.RatingScore;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.RatingJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversão entre Rating e RatingJpaEntity.
 */
@Component
public class RatingMapper {
    
    /**
     * Converte domain entity para JPA entity.
     */
    public RatingJpaEntity toJpaEntity(Rating rating) {
        if (rating == null) {
            return null;
        }
        
        RatingJpaEntity entity = new RatingJpaEntity();
        entity.setId(rating.getId());
        entity.setTripId(rating.getTripId());
        entity.setRaterId(rating.getRaterId());
        entity.setRateeId(rating.getRateeId());
        entity.setType(rating.getType().name());
        entity.setScore(rating.getScore().value());
        entity.setComment(rating.getComment().value());
        entity.setStatus(rating.getStatus().name());
        entity.setTenantId(rating.getTenantId().getValue());
        entity.setCreatedAt(rating.getCreatedAt());
        
        return entity;
    }
    
    /**
     * Converte JPA entity para domain entity.
     */
    public Rating toDomain(RatingJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new Rating(
            entity.getId(),
            entity.getTripId(),
            entity.getRaterId(),
            entity.getRateeId(),
            RatingType.valueOf(entity.getType()),
            RatingScore.of(entity.getScore()),
            RatingComment.of(entity.getComment()),
            RatingStatus.valueOf(entity.getStatus()),
            new TenantId(entity.getTenantId()),
            entity.getCreatedAt()
        );
    }
}
