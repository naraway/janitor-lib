package io.naraway.janitor.converter;

import io.naraway.janitor.context.JanitorStreamEvent;

public interface PayloadConverter {
    //
    Object convert(JanitorStreamEvent message);
}