package io.naraway.janitor.endpoint;

import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.context.JanitorStreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/janitor/event")
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class EventEndpoint {
    //
    private final ApplicationEventPublisher publisher;

    @PostMapping(value = {"", "/"}, headers = {"command=FireEvent"})
    public void fireEvent(@RequestHeader(value = "eventType") String eventType, @RequestBody String eventJson) {
        //
        JanitorStreamEvent event = JsonUtil.fromJson(eventJson, JanitorStreamEvent.class);

        try {
            Class clazz = Class.forName(event.getPayloadType());
            Object payload = JsonUtil.fromJson(event.getPayload(), clazz);
            this.publisher.publishEvent(payload);
        } catch (ClassNotFoundException e) {
            log.warn("Fire event is failed, cannot convert payload", e);
        } catch (Exception e) {
            log.warn("Fire event is failed with unknown reason", e);
        }
    }
}