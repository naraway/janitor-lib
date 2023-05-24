package io.naraway.janitor.enhancer;

import io.nats.client.api.ConsumerConfiguration;

@FunctionalInterface
public interface NatsConsumerConfigurationEnhancer extends JanitorNatsConfigEnhancer<ConsumerConfiguration> {
    //
}
