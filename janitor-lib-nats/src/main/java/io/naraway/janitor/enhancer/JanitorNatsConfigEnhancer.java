package io.naraway.janitor.enhancer;

@FunctionalInterface
public interface JanitorNatsConfigEnhancer<T> {
    T enhance(T defaultConfiguration);
}
