package com.rappidrive.infrastructure.persistence.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity para persistência de avaliações.
 */
@Entity
@Table(
    name = "ratings",
    indexes = {
        @Index(name = "idx_ratings_trip_id", columnList = "trip_id"),
        @Index(name = "idx_ratings_ratee_type_status", columnList = "ratee_id,type,status"),
        @Index(name = "idx_ratings_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_ratings_created_at", columnList = "created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_ratings_trip_rater_type", columnNames = {"trip_id", "rater_id", "type"})
    }
)
public class RatingJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "trip_id", nullable = false)
    private UUID tripId;
    
    @Column(name = "rater_id", nullable = false)
    private UUID raterId;
    
    @Column(name = "ratee_id", nullable = false)
    private UUID rateeId;
    
    @Column(name = "type", nullable = false, length = 30)
    private String type;
    
    @Column(name = "score", nullable = false)
    private Integer score;
    
    @Column(name = "comment", length = 500)
    private String comment;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public RatingJpaEntity() {
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getTripId() {
        return tripId;
    }
    
    public void setTripId(UUID tripId) {
        this.tripId = tripId;
    }
    
    public UUID getRaterId() {
        return raterId;
    }
    
    public void setRaterId(UUID raterId) {
        this.raterId = raterId;
    }
    
    public UUID getRateeId() {
        return rateeId;
    }
    
    public void setRateeId(UUID rateeId) {
        this.rateeId = rateeId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public UUID getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
