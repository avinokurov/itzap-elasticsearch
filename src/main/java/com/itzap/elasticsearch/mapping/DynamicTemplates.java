package com.itzap.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.itzap.common.BuilderInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = DynamicTemplates.Builder.class)
public class DynamicTemplates {

    @JsonProperty("dynamic_templates")
    private final List<Map<String, TemplateField>> dynamicTemplates;

    private DynamicTemplates(Builder builder) {
        this.dynamicTemplates = builder.dynamicTemplates;
    }

    public List<Map<String, TemplateField>> getDynamicTemplates() {
        return dynamicTemplates;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder implements BuilderInterface<DynamicTemplates> {
        private List<Map<String, TemplateField>> dynamicTemplates = new ArrayList<>();

        public Builder setDynamicTemplates(List<Map<String, TemplateField>> dynamicTemplates) {
            this.dynamicTemplates = dynamicTemplates;
            return this;
        }

        public Builder putTemplateField(String name, TemplateField field) {
            Map<String, TemplateField> fieldMap;

            if (dynamicTemplates.isEmpty()) {
                fieldMap = new HashMap<>();
                dynamicTemplates.add(fieldMap);
            } else {
                fieldMap = dynamicTemplates.get(0);
            }

            fieldMap.put(name, field);

            return this;
        }

        public Builder addTemplate(Map<String,TemplateField> template) {
            dynamicTemplates.add(template);
            return this;
        }

        @Override
        public DynamicTemplates build() {
            return new DynamicTemplates(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
