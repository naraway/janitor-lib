/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Conditional(NatsModeCondition.class)
@Configuration
@ComponentScan(basePackages = "io.naraway.janitor")
public class NatsConfiguration {
    //
    @Value("${nara.janitor.bootstrap-address:}")
    private String broker;
    @Value("${nara.janitor.subscriptions:}")
    private String[] topics;
    @Value("${nara.janitor.id}")
    private String serviceName;
    @Value(("${nara.janitor.replica:3}"))
    private short replica;

    public static String SUBJECT_POSTFIX_DATA = "data";
    public static String SUBJECT_POSTFIX_DOMAIN = "domain";
    public static String SUBJECT_POSTFIX_REQUEST = "request";
}
