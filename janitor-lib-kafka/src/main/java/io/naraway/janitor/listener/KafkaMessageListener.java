package io.naraway.janitor.listener;

import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.context.JanitorStreamEvent;
import io.naraway.janitor.converter.PayloadConvertException;
import io.naraway.janitor.converter.PayloadConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"java:S1068", "java:S3740"})
public class KafkaMessageListener implements AcknowledgingMessageListener<String, String> {
    //
    private final ApplicationEventPublisher publisher;
    private final PayloadConverter converter;

    @Override
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        //
        if (log.isTraceEnabled()) {
            log.trace("Stream received: " + consumerRecord.value());
        }
        boolean success = false;
        JanitorStreamEvent event = JsonUtil.fromJson(consumerRecord.value(), JanitorStreamEvent.class);

        try {
            Object payload = this.converter.convert(event);
            this.publisher.publishEvent(payload);
            success = true;
        } catch (PayloadConvertException e) {
            log.info("Cannot process payload, {}", e.getMessage());
        }

        if (success) {
            acknowledgment.acknowledge();
        }
    }
}
