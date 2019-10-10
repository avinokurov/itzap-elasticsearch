package com.itzap.elasticsearch.impl.writer;

import com.itzap.common.BuilderInterface;
import com.itzap.data.api.Connection;
import com.itzap.data.api.Listener;
import com.itzap.data.api.ListenerRegistry;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.List;

public class EsConnection extends ListenerRegistry implements Connection {
    private final RestClient admin;
    private final RestHighLevelClient client;

    private EsConnection(RestClient admin, RestHighLevelClient client) {
        this.admin = admin;
        this.client = client;
    }

    public RestClient getAdmin() {
        return admin;
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    @Override
    public boolean connect() {
        fireEvent(ConnectionEvents.CONNECTED);
        return true;
    }

    @Override
    public boolean disconnect() {
        fireEvent(ConnectionEvents.DISCONNECTED);

        return true;
    }

    @Override
    public boolean isValid() {
        return client != null && admin != null;
    }

    @Override
    public String getName() {
        return "elasticsearch";
    }

    public static class Builder implements BuilderInterface<EsConnection> {
        private List<HttpHost> hosts;
        private Listener listener;

        public Builder setHosts(List<HttpHost> hosts) {
            this.hosts = hosts;
            return this;
        }

        public Builder setListener(Listener listener) {
            this.listener = listener;
            return this;
        }

        @Override
        public EsConnection build() {
            if (this.listener != null) {
                this.listener.handle(ConnectionEvents.CREATING);
            }
            RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[0]));

            EsConnection connection =  new EsConnection(builder.build(),
                    new RestHighLevelClient(builder));

            if (listener != null) {
                this.listener.handle(ConnectionEvents.CREATED);
                connection.registerListener(listener);
            }

            return connection;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
