package com.itzap.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.itzap.common.BuilderInterface;

@JsonDeserialize(builder = TemplateField.Builder.class)
public class TemplateField {

    @JsonProperty("path_match")
    private final String pathMatch;

    private final Mapping mapping;

    private TemplateField(Builder builder) {
        this.pathMatch = builder.pathMatch;
        this.mapping = builder.mapping;
    }

    public String getPathMatch() {
        return pathMatch;
    }

    public Mapping getMapping() {
        return mapping;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder implements BuilderInterface<TemplateField> {
        private String pathMatch;
        private Mapping mapping;

        public Builder setPathMatch(String pathMatch) {
            this.pathMatch = pathMatch;
            return this;
        }

        public Builder setMapping(Mapping mapping) {
            this.mapping = mapping;
            return this;
        }

        @Override
        public TemplateField build() {
            return new TemplateField(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
