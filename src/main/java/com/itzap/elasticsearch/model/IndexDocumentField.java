package com.itzap.elasticsearch.model;

import com.itzap.common.BuilderInterface;
import com.itzap.common.Named;

public class IndexDocumentField implements Named {
    private final String name;

    private IndexDocumentField(Builder builder) {
        this.name = builder.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static class Builder implements BuilderInterface<IndexDocumentField> {
        private String name;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public IndexDocumentField build() {
            return null;
        }
    }
}
