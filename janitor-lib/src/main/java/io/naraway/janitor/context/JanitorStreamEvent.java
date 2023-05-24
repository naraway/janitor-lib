/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.context;

import io.naraway.accent.domain.message.AbstractQuery;
import io.naraway.accent.domain.message.CommandRequest;
import io.naraway.accent.domain.message.DataEvent;
import io.naraway.accent.domain.message.DomainEvent;
import io.naraway.accent.domain.message.DomainMessage;
import io.naraway.accent.util.json.JsonSerializable;
import io.naraway.janitor.event.JanitorEventType;
import io.naraway.janitor.event.NamedChannelEvent;
import lombok.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
// NOTE: Temporary
public class JanitorStreamEvent implements JsonSerializable {
    //
    private String id;
    private OffsetDateTime time;
    private JanitorEventType eventType;
    @Setter(AccessLevel.NONE)
    private Map<String, String> header;
    private String payloadType;
    private String payload;
    private String routeKey;
    @Getter(AccessLevel.NONE)
    private URI source;
    private String subject;
    private String channelName;

    public JanitorStreamEvent() {
        //
        this.id = UUID.randomUUID().toString();
        this.time = OffsetDateTime.now();
        this.header = new HashMap<>();
    }

    public JanitorStreamEvent(DomainMessage payload) {
        //
        this();

        if (payload == null) {
            return;
        }

        this.payload = payload.toJson();
        if (NamedChannelEvent.class.isAssignableFrom(payload.getClass())) {
            this.eventType = JanitorEventType.NamedChannel;
            this.channelName = ((NamedChannelEvent) payload).getChannelName();
        } else if (DataEvent.class.isAssignableFrom(payload.getClass())) {
            this.eventType = JanitorEventType.Data;
        } else if (DomainEvent.class.isAssignableFrom(payload.getClass())) {
            this.eventType = JanitorEventType.Domain;
        } else if (AbstractQuery.class.isAssignableFrom(payload.getClass())) {
            this.eventType = JanitorEventType.Request;
        } else if (CommandRequest.class.isAssignableFrom(payload.getClass())) {
            this.eventType = JanitorEventType.Request;
        }
    }

    public Object getHeader(String name) {
        //
        return this.header.get(name);
    }

    public Set<String> getHeaderNames() {
        //
        return this.header.keySet();
    }

    public static JanitorStreamEvent newInstance() {
        //
        return new JanitorStreamEvent();
    }
}