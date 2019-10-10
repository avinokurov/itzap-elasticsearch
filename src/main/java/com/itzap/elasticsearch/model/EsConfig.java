package com.itzap.elasticsearch.model;

import com.itzap.common.Property;

public enum EsConfig implements Property {
    URL("url", "http://localhost:9200"),
    INDEX_NAME("index"),
    HEALTH_COLOR("health", "green"),
    HEALTH_TIMEOUT("health.timeout.sec", "10"),
    RETRIES("retries", "3"),

    DATA_PATH("path.data", "path.data"),
    HOME_PATH("path.home", "path.home"),
    PORT("port", "9200"),
    CLUSTER_NAME("cluster.name", "es_test_cluster"),
    HOST("host", "127.0.0.1"),

    MEMORY("memory", "false");

    private final String name;
    private final String defaultValue;

    EsConfig(String name) {
        this.name = name;
        this.defaultValue = null;
    }

    EsConfig(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }
}
