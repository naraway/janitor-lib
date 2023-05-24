/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.autoconfigure

import io.naraway.janitor.listener.KafkaMessageInitializer
import io.naraway.janitor.relay.KafkaEventRelay
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject

class KafkaAutoConfigurationTest extends Specification {
    //
    @Subject
    KafkaAutoConfiguration autoConfiguration

    //
    @Ignore('no active embed severs')
    def 'auto configuration should loaded'() {
        given:
        def contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JanitorAutoConfiguration.class,
                        KafkaAutoConfiguration.class,
                ))
                .withPropertyValues(
                        "nara.janitor.mode=kafka",
                        "nara.janitor.servers=127.0.0.1:9092",
                )

        when:
        def properties
        def registrar
        def relay
        contextRunner.run(context -> {
            try {
                context.getBean(JanitorProperties.class)
                properties = true
            } catch (Exception e) {
                properties = false
            }
            try {
                context.getBean(KafkaMessageInitializer.class)
                registrar = true
            } catch (Exception e) {
                registrar = false
            }
            try {
                context.getBean(KafkaEventRelay.class)
                relay = true
            } catch (Exception e) {
                relay = false
            }
        })

        then:
        properties == true
        registrar == true
        relay == true
    }

    //
    def 'auto configuration should not be loaded'() {
        given:
        def contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JanitorAutoConfiguration.class,
                        KafkaAutoConfiguration.class,
                ))
                .withPropertyValues(
                        "nara.janitor.mode=local",
                )

        when:
        def registrar
        def relay
        contextRunner.run(context -> {
            try {
                context.getBean(KafkaMessageInitializer.class)
                registrar = true
            } catch (Exception e) {
                registrar = false
            }
            try {
                context.getBean(KafkaEventRelay.class)
                relay = true
            } catch (Exception e) {
                relay = false
            }
        })

        then:
        registrar == false
        relay == false
    }
}
