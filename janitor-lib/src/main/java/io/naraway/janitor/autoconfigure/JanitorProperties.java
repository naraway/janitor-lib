/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.autoconfigure;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "nara.janitor")
public class JanitorProperties {
    //
    @Value("io.naraway.${spring.application.name:drama}")
    private String applicationName;

    private JanitorModeType mode;
    private List<String> servers;
    private String id;
    private List<String> subscriptions;
    private JanitorEventProperties event;
    private List<String> kafkaConfigs;

    public JanitorProperties() {
        //
        this.mode = JanitorModeType.LOCAL;
        this.event = new JanitorEventProperties();
        this.subscriptions = new ArrayList<>();
        this.kafkaConfigs = new ArrayList<>();
    }

    public String getName() {
        //
        return StringUtils.hasText(this.id) ? this.id : this.applicationName;
    }

    public enum JanitorModeType {
        //
        LOCAL("local"),
        KAFKA("kafka"),
        NATS("nats");

        private final String mode;

        JanitorModeType(String mode) {
            //
            this.mode = mode;
        }

        public String mode() {
            //
            return this.mode;
        }
    }

    @Data
    public static class JanitorEventProperties {
        //
        private EventProperties data;
        private EventProperties domain;
        private EventProperties request;
        private EventProperties namedChannel;

        public JanitorEventProperties() {
            //
            this.data = new EventProperties();
            this.domain = new EventProperties();
            this.request = new EventProperties();
            this.namedChannel = new EventProperties();
        }
    }

    @Data
    public static class EventProperties {
        //
        private boolean enabled;
        private int partition;

        public EventProperties() {
            //
            this.enabled = true;
            this.partition = 2;
        }
    }
}
