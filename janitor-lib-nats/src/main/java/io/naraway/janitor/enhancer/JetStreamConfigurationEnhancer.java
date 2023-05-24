package io.naraway.janitor.enhancer;

import io.nats.client.api.StreamConfiguration;

@FunctionalInterface
public interface JetStreamConfigurationEnhancer extends JanitorNatsConfigEnhancer<StreamConfiguration> {
    //
}
