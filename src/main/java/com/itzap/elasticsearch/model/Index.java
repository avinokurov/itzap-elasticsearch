package com.itzap.elasticsearch.model;

import com.itzap.common.BuilderInterface;
import com.itzap.common.Immutable;
import com.itzap.common.Named;

import java.util.List;

public class Index implements Named, Immutable {
    private static class State implements Immutable.State {
        private String name;
        private List<IndexDocument> documents;
    }

    private final State state;

    private Index(Builder builder) {
        this.state = builder.state;
    }

    @Override
    public String getName() {
        return this.state.name;
    }

    public List<IndexDocument> getDocuments() {
        return state.documents;
    }

    public static class Builder implements BuilderInterface<Index> {
        private final State state = new State();

        public Builder setName(String name) {
            this.state.name = name;
            return this;
        }

        public Builder setDocuments(List<IndexDocument> documents) {
            this.state.documents = documents;
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
