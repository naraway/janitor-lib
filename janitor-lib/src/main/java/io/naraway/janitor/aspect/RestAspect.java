/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.aspect;

import io.naraway.accent.domain.trail.*;
import io.naraway.accent.domain.trail.wrapper.StreamEvent;
import io.naraway.janitor.context.TrailContextController;
import io.naraway.janitor.proxy.Relay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class RestAspect {
    //
    private final Relay relay;

    @Value("${nara.janitor.id}")
    private String janitorId;

    @Around("withRestControllerClass() & onlyPostMappingMethod()")
    public Object aroundRestCall(ProceedingJoinPoint joinPoint) throws Throwable {
        //
        Object firstArgument = AspectHelper.extractFirstArgument(joinPoint);

        // Skip trail for non-domain message arguments rest mapping
        if (firstArgument == null || !TrailMessage.class.isAssignableFrom(firstArgument.getClass())) {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw throwable;
            }
        }

        TrailMessage message = AspectHelper.extractMessage(firstArgument);
        log.debug("message class = {}", message.getClass().getName());
        TrailContextController.setTrailInfo(message, janitorId);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            StreamEvent event = new StreamEvent(message);
            relay.publishAfterRollback(event);
            TrailContextController.clearTrailInfo();
            throw throwable;
        }

        if (message.getTrailInfo().getRequestTime() > 0) {
            message.getTrailInfo().calculateWaitingTime();
        }

        TrailMessageType payloadType = null;
        if (CommandRequest.class.isAssignableFrom(message.getClass())) {
            payloadType = TrailMessageType.CommandRequest;
        } else if (QueryRequest.class.isAssignableFrom(message.getClass())) {
            payloadType = TrailMessageType.QueryRequest;
        } else if (DynamicQueryRequest.class.isAssignableFrom(message.getClass())) {
            payloadType = TrailMessageType.DynamicQueryRequest;
        } else if (ClientRequest.class.isAssignableFrom(message.getClass())) {
            payloadType = TrailMessageType.ClientRequest;
        }

        StreamEvent event = new StreamEvent(message);
        if (payloadType != null) {
            event.setPayloadType(payloadType.name());
        }
        relay.publish(event);
        TrailContextController.clearTrailInfo();

        return result;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    private void withRestControllerClass() {
        //
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    private void onlyPostMappingMethod() {
        //
    }
}
