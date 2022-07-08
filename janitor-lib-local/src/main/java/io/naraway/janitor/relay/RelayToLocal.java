/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.relay;

import io.naraway.accent.domain.trail.TrailMessageType;
import io.naraway.accent.domain.trail.wrapper.StreamEvent;
import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.proxy.Relay;
import io.naraway.janitor.configuration.LocalModeCondition;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Slf4j
@Component
@Conditional(LocalModeCondition.class)
@NoArgsConstructor
public class RelayToLocal implements Relay {
    //
    @Value("${nara.janitor.id}")
    private String serviceName;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(StreamEvent event) {
        //
        log.debug("after commit, payload class = {}", event.getPayloadClass());
        log.debug("after commit, event = {}", JsonUtil.toJson(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void publishAfterRollback(StreamEvent event) {
        //
        log.debug("after rollback, payload class = {}", event.getPayloadClass());
        if (TrailMessageType.CommandRequest.name().equals(event.getPayloadType())
                || TrailMessageType.QueryRequest.name().equals(event.getPayloadType())
                || TrailMessageType.DynamicQueryRequest.name().equals(event.getPayloadType())) {
            log.debug("after rollback, event = {}", JsonUtil.toJson(event));
        }
    }
}
