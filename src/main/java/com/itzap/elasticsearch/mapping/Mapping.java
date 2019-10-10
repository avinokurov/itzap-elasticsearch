package com.itzap.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.itzap.common.BuilderInterface;

@JsonDeserialize(builder = Mapping.Builder.class)
public class Mapping {
    private final String type;

    @JsonProperty("copy_to")
    private final String copyTo;

    public Mapping(Builder builder) {
        this.type = builder.type;
        this.copyTo = builder.copyTo;
    }

    public String getType() {
        return type;
    }

    public String getCopyTo() {
        return copyTo;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder implements BuilderInterface<Mapping> {
        private String type;
        private String copyTo;

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setCopyTo(String copyTo) {
            this.copyTo = copyTo;
            return this;
        }

        @Override
        public Mapping build() {
            return new Mapping(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
