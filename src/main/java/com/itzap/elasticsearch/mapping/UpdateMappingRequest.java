package com.itzap.elasticsearch.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateMappingRequest {
    private final Map<String, TypedProperty> properties = new HashMap<>();

    public Map<String, TypedProperty> getProperties() {
        return properties;
    }

    public void putProperty(TypedProperty property) {
        this.properties.put(property.getName(), property);
    }

    public static UpdateMappingRequest fromFields(List<FieldProperties> fields) {
        UpdateMappingRequest request = new UpdateMappingRequest();

        if (fields == null || fields.isEmpty()) {
            return request;
        }

        for (FieldProperties field: fields) {
            request.putProperty(new TypedProperty(field.getName(), field.getType().getName()));
        }

        return request;
    }
}
