package io.naraway.janitor.proxy;

import io.naraway.accent.domain.message.DataEvent;
import io.naraway.accent.domain.message.DomainEvent;
import io.naraway.janitor.context.JanitorContext;
import io.naraway.janitor.context.JanitorStreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
public class EventProxy {
    //
    private final ApplicationEventPublisher publisher;

    public void publishEvent(DomainEvent message) {
        //
        JanitorStreamEvent event = new JanitorStreamEvent(message);
        event.setPayloadType(message.getClass().getName());
        JanitorContext.set(event);

        this.publisher.publishEvent(event);
    }

    public void publishEvent(DomainEvent message, String... entityIds) {
        //
        JanitorStreamEvent event = new JanitorStreamEvent(message);

        StringBuilder routeKey = new StringBuilder();
        for (String entityId : entityIds) {
            routeKey.append(entityId);
            routeKey.append(":");
        }

        if (routeKey.length() > 0) {
            routeKey.deleteCharAt(routeKey.length() - 1);
            event.setRouteKey(routeKey.toString());
        }

        event.setPayloadType(message.getClass().getName());
        JanitorContext.set(event);

        this.publisher.publishEvent(event);
    }

    public void publishEvent(DataEvent message) {
        //
        JanitorStreamEvent event = new JanitorStreamEvent(message);

        event.setRouteKey(message.getEntityId());
        event.setPayloadType(message.getClass().getName());
        JanitorContext.set(event);

        this.publisher.publishEvent(event);
        this.publisher.publishEvent(message);
    }
}