/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.relay;

import io.naraway.janitor.context.JanitorStreamEvent;

public interface MessageRelay {
    //
    void publish(String reployTo, JanitorStreamEvent event);
}