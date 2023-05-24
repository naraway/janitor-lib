package io.naraway.janitor.relay;

import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.autoconfigure.JanitorProperties;
import io.naraway.janitor.context.JanitorStreamEvent;
import io.naraway.janitor.event.JanitorEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
public class LocalEventRelay implements EventRelay {
    //
    private final JanitorProperties properties;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Override
    public void publish(JanitorStreamEvent event) {
        //
        if (!enabled(event)) {
            log.trace("Event was disabled by configuration, type = {}", event.getEventType());
            return;
        }

        log.trace("After commit, payload class = {}", event.getPayloadType());
        log.trace("After commit, event = {}", JsonUtil.toJson(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Override
    public void publishAfterRollback(JanitorStreamEvent event) {
        //
        if (!this.properties.getEvent().getRequest().isEnabled()) {
            log.trace("RequestRelay event was disabled by configuration, type = {}", event.getEventType());
            return;
        }

        log.trace("After rollback, payload class = {}", event.getPayloadType());
        if (event.getEventType() == JanitorEventType.Request) {
            log.trace("After rollback, event = {}", JsonUtil.toJson(event));
        }
    }

    private boolean enabled(JanitorStreamEvent event) {
        //
        JanitorEventType eventType = event.getEventType();

        switch (eventType) {
            case Request:
                return this.properties.getEvent().getRequest().isEnabled();
            case Data:
                return this.properties.getEvent().getData().isEnabled();
            case Domain:
                return this.properties.getEvent().getDomain().isEnabled();
            default:
                throw new IllegalArgumentException("Unimplemented event type = " + event.getEventType());
        }
    }
}