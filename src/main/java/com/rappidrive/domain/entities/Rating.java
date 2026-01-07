package com.rappidrive.domain.entities;

import com.rappidrive.domain.enums.RatingStatus;
import com.rappidrive.domain.enums.RatingType;
import com.rappidrive.domain.exceptions.InvalidRatingException;
import com.rappidrive.domain.valueobjects.RatingComment;
import com.rappidrive.domain.valueobjects.RatingScore;
import com.rappidrive.domain.valueobjects.TenantId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representando uma avaliação (rating).
 * Passageiros podem avaliar motoristas e vice-versa após conclusão de uma viagem.
 * 
 * Invariantes:
 * - Score deve estar entre 1 e 5
 * - Comentário opcional com max 500 caracteres
 * - Ratings são imutáveis após criação
 * - Apenas soft-delete através de status
 */
public class Rating {
    
    private final UUID id;
    private final UUID tripId;
    private final UUID raterId;      // Quem avalia
    private final UUID rateeId;      // Quem é avaliado
    private final RatingType type;
    private final RatingScore score;
    private final RatingComment comment;
    private RatingStatus status;
    private final TenantId tenantId;
    private final LocalDateTime createdAt;
    
    /**
     * Construtor para criar nova avaliação.
     */
    public Rating(
            UUID tripId,
            UUID raterId,
            UUID rateeId,
            RatingType type,
            RatingScore score,
            RatingComment comment,
            TenantId tenantId
    ) {
        this(
            UUID.randomUUID(),
            tripId,
            raterId,
            rateeId,
            type,
            score,
            comment,
            RatingStatus.ACTIVE,
            tenantId,
            LocalDateTime.now()
        );
    }
    
    /**
     * Construtor de reconstituição (para repositório).
     */
    public Rating(
            UUID id,
            UUID tripId,
            UUID raterId,
            UUID rateeId,
            RatingType type,
            RatingScore score,
            RatingComment comment,
            RatingStatus status,
            TenantId tenantId,
            LocalDateTime createdAt
    ) {
        validateConstructorParams(id, tripId, raterId, rateeId, type, score, tenantId, createdAt);
        
        this.id = id;
        this.tripId = tripId;
        this.raterId = raterId;
        this.rateeId = rateeId;
        this.type = type;
        this.score = score;
        this.comment = comment != null ? comment : RatingComment.empty();
        this.status = status;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
    }
    
    private void validateConstructorParams(
            UUID id, UUID tripId, UUID raterId, UUID rateeId,
            RatingType type, RatingScore score, TenantId tenantId, LocalDateTime createdAt
    ) {
        if (id == null) {
            throw new InvalidRatingException("Rating ID não pode ser nulo");
        }
        if (tripId == null) {
            throw new InvalidRatingException("Trip ID não pode ser nulo");
        }
        if (raterId == null) {
            throw new InvalidRatingException("Rater ID não pode ser nulo");
        }
        if (rateeId == null) {
            throw new InvalidRatingException("Ratee ID não pode ser nulo");
        }
        if (type == null) {
            throw new InvalidRatingException("Rating type não pode ser nulo");
        }
        if (score == null) {
            throw new InvalidRatingException("Rating score não pode ser nulo");
        }
        if (tenantId == null) {
            throw new InvalidRatingException("Tenant ID não pode ser nulo");
        }
        if (createdAt == null) {
            throw new InvalidRatingException("Created at não pode ser nulo");
        }
        if (raterId.equals(rateeId)) {
            throw new InvalidRatingException("Usuário não pode avaliar a si mesmo");
        }
    }
    
    /**
     * Marca avaliação como reportada (denunciada como ofensiva).
     */
    public void markAsReported() {
        if (this.status == RatingStatus.DELETED) {
            throw new InvalidRatingException("Não é possível reportar avaliação deletada");
        }
        this.status = RatingStatus.REPORTED;
    }
    
    /**
     * Remove avaliação (soft-delete).
     */
    public void delete() {
        this.status = RatingStatus.DELETED;
    }
    
    /**
     * Verifica se a avaliação está ativa.
     */
    public boolean isActive() {
        return this.status.isActive();
    }
    
    /**
     * Verifica se a avaliação foi reportada.
     */
    public boolean isReported() {
        return this.status.isReported();
    }
    
    /**
     * Verifica se a avaliação foi deletada.
     */
    public boolean isDeleted() {
        return this.status.isDeleted();
    }
    
    /**
     * Verifica se tem comentário.
     */
    public boolean hasComment() {
        return comment.isPresent();
    }
    
    /**
     * Verifica se é uma avaliação de motorista.
     */
    public boolean isDriverRating() {
        return type.isDriverRating();
    }
    
    /**
     * Verifica se é uma avaliação de passageiro.
     */
    public boolean isPassengerRating() {
        return type.isPassengerRating();
    }
    
    // Getters
    public UUID getId() {
        return id;
    }
    
    public UUID getTripId() {
        return tripId;
    }
    
    public UUID getRaterId() {
        return raterId;
    }
    
    public UUID getRateeId() {
        return rateeId;
    }
    
    public RatingType getType() {
        return type;
    }
    
    public RatingScore getScore() {
        return score;
    }
    
    public RatingComment getComment() {
        return comment;
    }
    
    public RatingStatus getStatus() {
        return status;
    }
    
    public TenantId getTenantId() {
        return tenantId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return Objects.equals(id, rating.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", tripId=" + tripId +
                ", type=" + type +
                ", score=" + score.value() +
                ", status=" + status +
                ", hasComment=" + hasComment() +
                '}';
    }
}
