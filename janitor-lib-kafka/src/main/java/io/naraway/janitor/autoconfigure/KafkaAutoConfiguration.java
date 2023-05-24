/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.autoconfigure;

import io.naraway.janitor.converter.PayloadConverter;
import io.naraway.janitor.event.JanitorEventType;
import io.naraway.janitor.listener.KafkaMessageInitializer;
import io.naraway.janitor.relay.KafkaEventRelay;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoConfiguration(after = JanitorAutoConfiguration.class)
@ConditionalOnProperty(prefix = "nara.janitor", name = "mode", havingValue = "kafka")
@EnableConfigurationProperties(JanitorProperties.class)
@EnableKafka
@RequiredArgsConstructor
public class KafkaAutoConfiguration {
    //
    private final JanitorProperties properties;

    @Value("${nara.janitor.kafka.consumer.option.auto-offset-reset-config:earliest}")
    private String autoOffsetResetConfig;

    @Bean
    @ConditionalOnMissingBean
    public KafkaMessageInitializer kafkaMessageInitializer(
            JanitorProperties properties,
            ApplicationEventPublisher publisher,
            ConsumerFactory<String, String> consumerFactory,
            PayloadConverter converter) {
        //
        return new KafkaMessageInitializer(properties, publisher, consumerFactory, converter);
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaEventRelay kafkaEventRelay() {
        //
        return new KafkaEventRelay(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        //
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory();

        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(10);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setSyncCommits(true);

        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsumerFactory<String, String> consumerFactory() {
        //
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("java:S2275")
    public Map<String, Object> consumerConfigs() {
        //
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getServers());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getName());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig);
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);

        if (!CollectionUtils.isEmpty(properties.getKafkaConfigs())) {
            List<String> kafkaConfigs = properties.getKafkaConfigs();
            kafkaConfigs.forEach(kafkaConfig -> {
                String key = kafkaConfig.substring(0, kafkaConfig.indexOf('='));
                String value = kafkaConfig.substring(kafkaConfig.indexOf('=') + 1);
                config.put(key, value);
            });
        }

        return config;
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaAdmin kafkaAdmin() {
        //
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getServers());
        return new KafkaAdmin(configs);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "nara.janitor.event.data", name = "enabled", havingValue = "true", matchIfMissing = true)
    public NewTopic dataEventTopic() {
        //
        String name = String.format("%s-%s", properties.getName(), JanitorEventType.Data.postfix());
        return TopicBuilder.name(name).partitions(properties.getEvent().getData().getPartition()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "nara.janitor.event.domain", name = "enabled", havingValue = "true", matchIfMissing = true)
    public NewTopic domainEventTopic() {
        //
        String name = String.format("%s-%s", properties.getName(), JanitorEventType.Domain.postfix());
        return TopicBuilder.name(name).partitions(properties.getEvent().getDomain().getPartition()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "nara.janitor.event.request", name = "enabled", havingValue = "true", matchIfMissing = true)
    public NewTopic requestEventTopic() {
        //
        String name = String.format("%s-%s", properties.getName(), JanitorEventType.Request.postfix());
        return TopicBuilder.name(name).partitions(properties.getEvent().getRequest().getPartition()).build();
    }
}