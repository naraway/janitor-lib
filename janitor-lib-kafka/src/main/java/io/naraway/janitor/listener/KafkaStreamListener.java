/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.listener;

import io.naraway.accent.domain.trail.wrapper.StreamEvent;
import io.naraway.accent.util.json.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

@Slf4j
public class KafkaStreamListener implements AcknowledgingMessageListener<String, String> {
    private final KafkaTemplate kafkaTemplate;
    private final ApplicationEventPublisher eventPublisher;
    //
    private final String serviceName;
    private final String topic;
    private final List<String> dataFields;

    public KafkaStreamListener(
            String serviceName,
            String topic,
            List<String> dataFields,
            KafkaTemplate kafkaTemplate,
            ApplicationEventPublisher eventPublisher) {
        //
        this.serviceName = serviceName;
        this.topic = topic;
        this.dataFields = dataFields;
        this.kafkaTemplate = kafkaTemplate;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        //
        log.trace("Stream received: " + consumerRecord.value());
        boolean success = false;
        // invoke handler
        StreamEvent event = JsonUtil.fromJson(consumerRecord.value(), StreamEvent.class);

        Class clazz = null;
        try {
            clazz = Class.forName(event.getPayloadClass());
        } catch (ClassNotFoundException e) {
            log.info(e.getMessage());
            acknowledgment.acknowledge();
            return;
        }

//        StreamEventMessage decryptedMessage = decrypt(message);

        try {
            Object payload = JsonUtil.fromJson(event.getPayload(), clazz);
            eventPublisher.publishEvent(payload);
            success = true;
        } catch (Exception e) {
            throw e;
        }

        if (success) {
            acknowledgment.acknowledge();
        }
    }
}
