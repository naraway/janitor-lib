package io.naraway.janitor.pubsub;

import io.nats.client.*;

import java.nio.charset.StandardCharsets;

public class NatsJetStreamSub {
    //
    public static void main(String[] args) throws Exception {
        //
        Connection nc = Nats.connect();
        JetStream js = nc.jetStream();
        System.out.println("JetStream sub connect.");

        Dispatcher disp = nc.createDispatcher();

        MessageHandler messageHandler = (msg) -> {
            String string = new String(msg.getData(), StandardCharsets.UTF_8);
            System.out.println("handler ==> " + string);
        };

        PushSubscribeOptions options = PushSubscribeOptions.builder()
                .durable("nats-jet-stream-sub")
                .build();
        JetStreamSubscription sub = js.subscribe("subject1", disp, messageHandler, true, options);

        System.out.println("JetStream sub listening...");

    }
}
