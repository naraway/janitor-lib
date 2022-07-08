/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Optional;

public class NatsModeCondition implements Condition {
    //
    private static final String NAME = "nara.janitor.mode";
    private static final String MODE = "nats";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        //
        Environment environment = context.getEnvironment();
        String mode = environment.getProperty(NAME);

        return Optional.ofNullable(mode)
                .map(item -> item.equals(MODE))
                .orElse(false);
    }
}
