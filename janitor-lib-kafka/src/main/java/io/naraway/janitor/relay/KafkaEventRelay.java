package io.naraway.janitor.relay;

import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.autoconfigure.JanitorProperties;
import io.naraway.janitor.context.JanitorStreamEvent;
import io.naraway.janitor.event.JanitorEventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class KafkaEventRelay implements EventRelay {
    //
    private final JanitorProperties properties;
    private final KafkaTemplate kafkaTemplate;

    public KafkaEventRelay(JanitorProperties properties) {
        //
        this.properties = properties;
        this.kafkaTemplate = new KafkaTemplate(producerFactory());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Override
    public void publish(JanitorStreamEvent event) {
        //
        log.trace("After commit, payload class = {}", event.getPayloadType());
        publishEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Override
    public void publishAfterRollback(JanitorStreamEvent event) {
        //
        log.trace("After rollback, payload class = {}", event.getPayloadType());
        if (event.getEventType() == JanitorEventType.Request) {
            publishEvent(event);
        }
    }

    private void publishEvent(JanitorStreamEvent event) {
        //
        String topic = getTopic(event);
        if (topic == null) {
            log.trace("Event was disabled by configuration, type = {}", event.getPayloadType());
            return;
        }

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, JsonUtil.toJson(event));
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                //
                log.warn("Send to kafka failed, exception = {}", throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                //
                log.trace("Send to kafka success");
            }
        });

        log.trace("Sent message to Kafka successfully, event = {}", JsonUtil.toPrettyJson(event));
    }

    @SuppressWarnings("java:S2275")
    private ProducerFactory producerFactory() {
        //
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.properties.getServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        if (!CollectionUtils.isEmpty(this.properties.getKafkaConfigs())) {
            List<String> kafkaConfigs = this.properties.getKafkaConfigs();
            kafkaConfigs.forEach(kafkaConfig -> {
                String key = kafkaConfig.substring(0, kafkaConfig.indexOf('='));
                String value = kafkaConfig.substring(kafkaConfig.indexOf('=') + 1);
                config.put(key, value);
            });
        }

        return new DefaultKafkaProducerFactory<>(config);
    }

    private String getTopic(JanitorStreamEvent event) {
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
