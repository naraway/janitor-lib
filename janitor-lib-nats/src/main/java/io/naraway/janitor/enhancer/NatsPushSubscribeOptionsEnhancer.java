package io.naraway.janitor.enhancer;

import io.nats.client.PushSubscribeOptions;

@FunctionalInterface
public interface NatsPushSubscribeOptionsEnhancer extends JanitorNatsConfigEnhancer<PushSubscribeOptions> {
    //
}
