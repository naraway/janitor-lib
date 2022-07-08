/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.configuration;

import io.naraway.accent.util.json.JsonUtil;
import io.nats.client.*;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static io.naraway.janitor.configuration.NatsConfiguration.*;

@Slf4j
@Component
@Conditional(NatsModeCondition.class)
public class JetStreamConnection implements ConnectionListener {
    //
    @Value("${nara.janitor.bootstrap-address:}")
    private String[] servers;
    @Value("${nara.janitor.subscriptions:}")
    private String[] topics;
    @Value("${nara.janitor.id}")
    private String serviceName;
    @Value(("${nara.janitor.replica:3}"))
    private short replica;
    private Connection connection;
    private JetStreamManagement jetStreamManagement;

    public Connection genConnection() {
        if ((connection == null) || (connection.getStatus() == Connection.Status.DISCONNECTED)) {

            Options.Builder connectionBuilder = new Options.Builder().connectionListener(this);

            /* iterate over the array of servers and add them to the  connection builder.
             */
            for (String server : servers) {
                String natsServer = "nats://" + server;
                log.info("adding nats server:" + natsServer);
                connectionBuilder.server(natsServer).maxReconnects(-1);
            }

            try {
                connection = Nats.connect(connectionBuilder.build());
            } catch (IOException | InterruptedException ex) {
                log.error(ex.getMessage());
            }
        }

        return connection;
    }

    public Connection getConnection() {
        //
        if (connection == null) {
            genConnection();
        }
        return connection;
    }

    public JetStream getJetStream() {
        //
        JetStream jetStream = null;
        try {
            if (connection == null) {
                genConnection();
                jetStream = connection.jetStream();
            }

            jetStream = connection.jetStream();
        } catch (IOException exception) {
            log.info("Cannot get JetStream");
            exception.printStackTrace();
        }

        return jetStream;
    }

    @Override
    public void connectionEvent(Connection conn, Events event) {
        //
        log.info("Connection Event:" + event);
        switch (event) {
            case CONNECTED:
                log.info("CONNECTED to NATS!");
                break;
            case DISCONNECTED:
                log.warn("RECONNECTED to NATS!");
                try {
                    connection = null;
                    getConnection();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
                break;
            case RECONNECTED:
                log.info("RECONNECTED to NATS!");
                break;
            case RESUBSCRIBED:
                log.info("RESUBSCRIBED!");
                break;

        }
    }

    private void createStream(Connection connection, String subject) throws IOException {
        //
        log.info("creating stream");
        String streamName = subject.replace('.', '_');
        jetStreamManagement = connection.jetStreamManagement();
        try {
            StreamConfiguration streamConfig = StreamConfiguration.builder()
                    .name(streamName)
                    .storageType(StorageType.File)
                    .subjects(subject)
                    .build();

            StreamInfo streamInfo = jetStreamManagement.addStream(streamConfig);
            log.info("Created Stream\n {}", JsonUtil.toPrettyJson(streamInfo));
        } catch (JetStreamApiException ex) {
            log.info("Stream exception");
            ex.printStackTrace();
        }

        log.info("{} stream is created", streamName);
    }

    @PostConstruct
    public void init() {
        //
        connection = genConnection();
        try {
            createStream(connection, String.format("%s-%s", serviceName, SUBJECT_POSTFIX_DATA));
            createStream(connection, String.format("%s-%s", serviceName, SUBJECT_POSTFIX_DOMAIN));
            createStream(connection, String.format("%s-%s", serviceName, SUBJECT_POSTFIX_REQUEST));
        } catch (IOException exception) {
            log.info("fail to create stream ");
            exception.printStackTrace();
        }
    }
}
