package com.ticket_booking.common;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.ticket_booking.domain.models.AggregateRoot;

@Component
public class DomainEventDispatcher {

    private final ApplicationEventPublisher eventPublisher;

    public DomainEventDispatcher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishEventsFor(AggregateRoot aggregate) {
        aggregate.releaseDomainEvents().forEach(eventPublisher::publishEvent);
    }
}
