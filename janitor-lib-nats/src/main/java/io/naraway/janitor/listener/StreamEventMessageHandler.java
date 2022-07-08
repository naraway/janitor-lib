/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.listener;

import io.naraway.accent.domain.trail.wrapper.StreamEvent;
import io.naraway.accent.util.json.JsonUtil;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;

@Slf4j
public class StreamEventMessageHandler implements MessageHandler {
    //
    private final ApplicationEventPublisher eventPublisher;
    private final String subject;

    public StreamEventMessageHandler(ApplicationEventPublisher eventPublisher, String subject) {
        //
        this.eventPublisher = eventPublisher;
        this.subject = subject;
    }

    @Override
    public void onMessage(Message msg) throws InterruptedException {
        //
        String json = new String(msg.getData(), StandardCharsets.UTF_8);
        StreamEvent event = JsonUtil.fromJson(json, StreamEvent.class);

        Class payloadClass = getMessagePayloadClass(event);
        log.debug("Received from NATS[{}] : class = {}, id = {}", subject, (payloadClass != null ? payloadClass.getName() : ""), event.getId());

        Object message = JsonUtil.fromJson(event.getPayload(), payloadClass);
        eventPublisher.publishEvent(message);
    }

    private Class getMessagePayloadClass(StreamEvent event) {
        //
        try {
            return Class.forName(event.getPayloadClass());
        } catch (ClassNotFoundException e) {
            log.info(e.getMessage());
        }
        return null;
    }
}
