package com.itzap.elasticsearch.impl.writer;

import com.itzap.common.BuilderInterface;
import com.itzap.elasticsearch.exceptions.EsErrors;

import java.util.List;

import static com.itzap.common.exception.IZapException.descriptor;
import static com.itzap.common.utils.PreconditionUtils.checkCollection;

public abstract class AbstractEsLayout implements EsLayout {
    private final String name;
    private final List<EsField> fields;

    protected AbstractEsLayout(Builder builder) {
        this.name = builder.name;
        this.fields = builder.fields;
    }

    @Override
    public List<EsField> getFields() {
        return this.fields;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static abstract class Builder<T extends EsLayout, B extends AbstractEsLayout.Builder> implements BuilderInterface<T> {
        private String name;
        protected List<EsField> fields;

        protected abstract B getThis();

        protected abstract T buildLayout();

        public B setName(String name) {
            this.name = name;
            return getThis();
        }

        public B setFields(List<EsField> fields) {
            this.fields = fields;
            return getThis();
        }

        @Override
        public T build() {
            checkCollection(fields,
                    descriptor(EsErrors.INVALID_SETUP, "Elastic search fields cannot be empty"));

            return buildLayout();
        }
    }
}
