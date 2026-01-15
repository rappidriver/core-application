package com.rappidrive.domain.events;

import com.rappidrive.domain.valueobjects.TenantId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain Event - Published when a new tenant is successfully onboarded.
 * This event can trigger:
 * - Welcome email to admin
 * - Notification to operations team
 * - Analytics/audit logging
 * - Integration with external systems
 */
public class TenantOnboardedEvent implements DomainEvent {
    
    private final String id;
    private final TenantId tenantId;
    private final String displayName;
    private final String adminEmail;
    private final String serviceAreaName;
    private final LocalDateTime occurredAtTime;

    private TenantOnboardedEvent(Builder builder) {
        this.id = Objects.requireNonNull(builder.eventId, "EventId cannot be null");
        this.tenantId = Objects.requireNonNull(builder.tenantId, "TenantId cannot be null");
        this.displayName = Objects.requireNonNull(builder.displayName, "DisplayName cannot be null");
        this.adminEmail = Objects.requireNonNull(builder.adminEmail, "AdminEmail cannot be null");
        this.serviceAreaName = Objects.requireNonNull(builder.serviceAreaName, "ServiceAreaName cannot be null");
        this.occurredAtTime = Objects.requireNonNull(builder.occurredAt, "OccurredAt cannot be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredAtTime;
    }

    @Override
    public String eventId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public String getServiceAreaName() {
        return serviceAreaName;
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantOnboardedEvent that = (TenantOnboardedEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TenantOnboardedEvent{" +
                "eventId=" + id +
                ", tenantId=" + tenantId +
                ", displayName='" + displayName + '\'' +
                ", adminEmail='" + adminEmail + '\'' +
                ", occurredAt=" + occurredAtTime +
                '}';
    }

    public static class Builder {
        private String eventId = UUID.randomUUID().toString();
        private TenantId tenantId;
        private String displayName;
        private String adminEmail;
        private String serviceAreaName;
        private LocalDateTime occurredAt = LocalDateTime.now();

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder tenantId(TenantId tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder adminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
            return this;
        }

        public Builder serviceAreaName(String serviceAreaName) {
            this.serviceAreaName = serviceAreaName;
            return this;
        }

        public Builder occurredAt(LocalDateTime occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public TenantOnboardedEvent build() {
            return new TenantOnboardedEvent(this);
        }
    }
}
