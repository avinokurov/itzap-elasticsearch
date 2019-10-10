package com.itzap.elasticsearch.components.providers;

import com.itzap.common.AnyConfig;
import com.itzap.common.Provider;
import com.itzap.data.api.Connection;
import com.itzap.data.api.Event;
import com.itzap.data.api.Listener;
import com.itzap.elasticsearch.components.embedded.EmbeddedElasticSearchServer;
import com.itzap.elasticsearch.impl.writer.EsConnection;
import com.itzap.elasticsearch.model.EsConfig;
import com.itzap.rxjava.command.RunnableCommand;
import io.reactivex.Observable;
import org.apache.http.HttpHost;

import java.util.List;
import java.util.stream.Collectors;

public class EsConnectionProvider implements Provider<EsConnection> {
    private final AnyConfig config;
    private final EmbeddedElasticSearchServer server;

    public EsConnectionProvider(AnyConfig config) {
        this.config = config;
        this.server = new EmbeddedElasticSearchServer(config);
    }

    @Override
    public Observable<EsConnection> get() {
        return new RunnableCommand<EsConnection>("cmd-EsConnectionProvider") {
            @Override
            protected EsConnection run() {
                Listener listener = null;
                if (config.getBool(EsConfig.MEMORY)) {
                    listener = new Listener() {
                        @Override
                        public void handle(Event event) {
                            if (event == Connection.ConnectionEvents.CREATING) {
                                server.start()
                                        .blockingGet();
                            }

                            if (event == Connection.ConnectionEvents.DISCONNECTED) {
                                server.stop()
                                        .blockingGet();
                            }
                        }

                        @Override
                        public String getName() {
                            return "connection-listener";
                        }
                    };
                }
                return EsConnection.builder()
                        .setHosts(toHttpHosts(config.getList(EsConfig.URL)))
                        .setListener(listener)
                        .build();
            }
        }.toObservable();
    }

    private static List<HttpHost> toHttpHosts(List<String> urls) {
        return urls.stream()
                .map(HttpHost::create)
                .collect(Collectors.toList());
    }
}
