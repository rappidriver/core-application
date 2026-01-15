package com.rappidrive.domain.entities;

import com.rappidrive.domain.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public final class Tenant {

    private final TenantId id;
    private final String name;
    private final String slug;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Tenant(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "TenantId cannot be null");
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.slug = Objects.requireNonNull(builder.slug, "Slug cannot be null");
        this.active = builder.active;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(builder.updatedAt, "updatedAt cannot be null");
    }

    public static Tenant create(TenantId id, String name, String slug) {
        Instant now = Instant.now();
        return new Builder()
                .id(id)
                .name(name)
                .slug(slug)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public TenantId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class Builder {
        private TenantId id;
        private String name;
        private String slug;
        private boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(TenantId id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder slug(String slug) { this.slug = slug; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Tenant build() { return new Tenant(this); }
    }
}