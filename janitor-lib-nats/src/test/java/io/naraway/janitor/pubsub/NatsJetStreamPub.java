package io.naraway.janitor.pubsub;

import io.naraway.accent.util.json.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamManagement;
import io.nats.client.Nats;
import io.nats.client.api.PublishAck;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;

import java.nio.charset.StandardCharsets;

public class NatsJetStreamPub {
    //
    public static void main(String[] args) throws Exception {
        //
        Connection nc = Nats.connect();
        // Create stream
        JetStreamManagement jsm = nc.jetStreamManagement();
        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .name("file_stream1")
                .subjects("subject1")
                .storageType(StorageType.File)
                .build();

        StreamInfo streamInfo = jsm.addStream(streamConfig);
        System.out.println(JsonUtil.toPrettyJson(streamInfo));

        //
        JetStream js = nc.jetStream();
        System.out.println("JetStream pub connect.");

        PublishAck ack = js.publish("subject1", "hello1".getBytes(StandardCharsets.UTF_8));
        System.out.println("ack1: " + JsonUtil.toPrettyJson(ack));

        ack = js.publish("subject1", "hello2".getBytes(StandardCharsets.UTF_8));
        System.out.println("ack2: " + JsonUtil.toPrettyJson(ack));

        nc.close();
        System.out.println("Connect close.");
    }
}
