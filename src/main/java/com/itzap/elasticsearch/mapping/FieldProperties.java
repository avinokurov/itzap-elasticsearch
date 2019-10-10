package com.itzap.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.itzap.common.BuilderInterface;
import com.itzap.common.Named;

@JsonDeserialize(builder = FieldProperties.Builder.class)
public class FieldProperties implements Named {
    @JsonIgnore
    private final String name;

    @JsonProperty("index")
    private final boolean index;
    private final MappingTypes type;

    private FieldProperties(Builder builder) {
        this.name = builder.name;
        this.index = builder.index;
        this.type = builder.type;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isIndex() {
        return index;
    }

    public MappingTypes getType() {
        return type;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder implements BuilderInterface<FieldProperties> {
        private String name;
        private boolean index = true;
        private MappingTypes type = MappingTypes.KEYWORD;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setIndex(boolean index) {
            this.index = index;
            return this;
        }

        public Builder setType(MappingTypes type) {
            this.type = type;
            return this;
        }

        @Override
        public FieldProperties build() {
            return new FieldProperties(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
