package com.itzap.elasticsearch.model;

import com.itzap.common.BuilderInterface;
import com.itzap.common.Named;

import java.util.List;

public class Index implements Named {
    private final String name;
    private final List<IndexDocument> documents;

    private Index(Builder builder) {
        this.name = builder.name;
        this.documents = builder.documents;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public List<IndexDocument> getDocuments() {
        return documents;
    }

    public static class Builder implements BuilderInterface<Index> {
        private String name;
        private List<IndexDocument> documents;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDocuments(List<IndexDocument> documents) {
            this.documents = documents;
            return this;
        }

        @Override
        public Index build() {
            return new Index(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
