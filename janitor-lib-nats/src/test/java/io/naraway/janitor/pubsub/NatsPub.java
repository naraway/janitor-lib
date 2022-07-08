package io.naraway.janitor.pubsub;

import io.nats.client.Connection;
import io.nats.client.Nats;

import java.nio.charset.StandardCharsets;

public class NatsPub {
    //
    public static void main(String[] args) throws Exception {
        //
        try (Connection nc = Nats.connect()) {
            System.out.println("pub connected.");

            nc.publish("nats", "Hello world".getBytes(StandardCharsets.UTF_8));
            System.out.println("pub published.");

        }
        System.out.println("pub closed");
    }
}
