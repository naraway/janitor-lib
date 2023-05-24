/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.autoconfigure;

import io.naraway.janitor.converter.DefaultPayloadConverter;
import io.naraway.janitor.converter.PayloadConverter;
import io.naraway.janitor.proxy.EventProxy;
import io.naraway.janitor.proxy.MessageProxy;
import io.naraway.janitor.proxy.RequestProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@AutoConfiguration
@EnableConfigurationProperties(JanitorProperties.class)
@ComponentScan("io.naraway.janitor.endpoint")
@RequiredArgsConstructor
public class JanitorAutoConfiguration {
    //
    @Bean
    @ConditionalOnMissingBean
    public EventProxy eventProxy(ApplicationEventPublisher publisher) {
        //
        return new EventProxy(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageProxy messageProxy() {
        //
        return new MessageProxy();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestProxy requestProxy() {
        //
        return new RequestProxy();
    }

    @Bean
    @ConditionalOnMissingBean
    public PayloadConverter payloadConverter() {
        //
        return new DefaultPayloadConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        //
        return new PropertySourcesPlaceholderConfigurer();
    }
}
