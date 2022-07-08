/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.relay;

import io.naraway.accent.domain.trail.TrailMessageType;
import io.naraway.accent.domain.trail.wrapper.StreamEvent;
import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.configuration.JetStreamConnection;
import io.naraway.janitor.proxy.Relay;
import io.naraway.janitor.configuration.NatsModeCondition;
import io.naraway.janitor.shipper.UDPClient;
import io.nats.client.Connection;
import io.nats.client.Nats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@Conditional(NatsModeCondition.class)
@RequiredArgsConstructor
public class RelayToNats implements Relay {
    //
    private final JetStreamConnection jetStreamConnection;

    @Value("${nara.janitor.bootstrap-address:}")
    private String broker;
    @Value("${nara.janitor.id}")
    private String serviceName;
    @Value("${nara.janitor.crypto.data.fields:}")
    private List<String> dataFields;

    @Value("${nara.janitor.logstash.address:10.0.2.100}")
    private String logstashServerAddress;
    @Value("${nara.janitor.logstash.port:4452}")
    private int logstashServerPort;

    private UDPClient udpClient;

    @PostConstruct
    public void init() throws Exception{
        //
        this.udpClient = new UDPClient(logstashServerAddress, logstashServerPort);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void publish(StreamEvent event) {

        //
        log.debug("after commit, payload class = {}", event.getPayloadClass());
        publishEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Override
    public void publishAfterRollback(StreamEvent event) {
        //
        log.debug("after rollback, payload class = {}", event.getPayloadClass());
        if (TrailMessageType.CommandRequest.name().equals(event.getPayloadType())
                || TrailMessageType.QueryRequest.name().equals(event.getPayloadType())
                || TrailMessageType.DynamicQueryRequest.name().equals(event.getPayloadType())) {
            publishEvent(event);
        }
    }

    private void publishEvent(StreamEvent event){
        //  publish to JetStream
        String subject = getSubject(event);
        byte[] messageBytes = getMessageBytes(event);

        try {
            Connection nc = jetStreamConnection.getConnection();
            log.debug("Send to NATS[{}] : payload class = {}, id = {}", subject, event.getPayloadClass(), event.getId());
            nc.publish(subject, messageBytes);
            udpClient.sendLog(messageBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] getMessageBytes(StreamEvent event) {
        //
        return JsonUtil.toJson(event).getBytes(StandardCharsets.UTF_8);
    }

    private String getSubject(StreamEvent event) {
        //
        TrailMessageType messageType = TrailMessageType.valueOf(event.getPayloadType());

        switch (messageType) {
            case CommandRequest:
            case QueryRequest:
            case DynamicQueryRequest:
            case ClientRequest:
                return String.format("%s-%s", this.serviceName, "request");
            case DataEvent:
                return String.format("%s-%s", this.serviceName, "data");
            case DomainEvent:
                return String.format("%s-%s", this.serviceName, "domain");
            default:
                throw new IllegalArgumentException("Unimplemented payload type = " + event.getPayloadType());
        }
    }
}
