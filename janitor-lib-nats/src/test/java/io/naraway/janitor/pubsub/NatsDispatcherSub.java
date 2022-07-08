package io.naraway.janitor.pubsub;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;

import java.nio.charset.StandardCharsets;

public class NatsDispatcherSub {
    //
    public static void main(String[] args) throws Exception {
        Connection nc = Nats.connect();
        System.out.println("disp connected.");

        Dispatcher d = nc.createDispatcher(msg -> {
            String string = new String(msg.getData(), StandardCharsets.UTF_8);
            System.out.println("disp received : " + string);
        });
        d.subscribe("nats");
        System.out.println("disp subscribed.");
    }
}
