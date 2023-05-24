package io.naraway.janitor.converter;

import io.naraway.accent.util.json.JsonUtil;
import io.naraway.janitor.context.JanitorStreamEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultPayloadConverter implements PayloadConverter {
    //
    @Override
    @SuppressWarnings("unchecked")
    public Object convert(JanitorStreamEvent message) {
        //
        Class clazz = null;

        try {
            clazz = Class.forName(message.getPayloadType());
        } catch (ClassNotFoundException e) {
            log.info(e.getMessage());
            throw new PayloadConvertException("No suitable class for payload converter");
        }

        return JsonUtil.fromJson(message.getPayload(), clazz);
    }
}