package io.naraway.janitor.listener;

import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.autoconfigure.JanitorProperties;
import io.naraway.janitor.connection.JetStreamConnection;
import io.naraway.janitor.converter.PayloadConverter;
import io.naraway.janitor.enhancer.NatsConsumerConfigurationEnhancer;
import io.naraway.janitor.enhancer.NatsPushSubscribeOptionsEnhancer;
import io.naraway.janitor.event.JanitorEventType;
import io.nats.client.*;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@Slf4j
@RequiredArgsConstructor
public class NatsListenerInitializer {
    //
    private final JanitorProperties properties;
    private final JetStreamConnection jetStreamConnection;
    private final NatsConsumerConfigurationEnhancer consumerConfigEnhancer;
    private final NatsPushSubscribeOptionsEnhancer subscribeOptionsEnhancer;
    private final ApplicationEventPublisher publisher;
    private final PayloadConverter converter;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        //
        Connection connection = this.jetStreamConnection.getConnection();
        String consumerNamePrefix = this.properties.getName().replace('.', '_');
        String queueGroup = String.format("%s@queue_group", consumerNamePrefix);

        log.debug("Add Nats subscribers");

        if (CollectionUtils.isEmpty(this.properties.getSubscriptions())) {
            return;
        }

        for (String subscription : this.properties.getSubscriptions()) {
            if(subscription.startsWith("^")) {
                String serviceGroupName = subscription.substring(0, subscription.lastIndexOf('.'));
                serviceGroupName = serviceGroupName.replace("^", "");
                String subject = serviceGroupName.concat(".").concat(subscription.substring(subscription.lastIndexOf('.') + 1, subscription.length()));
                subscribe(connection, consumerNamePrefix, subject, queueGroup);
            } else {
                String subject = String.format("%s-%s", subscription, JanitorEventType.Domain.postfix());
                subscribe(connection, consumerNamePrefix, subject, queueGroup);
            }
        }
    }

    private void subscribe(Connection connection, String consumerNamePrefix, String subject, String queueGroup) {
        //
        try {
            JetStream js = connection.jetStream();

            // define consumer configuration with enhancer
            String consumerName = consumerNamePrefix.concat("@").concat(subject.replace('.', '_'));
            ConsumerConfiguration defaultConsumerConfig = ConsumerConfiguration.builder()
                    .durable(consumerName)
                    .filterSubject(subject)
                    .deliverPolicy(DeliverPolicy.New)
                    .replayPolicy(ReplayPolicy.Instant)
                    .build();
            ConsumerConfiguration enhancedConsumerConfig = withImmutableConfigChecksumPostfix(
                    this.consumerConfigEnhancer.enhance(defaultConsumerConfig));
            log.debug("Enhanced ConsumerConfiguration :\n{}", JsonUtil.toPrettyJson(enhancedConsumerConfig));

            // define pushSubscribe options with enhancer
            PushSubscribeOptions defaultSubscribeOptions = PushSubscribeOptions.builder()
                    .configuration(enhancedConsumerConfig).build();
            PushSubscribeOptions enhancedSubscribeOptions = this.subscribeOptionsEnhancer.enhance(
                    defaultSubscribeOptions);
            log.debug("Enhanced PushSubscribeOptions :\n{}", JsonUtil.toPrettyJson(enhancedSubscribeOptions));

            NatsMessageHandler messageHandler = new NatsMessageHandler(this.publisher, subject, this.converter);
            Dispatcher dispatcher = connection.createDispatcher();

            // subscribe
            JetStreamSubscription subscription = js.subscribe(
                    subject, queueGroup, dispatcher, messageHandler, false, enhancedSubscribeOptions);
            log.debug("JetStreamSubscription :\n{}", JsonUtil.toPrettyJson(subscription.getConsumerInfo()));
        } catch (IOException | JetStreamApiException e) {
            log.warn("Subscribing " + subject + " is failed", e);
        }
    }

    private ConsumerConfiguration withImmutableConfigChecksumPostfix(ConsumerConfiguration origin) {
        //
        long checksum = new ImmutableConsumerConfig(
                origin.getAckPolicy(),
                origin.getDeliverPolicy(),
                origin.getReplayPolicy())
                .checksum();

        return ConsumerConfiguration.builder(origin)
                .durable(origin.getDurable().concat("@").concat(String.valueOf(checksum)))
                .build();
    }

    @AllArgsConstructor
    private static class ImmutableConsumerConfig {
        //
        private AckPolicy ackPolicy;
        private DeliverPolicy deliverPolicy;
        private ReplayPolicy replayPolicy;

        @Override
        public String toString() {
            //
            return String.format("ImmutableConsumerConfig { ackPolicy=%s, deliverPolicy=%s, replayPolicy=%s }",
                    this.ackPolicy, this.deliverPolicy, this.replayPolicy);
        }

        public long checksum() {
            //
            Checksum crc32 = new CRC32();
            byte[] bytes = toString().getBytes(StandardCharsets.UTF_8);
            crc32.update(bytes, 0, bytes.length);

            return crc32.getValue();
        }
    }
}
