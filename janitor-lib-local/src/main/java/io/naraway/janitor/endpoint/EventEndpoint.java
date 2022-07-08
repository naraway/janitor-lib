/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.endpoint;

import io.naraway.accent.domain.trail.wrapper.StreamEvent;
import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.configuration.LocalModeCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/standalone/event")
@Conditional(LocalModeCondition.class)
@Slf4j
public class EventEndpoint {
    //
    private final ApplicationEventPublisher eventPublisher;

    @Value("${cryptography.data.fields:}")
    private List<String> dataFields;

    public EventEndpoint(ApplicationEventPublisher eventPublisher) {
        //
        this.eventPublisher = eventPublisher;
    }

    @PostMapping(value = {"", "/"}, headers = {"command=FireEvent"})
    public void fireEvent(@RequestHeader(value = "eventType") String eventType, @RequestBody String eventJson) {
        //
        StreamEvent event = JsonUtil.fromJson(eventJson, StreamEvent.class);

        try {
            Class clazz = Class.forName(event.getPayloadClass());
            Object payload = JsonUtil.fromJson(event.getPayload(), clazz);
            eventPublisher.publishEvent(payload);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw e;
        }
    }
}
