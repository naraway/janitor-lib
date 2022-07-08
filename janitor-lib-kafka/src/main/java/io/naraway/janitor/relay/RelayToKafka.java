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
import io.naraway.janitor.configuration.KafkaConfiguration;
import io.naraway.janitor.configuration.KafkaModeCondition;
import io.naraway.janitor.configuration.KafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Conditional(KafkaModeCondition.class)
public class RelayToKafka implements Relay {
    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate kafkaTemplate;
    //
    @Value("${nara.janitor.id}")
    private String serviceName;
    @Value("${nara.janitor.crypto.data.fields:}")
    private List<String> dataFields;

    public RelayToKafka(KafkaProperties kafkaProperties) {
        //
        this.kafkaProperties = kafkaProperties;
        this.kafkaTemplate = new KafkaTemplate(producerFactory());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(StreamEvent event) {
        //
        log.debug("after commit, payload class = {}", event.getPayloadClass());
        publishEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void publishAfterRollback(StreamEvent event) {
        //
        log.debug("after rollback, payload class = {}", event.getPayloadClass());
        if (TrailMessageType.CommandRequest.name().equals(event.getPayloadType())
                || TrailMessageType.QueryRequest.name().equals(event.getPayloadType())
                || TrailMessageType.DynamicQueryRequest.name().equals(event.getPayloadType())) {
            publishEvent(event);
        }
    }

    private String getSendTopic(StreamEvent event) {
        //
        TrailMessageType payloadType = TrailMessageType.valueOf(event.getPayloadType());

        switch (payloadType) {
            case CommandRequest:
            case QueryRequest:
            case DynamicQueryRequest:
            case ClientRequest:
                return String.format("%s-%s", this.serviceName, KafkaConfiguration.TOPIC_POSTFIX_REQUEST);
            case DataEvent:
                return String.format("%s-%s", this.serviceName, KafkaConfiguration.TOPIC_POSTFIX_DATA);
            case DomainEvent:
                return String.format("%s-%s", this.serviceName, KafkaConfiguration.TOPIC_POSTFIX_DOMAIN);
            default:
                throw new IllegalArgumentException("Unimplemented payload type = " + event.getPayloadType());
        }
    }

    private void publishEvent(StreamEvent event) {
        //
        String sendTopic = getSendTopic(event);
//        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(sendTopic, eventMessage.getRoutingKey(), JsonUtil.toJson(event)); // FIXME: impl routing key
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(sendTopic, JsonUtil.toJson(event));
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                //
                log.warn("send to kafka failed, exception = {}", throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                //
                log.trace("send to kafka successed");
            }
        });

        log.trace("Sent message to Kafka successfully. event = {}", JsonUtil.toPrettyJson(event));
    }

    private ProducerFactory producerFactory() {
        //
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaProperties.getBootstrapAddress());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    public KafkaTemplate getKafkaTemplate() {
        //
        return this.kafkaTemplate;
    }
}
