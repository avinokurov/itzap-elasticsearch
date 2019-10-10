package com.itzap.elasticsearch.components.embedded;

import com.itzap.common.AnyConfig;
import com.itzap.config.ConfigBuilder;
import com.itzap.config.ConfigType;
import org.junit.Before;
import org.junit.Test;

public class EmbeddedElasticSearchServerTest {
    private EmbeddedElasticSearchServer server;

    @Before
    public void setup() {
        AnyConfig config = ConfigBuilder.builder(ConfigType.TYPE_SAFE)
                .setFileName(this.getClass()
                        .getResource("/es-config.properties").getFile())
                .build();

        server = new EmbeddedElasticSearchServer(config);
    }

    @Test
    public void startTest() {
        server.start().blockingGet();
        server.stop().blockingGet();
    }
}