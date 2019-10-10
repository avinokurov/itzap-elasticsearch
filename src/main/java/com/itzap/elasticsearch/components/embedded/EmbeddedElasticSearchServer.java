package com.itzap.elasticsearch.components.embedded;

import com.google.common.collect.ImmutableMap;
import com.itzap.common.AnyConfig;
import com.itzap.common.exception.IZapException;
import com.itzap.data.api.IOData;
import com.itzap.elasticsearch.model.EsConfig;
import com.itzap.rxjava.command.RunnableCommand;
import io.reactivex.Completable;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

public class EmbeddedElasticSearchServer implements IOData {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedElasticSearchServer.class);

    private Node instance;
    private int port;
    private final AnyConfig config;

    public EmbeddedElasticSearchServer(AnyConfig config) {
        this.config = config;
    }

    private static class PluginConfigurableNode extends Node {
        PluginConfigurableNode(Settings input,
                               Map<String, String> properties,
                               Path configPath,
                               Supplier<String> defaultNodeName,
                               Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(input, properties, configPath, defaultNodeName), classpathPlugins, false);
        }
    }

    @Override
    public synchronized Completable start() {
        return new RunnableCommand<Void>("cmd-start") {
            @Override
            protected Void run() {
                Settings settings = getSettings();

                instance = new PluginConfigurableNode(settings, ImmutableMap.of(),
                        null, () -> config.getString(EsConfig.CLUSTER_NAME),
                        singletonList(Netty4Plugin.class));
                try {
                    instance.start();
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            if (instance != null) {
                                instance.close();
                            }
                        } catch (IOException e) {
                            LOGGER.error("Error closing ElasticSearch");
                        }
                    }));
                    LOGGER.info("ElasticSearch cluster {} started in local mode on port {}", instance.settings().get("cluster.name"),
                            port);
                    return null;
                } catch (NodeValidationException e) {
                    throw new IZapException("Failed to start embedded elastic search server", e);
                }
            }
        }.toCompletable();
    }

    @Override
    public synchronized Completable stop() {
        return new RunnableCommand<Void>("cmd-stop") {
            @Override
            protected Void run() {
                if (instance != null && !instance.isClosed()) {
                    LOGGER.info("Stopping Elastic Search");
                    try {
                        instance.close();
                        instance = null;
                        LOGGER.info("Elastic Search on port {} stopped", port);
                    } catch (IOException e) {
                        throw new IZapException("Failed to close elastic search embedded server", e);
                    }
                }

                return null;
            }
        }.toCompletable();
    }

    private Settings getSettings() {
        String clusterName = config.getString(EsConfig.CLUSTER_NAME);
        String host = config.getString(EsConfig.HOST);
        port = config.getInt(EsConfig.PORT);

        try {
            File dataDir = Files.createTempDirectory(clusterName + "_" + System.currentTimeMillis() + "data").toFile();
            FileUtils.forceDeleteOnExit(dataDir);
            cleanDataDir(dataDir.getAbsolutePath());

            File homeDir = Files.createTempDirectory(clusterName + "_" + System.currentTimeMillis() + "-home").toFile();
            cleanDataDir(homeDir.getAbsolutePath());
            FileUtils.forceDeleteOnExit(homeDir);

            Settings.Builder settingsBuilder = Settings.builder()
                    .put("cluster.name", clusterName)
                    .put("http.host", host)
                    .put("http.port", port)
                    .put("transport.tcp.port", port + 100)
                    .put(EsConfig.DATA_PATH.getName(), dataDir.getAbsolutePath())
                    .put(EsConfig.HOME_PATH.getName(), homeDir.getAbsolutePath())
                    .put("http.cors.enabled", true)
                    .put("node.data", true)
                    .put("http.type", "netty4")
                    .put("transport.type", "netty4");

            return settingsBuilder.build();
        } catch (IOException e) {
            throw new IZapException("Failed to create temp data/home dir.", e);
        }
    }
}
