package com.itzap.elasticsearch.impl.writer;

import com.itzap.common.Named;
import com.itzap.data.api.DataWriter;
import com.itzap.data.api.IOData;
import com.itzap.data.api.Layout;
import com.itzap.elasticsearch.ElasticSearchClient;
import com.itzap.elasticsearch.mapping.DynamicTemplates;
import com.itzap.elasticsearch.mapping.FieldProperties;
import com.itzap.elasticsearch.mapping.MappingBuilder;
import com.itzap.elasticsearch.model.DocumentHolder;
import com.itzap.elasticsearch.model.IndexDocument;
import com.itzap.rxjava.command.RunnableCommand;
import io.reactivex.Completable;
import io.reactivex.Observable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.stream.Collectors;

public class EsDataWriter implements DataWriter {
    private final EsLayout layout;
    private final ElasticSearchClient client;
    private final EsDestination destination;
    private final IndexDocument document;

    private EsDataWriter(Builder builder) {
        this.layout = builder.layout;
        this.client = builder.client;
        this.destination = builder.destination;
        this.document = IndexDocument.builder()
                .setName(destination.getName())
                .build();
    }

    @Override
    public Observable<Boolean> write(Pair[] record) {
        DocumentHolder documentHolder = layout.asDocument(record);

        return client.indexDocument(document, documentHolder.getId(),
                documentHolder.getDocument());
    }

    @Override
    public Completable start() {
        return client.healthCheck()
                .toObservable()
                .flatMap(v -> client.addIndexIfMissing(destination.getName()))
                .flatMap(v -> client.addMappingToIndex(document,
                        MappingBuilder.builder()
                                .setProperties(toFieldMap())
                                .build()))
                .flatMap(v -> addTemplate(document))
                .flatMapCompletable(v -> {
                    destination.getConnection().connect();
                    return Completable.complete();
                });
    }

    private Map<String, FieldProperties> toFieldMap() {
        return layout.getFields()
                .stream()
                .collect(Collectors.toMap(Named::getName,
                        f -> FieldProperties.builder()
                                .setName(f.getName())
                                .setIndex(f.isIndex())
                                .setType(f.getMappingType())
                                .build()));
    }

    private Observable<Boolean> addTemplate(IndexDocument document) {
        DynamicTemplates templates = layout.asTemplate();
        return templates == null ? Observable.just(false) :
                client.addMappingTemplatesToIndex(document, templates);
    }

    @Override
    public Completable stop() {
        return new RunnableCommand<Void>("cmd-EsDataWriter-stop") {

            @Override
            protected Void run() {
                destination.getConnection().disconnect();
                return null;
            }
        }.toCompletable();
    }

    public static class Builder implements IOData.Builder<EsDataWriter, Builder> {
        private EsLayout layout;
        private ElasticSearchClient client;
        private EsDestination destination;

        public Builder setClient(ElasticSearchClient client) {
            this.client = client;
            return this;
        }

        public Builder setDestination(EsDestination destination) {
            this.destination = destination;
            return this;
        }

        @Override
        public EsDataWriter build() {
            return new EsDataWriter(this);
        }

        @Override
        public Builder setLayout(Layout layout) {
            this.layout = (EsLayout) layout;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
