/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.relay;

public interface RequestRelay {
    //
    String request(String subjectId, byte[] data);
}