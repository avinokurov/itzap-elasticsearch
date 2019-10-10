package com.itzap.elasticsearch.components.providers;

import com.itzap.common.AnyConfig;
import com.itzap.common.Provider;
import com.itzap.elasticsearch.impl.writer.EsConnection;
import com.itzap.elasticsearch.impl.writer.EsDestination;
import com.itzap.elasticsearch.model.EsConfig;
import com.itzap.rxjava.command.RunnableCommand;
import io.reactivex.Observable;

public class EsDestinationProvider implements Provider<EsDestination> {
    private final AnyConfig config;
    private final EsConnectionProvider connectionProvider;

    public EsDestinationProvider(AnyConfig config,
                                 EsConnectionProvider connectionProvider) {
        this.config = config;
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Observable<EsDestination> get() {
        return connectionProvider.get()
                .flatMap(this::connection);
    }

    private Observable<EsDestination> connection(EsConnection connection) {
        return new RunnableCommand<EsDestination>("cmd-EsDestinationProvider") {
            @Override
            protected EsDestination run() {
                return EsDestination.builder()
                        .setConnection(connection)
                        .setName(config.getString(EsConfig.INDEX_NAME))
                        .setHealthColor(config.getString(EsConfig.HEALTH_COLOR))
                        .setRetry(config.getInt(EsConfig.RETRIES))
                        .setHealthTypeoutSec(config.getInt(EsConfig.HEALTH_TIMEOUT))
                        .build();
            }
        }.toObservable();
    }
}
