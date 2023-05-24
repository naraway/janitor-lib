/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.autoconfigure

import io.naraway.janitor.relay.LocalEventRelay
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import spock.lang.Specification
import spock.lang.Subject

class LocalAutoConfigurationTest extends Specification {
    //
    @Subject
    LocalAutoConfiguration autoConfiguration

    def 'auto configuration should loaded'() {
        given:
        def contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JanitorAutoConfiguration.class,
                        LocalAutoConfiguration.class,
                ))
                .withPropertyValues(
                        "nara.janitor.mode=local",
                )

        when:
        def relay
        contextRunner.run(context -> {
            try {
                context.getBean(LocalEventRelay.class)
                relay = true
            } catch (Exception e) {
                relay = false
            }
        })

        then:
        relay == true
    }

    //
    def 'auto configuration should not be loaded'() {
        given:
        def contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JanitorAutoConfiguration.class,
                        LocalAutoConfiguration.class,
                ))
                .withPropertyValues(
                        "nara.janitor.mode=kafka",
                )

        when:
        def relay
        contextRunner.run(context -> {
            try {
                context.getBean(KafkaEventRelay.class)
                relay = true
            } catch (Exception e) {
                relay = false
            }
        })

        then:
        relay == false
    }
}
