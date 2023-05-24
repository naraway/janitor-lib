/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.event;

public enum JanitorEventType {
    //
    Data("data"),
    Domain("domain"),
    Request("request"),
    NamedChannel("named-channel");

    private final String postfix;

    JanitorEventType(String postfix) {
        //
        this.postfix = postfix;
    }

    public String postfix() {
        //
        return this.postfix;
    }
}
