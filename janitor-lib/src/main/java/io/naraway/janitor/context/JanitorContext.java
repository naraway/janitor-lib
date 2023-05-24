/*
 * COPYRIGHT (c) NEXTREE Inc. 2014
 * This software is the proprietary of NEXTREE Inc.
 * @since 2014. 6. 10.
 */

package io.naraway.janitor.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JanitorContext {
    //
    private static final ThreadLocal<JanitorStreamEvent> context = new ThreadLocal<>();

    public static JanitorStreamEvent get() {
        //
        if (!exists()) {
            setDefault();
        }

        return context.get();
    }

    public static void set(JanitorStreamEvent event) {
        //
        context.set(event);
    }

    public static void clear() {
        //
        context.remove();
    }

    public static void setDefault() {
        //
        context.set(JanitorStreamEvent.newInstance());
    }

    private static boolean exists() {
        //
        return context.get() != null;
    }
}