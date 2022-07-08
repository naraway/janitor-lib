package io.naraway.janitor.pubsub;

import io.naraway.accent.util.json.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.JetStreamManagement;
import io.nats.client.Nats;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;

import java.util.List;

public class NatsJetStreamInfo {
    //
    public static void main(String[] args) throws Exception {
        //
        try (Connection nc = Nats.connect()) {
            JetStreamManagement jsm = nc.jetStreamManagement();

            // add
            StreamConfiguration streamConfig = StreamConfiguration.builder()
                    .name("file_stream1")
                    .subjects("subject1")
                    .storageType(StorageType.File)
                    .build();

            StreamInfo streamInfo = jsm.addStream(streamConfig);

            //
            List<String> names = jsm.getStreamNames();
            System.out.println("names:" + names);
            for (String name : names) {
                System.out.println(name);
                StreamInfo info = jsm.getStreamInfo(name);
                System.out.println(JsonUtil.toPrettyJson(info));
            }
        }
    }
}
