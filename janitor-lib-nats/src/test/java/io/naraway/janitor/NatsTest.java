package io.naraway.janitor;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Subscription;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;


class NatsTest {
    //
    private static final String DEFAULT_URL = "nats://localhost:4222";

    @Test
    @Disabled("Local test only")
    @SuppressWarnings({"java:S2925", "java:S2699"})
    void test() throws Exception {
        Connection nc = initConnection();
        System.out.println("connect.");
        Thread.sleep(1000);

        //
        nc.publish("subject", "hello world".getBytes(StandardCharsets.UTF_8));
        System.out.println("publish.");
        Thread.sleep(1000);

        //
        Subscription sub = nc.subscribe("subject");
        Message message = sub.nextMessage(Duration.ofMillis(500));
        System.out.println("message:" + message);
        String response = new String(message.getData(), StandardCharsets.UTF_8);
        System.out.println("sub:" + response);
    }

    private Connection initConnection() throws Exception {
//        Options options = new Options.Builder()
//                .errorListener(ex -> System.out.println("connection exception: " + ex))
//                .build();

        return Nats.connect();
    }
}
