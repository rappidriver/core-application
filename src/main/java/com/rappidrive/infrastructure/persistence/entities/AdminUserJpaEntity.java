package com.rappidrive.infrastructure.persistence.entities;

import com.rappidrive.domain.enums.AdminRole;
import com.rappidrive.domain.valueobjects.Email;
import com.rappidrive.domain.valueobjects.TenantId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_users")
public class AdminUserJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private TenantId tenantId;

    @Column(nullable = false)
    private Email email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AdminRole role;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AdminUserJpaEntity() {}

    public AdminUserJpaEntity(UUID id, TenantId tenantId, Email email, AdminRole role, String fullName, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public Email getEmail() { return email; }
    public AdminRole getRole() { return role; }
    public String getFullName() { return fullName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
