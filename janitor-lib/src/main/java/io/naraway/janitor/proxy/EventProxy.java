/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.proxy;

import io.naraway.accent.domain.trail.DataEvent;
import io.naraway.accent.domain.trail.DomainEvent;
import io.naraway.accent.domain.trail.TrailMessageType;
import io.naraway.janitor.context.TrailContextController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventProxy {
    //
    private final ApplicationEventPublisher publisher;

    public void publishEvent(JanitorStreamEvent message) {
        //
        publisher.publishEvent(message);
    }

    public void publishEvent(DomainEvent message) {
        //
        TrailContextController.injectTrailInfo(message);
        JanitorStreamEvent event = new JanitorStreamEvent(message);
        event.setPayloadType(TrailMessageType.DomainEvent.name());
        publisher.publishEvent(event);
    }

    public void publishEvent(DomainEvent message, String... entityIds) {
        //
        TrailContextController.injectTrailInfo(message);
        JanitorStreamEvent event = new JanitorStreamEvent(message);


        StringBuilder routingKey = new StringBuilder();
        for (String entityId : entityIds) {
            routingKey.append(entityId);
            routingKey.append(":");
        }

        if (routingKey.length() > 0) {
            routingKey.deleteCharAt(routingKey.length() - 1);
            event.setRoutingKey(routingKey.toString());
        }

        event.setPayloadType(TrailMessageType.DomainEvent.name());
        publisher.publishEvent(event);
    }

    public void publishEvent(DataEvent message) {
        //
        TrailContextController.injectTrailInfo(message);
        JanitorStreamEvent event = new JanitorStreamEvent(message);
        event.setRoutingKey(message.getEntityId());
        event.setPayloadType(TrailMessageType.DataEvent.name());
        publisher.publishEvent(event);
    }
}
