package com.itzap.elasticsearch.components.providers;

import com.itzap.common.AnyConfig;
import com.itzap.common.Provider;
import com.itzap.elasticsearch.ElasticSearchClient;
import com.itzap.elasticsearch.ElasticSearchClientImpl;
import com.itzap.elasticsearch.impl.writer.EsDestination;
import com.itzap.rxjava.command.RunnableCommand;
import io.reactivex.Observable;

public class EsClientProvider implements Provider<ElasticSearchClient> {
    private final AnyConfig config;
    private final EsDestinationProvider destinationProvider;

    public EsClientProvider(AnyConfig config,
                            EsDestinationProvider destinationProvider) {
        this.config = config;
        this.destinationProvider = destinationProvider;
    }

    @Override
    public Observable<ElasticSearchClient> get() {
        return destinationProvider.get()
                .flatMap(this::getEsClient);
    }

    public Observable<ElasticSearchClient> getEsClient(EsDestination destination) {
        return new RunnableCommand<ElasticSearchClient>("cmd-EsClientProvider") {
            @Override
            protected ElasticSearchClient run() {
                return new ElasticSearchClientImpl(destination);
            }
        }.toObservable();
    }
}
