/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.proxy;

import io.naraway.accent.domain.trail.wrapper.StreamEvent;

public interface Relay {
    //
    void publish(StreamEvent event);
    void publishAfterRollback(StreamEvent event);
}
