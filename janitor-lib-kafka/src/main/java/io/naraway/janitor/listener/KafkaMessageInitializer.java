package io.naraway.janitor.listener;

import io.naraway.janitor.autoconfigure.JanitorProperties;
import io.naraway.janitor.converter.PayloadConverter;
import io.naraway.janitor.event.JanitorEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
public class KafkaMessageInitializer {
    //
    private final JanitorProperties properties;
    private final ApplicationEventPublisher publisher;
    private final ConsumerFactory<String, String> consumerFactory;
    private final PayloadConverter converter;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        //
        if (CollectionUtils.isEmpty(this.properties.getSubscriptions())) {
            return;
        }

        for (String subscription : this.properties.getSubscriptions()) {
            //
            if(subscription.startsWith("^")) {
                String serviceGroupName = subscription.substring(0, subscription.lastIndexOf('.'));
                serviceGroupName = serviceGroupName.replace("^", "");
                String subject = serviceGroupName.concat(".").concat(subscription.substring(subscription.lastIndexOf('.') + 1, subscription.length()));
                KafkaMessageListenerContainer domainListener = streamMessageListenerContainer(subject);
                domainListener.start();
            } else {
                KafkaMessageListenerContainer domainListener = streamMessageListenerContainer(
                        String.format("%s-%s", subscription, JanitorEventType.Domain.postfix()));
                domainListener.start();
            }
        }
    }

    private KafkaMessageListenerContainer streamMessageListenerContainer(String subscription) {
        //
        ContainerProperties containerProperties = new ContainerProperties(subscription);
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        containerProperties.setMessageListener(new KafkaMessageListener(this.publisher, this.converter));
        KafkaMessageListenerContainer<String, String> listenerContainer = new KafkaMessageListenerContainer<>(
                this.consumerFactory, containerProperties);
        listenerContainer.setAutoStartup(true);
        listenerContainer.setBeanName(subscription + "StreamListener");

        return listenerContainer;
    }
}