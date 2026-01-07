package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.RatingRepositoryPort;
import com.rappidrive.domain.entities.Rating;
import com.rappidrive.domain.enums.RatingStatus;
import com.rappidrive.domain.enums.RatingType;
import com.rappidrive.infrastructure.persistence.entities.RatingJpaEntity;
import com.rappidrive.infrastructure.persistence.mappers.RatingMapper;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataRatingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter JPA para repositório de avaliações.
 */
@Component
public class JpaRatingRepositoryAdapter implements RatingRepositoryPort {
    
    private final SpringDataRatingRepository jpaRepository;
    private final RatingMapper mapper;
    
    public JpaRatingRepositoryAdapter(
            SpringDataRatingRepository jpaRepository,
            RatingMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Rating save(Rating rating) {
        RatingJpaEntity entity = mapper.toJpaEntity(rating);
        RatingJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Rating> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Rating> findByTripIdAndRaterIdAndType(UUID tripId, UUID raterId, RatingType type) {
        return jpaRepository.findByTripIdAndRaterIdAndType(tripId, raterId, type.name())
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Rating> findByRateeIdAndType(UUID rateeId, RatingType type, RatingStatus status) {
        return jpaRepository.findByRateeIdAndTypeAndStatus(rateeId, type.name(), status.name())
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Rating> findByTripId(UUID tripId) {
        return jpaRepository.findByTripId(tripId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public double calculateAverageByRateeId(UUID rateeId, RatingType type, RatingStatus status) {
        Double average = jpaRepository.calculateAverageByRateeIdAndTypeAndStatus(
            rateeId,
            type.name(),
            status.name()
        );
        return average != null ? average : 0.0;
    }
    
    @Override
    public long countByRateeIdAndType(UUID rateeId, RatingType type, RatingStatus status) {
        Long count = jpaRepository.countByRateeIdAndTypeAndStatus(
            rateeId,
            type.name(),
            status.name()
        );
        return count != null ? count : 0L;
    }
    
    @Override
    public boolean existsByTripIdAndRaterIdAndType(UUID tripId, UUID raterId, RatingType type) {
        return jpaRepository.existsByTripIdAndRaterIdAndType(tripId, raterId, type.name());
    }
    
    @Override
    public List<Rating> findRecentByRateeId(UUID rateeId, RatingType type, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return jpaRepository.findRecentByRateeIdAndType(rateeId, type.name(), pageRequest)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
