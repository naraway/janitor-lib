/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.context;

import io.naraway.accent.domain.trail.TrailContext;
import io.naraway.accent.domain.trail.TrailInfo;
import org.springframework.core.task.TaskDecorator;

public class ContextCopyTaskDecorator implements TaskDecorator {
    //
    @Override
    public Runnable decorate(Runnable runnable) {
        //
        TrailInfo trailInfo = TrailContext.get();

        return () -> {
            try {
                TrailContext.set(trailInfo);
                runnable.run();
            } finally {
                TrailContext.clear();
            }
        };
    }
}
