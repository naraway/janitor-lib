package io.naraway.janitor.autoconfigure;

import io.naraway.janitor.connection.JetStreamConnection;
import io.naraway.janitor.converter.DefaultPayloadConverter;
import io.naraway.janitor.converter.PayloadConverter;
import io.naraway.janitor.enhancer.JetStreamConfigurationEnhancer;
import io.naraway.janitor.enhancer.NatsConsumerConfigurationEnhancer;
import io.naraway.janitor.enhancer.NatsPushSubscribeOptionsEnhancer;
import io.naraway.janitor.listener.NatsListenerInitializer;
import io.naraway.janitor.relay.NatsEventRelay;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = JanitorAutoConfiguration.class)
@ConditionalOnProperty(prefix = "nara.janitor", name = "mode", havingValue = "nats")
@EnableConfigurationProperties(JanitorProperties.class)
@RequiredArgsConstructor
public class NatsAutoConfiguration {
    //
    private final JanitorProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public NatsEventRelay natsEventRelay(JetStreamConnection jetStreamConnection) {
        //
        return new NatsEventRelay(properties, jetStreamConnection);
    }

    @Bean
    @ConditionalOnMissingBean
    public JetStreamConnection jetStreamConnection(
            JanitorProperties properties, JetStreamConfigurationEnhancer jetStreamConfigurationEnhancer) {
        //
        return new JetStreamConnection(properties, jetStreamConfigurationEnhancer);
    }

    @Bean
    @ConditionalOnMissingBean
    public NatsListenerInitializer natsListenerInitializer(
            JetStreamConnection connection,
            NatsConsumerConfigurationEnhancer consumerConfigEnhancer,
            NatsPushSubscribeOptionsEnhancer subscribeOptionsEnhancer,
            ApplicationEventPublisher publisher,
            PayloadConverter converter) {
        //
        return new NatsListenerInitializer(
                this.properties, connection, consumerConfigEnhancer, subscribeOptionsEnhancer, publisher, converter);
    }

    @Bean
    @ConditionalOnMissingBean
    public JetStreamConfigurationEnhancer jetStreamConfigurationEnhancer() {
        //
        return defaultStreamConfig -> defaultStreamConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public NatsConsumerConfigurationEnhancer natsConsumerConfigurationEnhancer() {
        //
        return defaultStreamConfig -> defaultStreamConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public NatsPushSubscribeOptionsEnhancer pushSubscribeOptionsEnhancer() {
        //
        return defaultConfiguration -> defaultConfiguration;
    }

    @Bean
    @ConditionalOnMissingBean
    public PayloadConverter payloadConverter() {
        //
        return new DefaultPayloadConverter();
    }
}
