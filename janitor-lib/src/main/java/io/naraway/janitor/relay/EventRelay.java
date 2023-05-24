/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.relay;

import io.naraway.janitor.context.JanitorStreamEvent;

public interface EventRelay {
    //
    void publish(JanitorStreamEvent event);
    void publishAfterRollback(JanitorStreamEvent event);
}
