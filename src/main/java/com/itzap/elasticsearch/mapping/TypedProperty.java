package com.itzap.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itzap.common.Named;

public class TypedProperty implements Named {
    @JsonIgnore
    private final String name;
    private final String type;

    public TypedProperty(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getType() {
        return type;
    }
}
