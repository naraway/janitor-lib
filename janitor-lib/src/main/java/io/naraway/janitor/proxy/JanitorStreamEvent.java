/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.proxy;

import io.naraway.accent.domain.trail.TrailMessage;
import io.naraway.accent.domain.trail.wrapper.StreamEvent;
import io.naraway.accent.util.json.JsonUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JanitorStreamEvent extends StreamEvent {
    //
    private String routingKey;

    public JanitorStreamEvent(TrailMessage trailMessage) {
        //
        super(trailMessage);
    }

    public String toString() {
        //
        return toJson();
    }

    public static JanitorStreamEvent fromJson(String json) {
        //
        return JsonUtil.fromJson(json, JanitorStreamEvent.class);
    }
}
