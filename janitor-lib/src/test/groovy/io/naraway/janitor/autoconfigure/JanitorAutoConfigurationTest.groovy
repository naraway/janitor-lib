/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class JanitorAutoConfigurationTest extends Specification {
    //
    @Subject
    JanitorAutoConfiguration autoConfiguration
    @Shared
    ApplicationContextRunner contextRunner

    //
    def setup() {
        contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JanitorAutoConfiguration.class))
                .withPropertyValues("spring.application.name=drama")
    }

    //
    def 'auto configuration should loaded'() {
        when:
        def properties
        contextRunner.run(context -> {
            properties = context.getBean(JanitorProperties.class)
        })

        then:
        println(properties)
        properties != null
        properties.applicationName == 'io.naraway.drama'
        properties.mode == JanitorProperties.JanitorModeType.LOCAL
    }
}
