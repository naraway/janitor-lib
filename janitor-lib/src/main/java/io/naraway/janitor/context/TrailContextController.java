/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.context;

import io.naraway.accent.domain.trail.CommandRequest;
import io.naraway.accent.domain.trail.TrailContext;
import io.naraway.accent.domain.trail.TrailInfo;
import io.naraway.accent.domain.trail.TrailMessage;

public class TrailContextController {
    //

    // ThreadLocal 의 TrailInfo 를 복사 하여 사용.
    // Message 만 변경
    public static void injectTrailInfo(TrailMessage message) {
        //
        TrailInfo trailInfo = TrailContext.get();

        // add default trail info (in case of junit test)
        if (trailInfo == null) {
            TrailContextController.setTrailInfo(new CommandRequest() {
            }, "unknown");
            trailInfo = TrailContext.get();
        }

        TrailInfo eventTrailInfo = trailInfo.copy();
        eventTrailInfo.setMessage(message.getClass().getSimpleName());
        eventTrailInfo.setMessageId(message.getId());
        message.setTrailInfo(eventTrailInfo);
    }

    // 새로 생성 하거나 / parent TrailInfo  부터 생성하여 ThreadLocal 에 저장
    // 서비스로 들어오는 진입점에서 사용하는 매서드
    public static void setTrailInfo(TrailMessage message, String service) {
        //
        if (message.getTrailInfo() == null) {
            TrailInfo trailInfo = new TrailInfo(service, message);
            message.setTrailInfo(trailInfo);
        }

        TrailInfo trailInfo = new TrailInfo(message.getTrailInfo(), service, message);
        TrailContext.set(trailInfo);
    }

    public static void setTrailInfo(TrailInfo trailInfo) {
        //
        TrailContext.set(trailInfo);
    }

    public static void clearTrailInfo() {
        //
        TrailContext.clear();
    }
}
