package io.naraway.janitor.relay;

import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.autoconfigure.JanitorProperties;
import io.naraway.janitor.connection.JetStreamConnection;
import io.naraway.janitor.context.JanitorStreamEvent;
import io.naraway.janitor.event.JanitorEventType;
import io.nats.client.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class NatsEventRelay implements EventRelay {
    //
    private final JanitorProperties properties;
    private final JetStreamConnection jetStreamConnection;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publish(JanitorStreamEvent event) {
        //
        log.trace("After commit, payload class = {}", event.getPayloadType());
        publishEvent(event);
    }

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void publishAfterRollback(JanitorStreamEvent event) {
        //
        log.trace("After rollback, payload class = {}", event.getEventType());
        if (event.getEventType() == JanitorEventType.Request) {
            publishEvent(event);
        }
    }

    private void publishEvent(JanitorStreamEvent event) {
        //
        String subject = getSubject(event);
        if (subject == null) {
            log.trace("Event was disabled by configuration, type = {}", event.getEventType());
            return;
        }

        try {
            Connection connection = this.jetStreamConnection.getConnection();
            log.trace("Send to Nats [{}] : payload class = {}, id = {}", subject, event.getPayloadType(), event.getId());
            byte[] messageBytes = getMessageBytes(event);
            connection.publish(subject, messageBytes);
        } catch (Exception e) {
            log.warn("Publishing " + subject + " is failed", e);
        }
    }

    private byte[] getMessageBytes(JanitorStreamEvent event) {
        //
        return JsonUtil.toJson(event).getBytes(StandardCharsets.UTF_8);
    }

    private String getSubject(JanitorStreamEvent event) {
        //
        JanitorEventType eventType = event.getEventType();

        switch (eventType) {
            case Request:
                return this.properties.getEvent().getRequest().isEnabled()
                        ? String.format("%s-%s", this.properties.getName(), JanitorEventType.Request.postfix()) : null;
            case Data:
                return this.properties.getEvent().getData().isEnabled()
                        ? String.format("%s-%s", this.properties.getName(), JanitorEventType.Data.postfix()) : null;
            case Domain:
                return this.properties.getEvent().getDomain().isEnabled()
                        ? String.format("%s-%s", this.properties.getName(), JanitorEventType.Domain.postfix()) : null;
            case NamedChannel:
                return this.properties.getEvent().getNamedChannel().isEnabled()
                               ? event.getChannelName() : null;
            default:
                throw new IllegalArgumentException("Unimplemented event type = " + event.getEventType());
        }
    }
}
