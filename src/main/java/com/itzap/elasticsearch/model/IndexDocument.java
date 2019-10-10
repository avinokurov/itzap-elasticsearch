package com.itzap.elasticsearch.model;

import com.itzap.common.BuilderInterface;
import com.itzap.common.Named;

public class IndexDocument implements Named {
    private final String name;


    private IndexDocument(Builder builder) {
        this.name = builder.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static class Builder implements BuilderInterface<IndexDocument> {
        private String name;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public IndexDocument build() {
            return new IndexDocument(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
