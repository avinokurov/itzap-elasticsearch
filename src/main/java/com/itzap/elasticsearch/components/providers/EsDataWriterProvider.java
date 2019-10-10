package com.itzap.elasticsearch.components.providers;

import com.itzap.common.AnyConfig;
import com.itzap.common.BuilderInterface;
import com.itzap.common.Provider;
import com.itzap.elasticsearch.impl.writer.EsDataWriter;
import com.itzap.rxjava.command.RunnableCommand;
import io.reactivex.Observable;

public class EsDataWriterProvider implements Provider<EsDataWriter> {
    private final AnyConfig config;
    private final EsClientProvider clientProvider;
    private final EsLayoutProvider layoutProvider;

    public EsDataWriterProvider(AnyConfig config,
                                EsClientProvider clientProvider,
                                EsLayoutProvider layoutProvider) {
        this.config = config;
        this.clientProvider = clientProvider;
        this.layoutProvider = layoutProvider;
    }

    @Override
    public Observable<EsDataWriter> get() {
        return Observable.zip(clientProvider.get(), layoutProvider.get(),
                (client, layout) -> EsDataWriter.builder()
                        .setLayout(layout)
                        .setClient(client))
                .flatMap(this::getEsWriter);
    }

    private Observable<EsDataWriter> getEsWriter(BuilderInterface<EsDataWriter> builder) {
        return new RunnableCommand<EsDataWriter>("cmd-EsDataWriterProvider") {
            @Override
            protected EsDataWriter run() {
                return builder.build();
            }
        }.toObservable();
    }
}
