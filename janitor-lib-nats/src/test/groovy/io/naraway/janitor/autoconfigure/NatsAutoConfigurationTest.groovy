/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.autoconfigure

import io.naraway.janitor.connection.JetStreamConnection
import io.naraway.janitor.listener.NatsListenerInitializer
import io.naraway.janitor.relay.NatsEventRelay
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject

class NatsAutoConfigurationTest extends Specification {
    //
    @Subject
    NatsAutoConfiguration autoConfiguration

    //
    @Ignore('no active embed severs')
    def 'auto configuration should loaded'() {
        given:
        def contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JanitorAutoConfiguration.class,
                        NatsAutoConfiguration.class,
                ))
                .withPropertyValues(
                        "nara.janitor.mode=nats",
                        "nara.janitor.servers=127.0.0.1:4222",
                )

        when:
        def properties
        def registrar
        def relay
        def connection
        contextRunner.run(context -> {
            try {
                context.getBean(JanitorProperties.class)
                properties = true
            } catch (Exception e) {
                properties = false
            }
            try {
                context.getBean(NatsListenerInitializer.class)
                registrar = true
            } catch (Exception e) {
                registrar = false
            }
            try {
                context.getBean(NatsEventRelay.class)
                relay = true
            } catch (Exception e) {
                relay = false
            }
            try {
                context.getBean(JetStreamConnection.class)
                connection = true
            } catch (Exception e) {
                connection = false
            }
        })

        then:
        properties == true
        registrar == true
        relay == true
        connection == true
    }

    //
    def 'auto configuration should not be loaded'() {
        given:
        def contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JanitorAutoConfiguration.class,
                        NatsAutoConfiguration.class,
                ))
                .withPropertyValues(
                        "nara.janitor.mode=local",
                )

        when:
        def registrar
        def relay
        def connection
        contextRunner.run(context -> {
            try {
                context.getBean(NatsListenerInitializer.class)
                registrar = true
            } catch (Exception e) {
                registrar = false
            }
            try {
                context.getBean(NatsEventRelay.class)
                relay = true
            } catch (Exception e) {
                relay = false
            }
            try {
                context.getBean(JetStreamConnection.class)
                connection = true
            } catch (Exception e) {
                connection = false
            }
        })

        then:
        registrar == false
        relay == false
        connection == false
    }
}
