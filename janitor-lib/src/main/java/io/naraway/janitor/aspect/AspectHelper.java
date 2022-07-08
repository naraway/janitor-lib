/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.aspect;

import io.naraway.accent.domain.trail.TrailMessage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;

@Slf4j
public class AspectHelper {
    //
    public static Object extractFirstArgument(JoinPoint joinPoint) {
        //
        Object[] args = joinPoint.getArgs();

        if (args == null || args.length <= 0) {
            return null;
        }
        return joinPoint.getArgs()[0];
    }

    public static TrailMessage extractMessage(Object argument) {
        //
        if (!(argument instanceof TrailMessage)) {
            throw new IllegalArgumentException("Not TrailMessage instance.");
        }

        return (TrailMessage) argument;
    }
}
