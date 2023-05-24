/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.connection;

import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.autoconfigure.JanitorProperties;
import io.naraway.janitor.enhancer.JetStreamConfigurationEnhancer;
import io.nats.client.*;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JetStreamConnection implements ConnectionListener {
    //
    private final JanitorProperties properties;
    private final JetStreamConfigurationEnhancer configurationEnhancer;

    private Connection connection;

    @PostConstruct
    private void initialize() throws JetStreamApiException, IOException {
        //
        connect();
        configureDefaultJetStream(this.connection, this.properties.getName());
        configureNamedJetStream(this.connection, this.properties.getSubscriptions());
    }

    @PreDestroy
    private void destroy() throws InterruptedException {
        //
        this.connection.close();
    }

    @SuppressWarnings("java:S2142")
    public void connect() {
        //
        if (this.connection == null || this.connection.getStatus() == Connection.Status.DISCONNECTED) {
            // iterate over the array of servers and add them to the  connection builder.
            Options.Builder connectionBuilder = new Options.Builder().connectionListener(this);
            for (String server : this.properties.getServers()) {
                String natsServer = "nats://" + server;
                log.info("Adding Nats server = {}", natsServer);
                connectionBuilder.server(natsServer).maxReconnects(-1);
            }

            try {
                this.connection = Nats.connect(connectionBuilder.build());
            } catch (IOException | InterruptedException e) {
                log.warn("Server connection is failed", e);
            }
        }
    }

    public Connection getConnection() {
        //
        if (this.connection == null) {
            connect();
        }
        return this.connection;
    }

    @Override
    @SuppressWarnings("java:S131")
    public void connectionEvent(Connection conn, Events event) {
        //
        log.info("Connection event = {}", event);

        switch (event) {
            case CONNECTED:
                log.info("Connected");
                break;
            case DISCONNECTED:
                log.warn("Disconnected, try reconnect...");
                try {
                    this.connection = null;
                    getConnection();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                break;
            case RECONNECTED:
                log.info("Reconnected");
                break;
            case RESUBSCRIBED:
                log.info("Resubscribed");
                break;
        }
    }

    private void configureDefaultJetStream(Connection connection, String applicationName) throws IOException, JetStreamApiException {
        //
        log.info("Create Default JetStream...");

        String serviceGroupName = applicationName.substring(0, applicationName.lastIndexOf('.'));
        String streamName = serviceGroupName.replace('.', '_');
        String subject = serviceGroupName.concat(".>");
        configureJetStream(connection, streamName, subject);
    }

    private void configureNamedJetStream(Connection connection, List<String> subscriptions) throws IOException, JetStreamApiException {
        //
        log.info("Create Named JetStream...");

        for (String subscription : subscriptions) {
            if(subscription.startsWith("^")) {
                String serviceGroupName = subscription.substring(0, subscription.lastIndexOf('.'));
                serviceGroupName = serviceGroupName.replace("^", "");
                String streamName = serviceGroupName.replace('.', '_');
                String subject = serviceGroupName.concat(".>");
                configureJetStream(connection, streamName, subject);
            }
        }
    }

    private void configureJetStream(Connection connection, String streamName, String subject) throws IOException, JetStreamApiException {
        //
        log.info("Create JetStream...");

        JetStreamManagement jsm = connection.jetStreamManagement();

        // set up jetStream
        boolean exists = jsm.getStreamNames().contains(streamName);

        if (exists) {
            // enhance stream configuration from predefinedConfiguration
            StreamConfiguration defaultStreamConfiguration =
                    jsm.getStreamInfo(streamName).getConfiguration();
            StreamConfiguration enhancedStreamConfiguration =
                    configurationEnhancer.enhance(defaultStreamConfiguration);
            log.debug("Enhanced [from PredefinedConfiguration] StreamConfiguration :\n{}",
                    JsonUtil.toPrettyJson(enhancedStreamConfiguration));

            StreamInfo streamInfo = jsm.updateStream(enhancedStreamConfiguration);
            log.debug("JetStream is updated.\n{}", JsonUtil.toPrettyJson(streamInfo));
        } else {
            // enhance stream configuration from defaultConfiguration
            StreamConfiguration configuration = StreamConfiguration.builder()
                    .name(streamName)
                    .subjects(subject)
                    .maxAge(Duration.ofHours(1))
                    .storageType(StorageType.File)
                    .build();

            StreamConfiguration enhancedStreamConfiguration = this.configurationEnhancer.enhance(configuration);
            log.debug("Enhanced [from DefaultConfiguration] StreamConfiguration :\n{}",
                    JsonUtil.toPrettyJson(enhancedStreamConfiguration));

            StreamInfo streamInfo = jsm.addStream(enhancedStreamConfiguration);
            log.debug("JetStream is created.\n{}", JsonUtil.toPrettyJson(streamInfo));
        }

        log.info("{} JetStream is set up", streamName);
    }

}
