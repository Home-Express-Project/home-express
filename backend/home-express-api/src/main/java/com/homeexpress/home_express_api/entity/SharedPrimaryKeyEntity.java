package com.homeexpress.home_express_api.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

/**
 * Base class for entities that share their primary key with another entity via {@code @MapsId}.
 * Spring Data relies on {@link Persistable#isNew()} to decide between {@code persist} and {@code merge}.
 * Because these entities assign the identifier manually, we track the persisted state ourselves.
 */
@MappedSuperclass
public abstract class SharedPrimaryKeyEntity<ID> implements Persistable<ID> {

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew || getId() == null;
    }

    /**
     * Mark the entity as freshly created so the next save operation uses {@code persist}.
     */
    protected void markNew() {
        this.isNew = true;
    }

    /**
     * Mark the entity as existing so updates use {@code merge}.
     */
    protected void markNotNew() {
        this.isNew = false;
    }

    @PostLoad
    @PostPersist
    protected void trackPersistenceState() {
        this.isNew = false;
    }
}
