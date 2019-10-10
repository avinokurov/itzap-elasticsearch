package com.itzap.elasticsearch.mapping;

import com.google.common.collect.ImmutableMap;
import com.itzap.common.BuilderInterface;

import java.util.HashMap;
import java.util.Map;

public class MappingBuilder implements BuilderInterface<Map<String, ?>> {
    private Map<String, FieldProperties> properties = new HashMap<>();

    public MappingBuilder setProperties(Map<String, FieldProperties> properties) {
        this.properties.putAll(properties);
        return this;
    }

    public MappingBuilder putProperties(String name, FieldProperties properties) {
        this.properties.put(name, properties);
        return this;
    }

    @Override
    public Map<String, ?> build() {
        return ImmutableMap.<String, Object>builder()
                .put("properties", properties)
                .build();
    }

    public static MappingBuilder builder() {
        return new MappingBuilder();
    }
}
