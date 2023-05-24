package io.naraway.janitor.autoconfigure;

import io.naraway.janitor.converter.DefaultPayloadConverter;
import io.naraway.janitor.converter.PayloadConverter;
import io.naraway.janitor.relay.LocalEventRelay;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = JanitorAutoConfiguration.class)
@ConditionalOnProperty(prefix = "nara.janitor", name = "mode", havingValue = "local")
@EnableConfigurationProperties(JanitorProperties.class)
@RequiredArgsConstructor
public class LocalAutoConfiguration {
    //
    private final JanitorProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public LocalEventRelay localEventRelay() {
        //
        return new LocalEventRelay(this.properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayloadConverter payloadConverter() {
        //
        return new DefaultPayloadConverter();
    }
}
