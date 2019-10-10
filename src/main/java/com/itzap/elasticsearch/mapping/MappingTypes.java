package com.itzap.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itzap.common.Named;

public enum MappingTypes implements Named {
    NESTED("nested"),
    TEXT("text"),
    KEYWORD("keyword"),
    DATE("date"),
    LONG("long"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    IP("ip");

    private final String name;

    MappingTypes(String name) {
        this.name = name;
    }

    @JsonValue
    @Override
    public String getName() {
        return this.name;
    }

    @JsonCreator
    public static MappingTypes fromString(String val) {
        for (MappingTypes type: values()) {
            if (type.getName().equalsIgnoreCase(val) ||
                    type.name().equalsIgnoreCase(val)) {
                return type;
            }
        }

        return KEYWORD;
    }
}
