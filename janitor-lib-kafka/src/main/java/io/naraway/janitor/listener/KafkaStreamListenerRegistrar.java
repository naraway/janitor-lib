/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.listener;

import io.naraway.janitor.configuration.KafkaModeCondition;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.naraway.janitor.configuration.KafkaConfiguration.TOPIC_POSTFIX_DATA;
import static io.naraway.janitor.configuration.KafkaConfiguration.TOPIC_POSTFIX_DOMAIN;

@Component
@Conditional(KafkaModeCondition.class)
public class KafkaStreamListenerRegistrar implements BeanFactoryAware {
    private final KafkaTemplate kafkaTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ConsumerFactory<String, String> consumerFactory;
    //
    private BeanFactory beanFactory;
    @Value("${nara.janitor.bootstrap-address:}")
    private String broker;
    @Value("${nara.janitor.subscriptions:}")
    private String[] topics;
    @Value("${nara.janitor.id}")
    private String serviceName;
    @Value("${cryptography.data.fields:}")
    private List<String> dataFields;

    public KafkaStreamListenerRegistrar(KafkaTemplate kafkaTemplate,
                                        ApplicationEventPublisher eventPublisher,
                                        ConsumerFactory<String, String> consumerFactory) {
        //
        this.kafkaTemplate = kafkaTemplate;
        this.eventPublisher = eventPublisher;
        this.consumerFactory = consumerFactory;
    }

    @PostConstruct
    public void initialize() {
        //
    }

    @EventListener(ApplicationReadyEvent.class)
    public void on() {
        //
        KafkaMessageListenerContainer myDataEventListener = streamMessageListenerContainer(
                String.format("%s-%s", serviceName, TOPIC_POSTFIX_DATA));
        myDataEventListener.start();

        if (topics == null) {
            return;
        }

        for (String topic : topics) {
            //
            KafkaMessageListenerContainer domainListener = streamMessageListenerContainer(
                    String.format("%s-%s", topic, TOPIC_POSTFIX_DOMAIN));
            domainListener.start();
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        //
        this.beanFactory = beanFactory;
    }

    private KafkaMessageListenerContainer streamMessageListenerContainer(String topic) {
        //
        ContainerProperties containerProperties = new ContainerProperties(topic);
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        containerProperties.setMessageListener(new KafkaStreamListener(serviceName, topic, dataFields, kafkaTemplate, eventPublisher));

        KafkaMessageListenerContainer<String, String> listenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        listenerContainer.setAutoStartup(true);
        listenerContainer.setBeanName(topic + "StreamListener");

        return listenerContainer;
    }

    private Map<String, Object> consumerProperties() {
        //
        Map<String, Object> properties = new HashMap<>();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.broker);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, this.serviceName);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);

        return properties;
    }
}
