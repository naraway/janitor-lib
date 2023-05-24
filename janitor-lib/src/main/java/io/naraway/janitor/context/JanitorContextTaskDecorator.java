package io.naraway.janitor.context;

import org.springframework.core.task.TaskDecorator;

public class JanitorContextTaskDecorator implements TaskDecorator {
    //
    @Override
    public Runnable decorate(Runnable runnable) {
        //
        JanitorStreamEvent event = JanitorContext.get();

        return () -> {
            try {
                JanitorContext.set(event);
                runnable.run();
            } finally {
                JanitorContext.clear();
            }
        };
    }
}