/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(KafkaModeCondition.class)
@Getter
@Setter
@Component
@ConfigurationProperties(value = "nara.janitor")
public class KafkaProperties {
    //
    private String bootstrapAddress;
}
