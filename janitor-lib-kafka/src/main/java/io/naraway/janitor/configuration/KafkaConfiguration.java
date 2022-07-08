/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.configuration;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Conditional(KafkaModeCondition.class)
@Configuration
@ComponentScan(basePackages = "io.naraway.janitor")
@EnableKafka
public class KafkaConfiguration {
    public static String TOPIC_POSTFIX_DATA = "data";
    public static String TOPIC_POSTFIX_DOMAIN = "domain";
    public static String TOPIC_POSTFIX_REQUEST = "request";
    //
    @Value("${nara.janitor.bootstrap-address:}")
    private String broker;
    @Value("${nara.janitor.subscriptions:}")
    private String[] topics;
    @Value("${nara.janitor.id}")
    private String serviceName;

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
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
    public ConsumerFactory<String, String> consumerFactory() {
        //
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
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

    @Bean
    public KafkaAdmin kafkaAdmin() {
        //
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.broker);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic dataEventTopic() {
        //
        return TopicBuilder.name(String.format("%s-%s", this.serviceName, TOPIC_POSTFIX_DATA))
                .partitions(2)
                .build();
    }

    @Bean
    public NewTopic domainEventTopic() {
        //
        return TopicBuilder.name(String.format("%s-%s", this.serviceName, TOPIC_POSTFIX_DOMAIN))
                .partitions(2)
                .build();
    }

    @Bean
    public NewTopic requestEventTopic() {
        //
        return TopicBuilder.name(String.format("%s-%s", this.serviceName, TOPIC_POSTFIX_REQUEST))
                .partitions(2)
                .build();
    }

}
