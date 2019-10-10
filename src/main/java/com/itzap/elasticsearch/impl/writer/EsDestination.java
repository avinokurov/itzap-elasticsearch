package com.itzap.elasticsearch.impl.writer;

import com.itzap.common.BuilderInterface;
import com.itzap.data.api.Connection;
import com.itzap.data.api.DataDestination;

public class EsDestination implements DataDestination {
    private final String name;
    private final EsConnection connection;
    private final String healthColor;
    private final int retry;
    private final int healthTypeoutSec;

    private EsDestination(Builder builder) {
        this.name = builder.name;
        this.connection = builder.connection;
        this.healthColor = builder.healthColor;
        this.retry = builder.retry;
        this.healthTypeoutSec = builder.healthTypeoutSec;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean isValid() {
        return this.connection.isValid();
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getHealthColor() {
        return healthColor;
    }

    public int getRetry() {
        return retry;
    }

    public int getHealthTypeoutSec() {
        return healthTypeoutSec;
    }

    public static class Builder implements BuilderInterface<EsDestination> {
        private String name;
        private EsConnection connection;
        private String healthColor;
        private int retry;
        private int healthTypeoutSec;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setConnection(EsConnection connection) {
            this.connection = connection;
            return this;
        }

        public Builder setHealthColor(String healthColor) {
            this.healthColor = healthColor;
            return this;
        }

        public Builder setRetry(int retry) {
            this.retry = retry;
            return this;
        }

        public Builder setHealthTypeoutSec(int healthTypeoutSec) {
            this.healthTypeoutSec = healthTypeoutSec;
            return this;
        }

        @Override
        public EsDestination build() {
            return new EsDestination(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
