package io.naraway.janitor.pubsub;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Subscription;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class NatsSub {
    //
    public static void main(String[] args) throws Exception {
        //
        try (Connection nc = Nats.connect()) {
            System.out.println("sub connected.");

            Subscription sub = nc.subscribe("nats");

            System.out.println("sub listening...");
            Message msg = sub.nextMessage(Duration.ofMillis(300000));
            String string = new String(msg.getData(), StandardCharsets.UTF_8);
            System.out.println("sub received : " + string);
        }
        System.out.println("sub closed.");
    }
}
