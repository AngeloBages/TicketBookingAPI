package com.ticket_booking.domain.models;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
public abstract class AggregateRoot {

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    protected void registerEvent(Object event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    public List<Object> releaseDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
}