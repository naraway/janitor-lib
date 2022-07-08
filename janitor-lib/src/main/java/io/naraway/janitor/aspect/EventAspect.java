/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.aspect;

import io.naraway.accent.domain.trail.TrailContext;
import io.naraway.accent.domain.trail.TrailMessage;
import io.naraway.janitor.context.TrailContextController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class EventAspect {
    //
    @Value("${nara.janitor.id}")
    private String janitorId;

    @Around("@annotation(io.naraway.janitor.event.EventHandler) || @annotation(io.naraway.janitor.aspect.annotation.EventHandler)")
    public Object aroundEventListenerMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        //
        Object argument = AspectHelper.extractFirstArgument(joinPoint);
        TrailMessage message = AspectHelper.extractMessage(argument);
        TrailContextController.setTrailInfo(message, janitorId);
        log.debug("TrailContext = {}", TrailContext.get());

        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }

        return result;
    }

}
