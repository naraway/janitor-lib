/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.listener;

import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.configuration.JetStreamConnection;
import io.naraway.janitor.configuration.NatsModeCondition;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static io.naraway.janitor.configuration.NatsConfiguration.SUBJECT_POSTFIX_DATA;
import static io.naraway.janitor.configuration.NatsConfiguration.SUBJECT_POSTFIX_DOMAIN;

@Slf4j
@Component
@Conditional(NatsModeCondition.class)
@RequiredArgsConstructor
public class NatsListenerRegister implements BeanFactoryAware {
    //
    private final JetStreamConnection jetStreamConnection;
    private final ApplicationEventPublisher eventPublisher;

    private BeanFactory beanFactory;
    @Value("${nara.janitor.bootstrap-address:}")
    private String broker;
    @Value("${nara.janitor.subscriptions:}")
    private String[] topics;
    @Value("${nara.janitor.id}")
    private String serviceName;
    @Value("${cryptography.data.fields:}")
    private List<String> dataFields;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // JetStream
        log.debug("Add nats subscribers");
        Connection nc = jetStreamConnection.getConnection();
        String subject = String.format("%s-%s", serviceName, SUBJECT_POSTFIX_DATA);
        String consumerName = serviceName.replace('.', '_');
        String queueGroup = String.format("%s_queue_group", consumerName);
        subscribe(nc, consumerName, subject, queueGroup);

        for (String topic : topics) {
            String domainSubject = String.format("%s-%s", topic, SUBJECT_POSTFIX_DOMAIN);
            subscribe(nc, consumerName, domainSubject, queueGroup);
        }
    }

    private void subscribe(Connection nc, String consumerName, String subject, String queueGroup) {
        //
        try {
            JetStream js = nc.jetStream();
            ConsumerConfiguration consumerConfiguration = ConsumerConfiguration.builder()
                    .durable(consumerName)
//                    .deliverSubject(subject)
                    .build();
            PushSubscribeOptions pushSubscribeOptions = PushSubscribeOptions.builder()
                    .configuration(consumerConfiguration)
                    .build();
            StreamEventMessageHandler messageHandler = new StreamEventMessageHandler(eventPublisher, subject);
            Dispatcher dispatcher = nc.createDispatcher();
//            JetStreamSubscription jsSub = js.subscribe(subject, queueGroup, dispatcher, messageHandler, true, pushSubscribeOptions);
            JetStreamSubscription jsSub = js.subscribe(subject, dispatcher, messageHandler, true);
            log.debug(JsonUtil.toPrettyJson(jsSub.getConsumerInfo()));
        } catch (IOException | JetStreamApiException exception ) {
            log.info("Subscribing {} is failed", subject);
            exception.printStackTrace();
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        //
        this.beanFactory = beanFactory;
    }
}
