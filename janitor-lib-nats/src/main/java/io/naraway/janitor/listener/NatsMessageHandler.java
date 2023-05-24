package io.naraway.janitor.listener;

import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.context.JanitorStreamEvent;
import io.naraway.janitor.converter.PayloadConverter;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class NatsMessageHandler implements MessageHandler {
    //
    private final ApplicationEventPublisher publisher;
    private final String subscription;
    private final PayloadConverter converter;

    @Override
    @SuppressWarnings("java:S4449")
    public void onMessage(Message message) {
        //
        if (log.isTraceEnabled()) {
            log.trace("Message received: " + new String(message.getData(), StandardCharsets.UTF_8));
        }
        boolean success = false;
        String json = new String(message.getData(), StandardCharsets.UTF_8);
        JanitorStreamEvent event = JsonUtil.fromJson(json, JanitorStreamEvent.class);

        try {
            Object payload = this.converter.convert(event);
            log.trace("Received from Nats [{}] : class = {}, id = {}",
                    this.subscription, (payload != null ? payload.getClass().getName() : ""), event.getId());
            this.publisher.publishEvent(payload);
            success = true;
        } catch (Exception e) {
            log.info("Cannot process payload, {}", e.getMessage());
        }

        if (success) {
            message.ack();
        }
    }
}
