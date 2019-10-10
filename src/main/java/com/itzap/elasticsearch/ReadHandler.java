package com.itzap.elasticsearch;

import com.itzap.common.Named;
import org.elasticsearch.client.Response;

import java.io.IOException;

abstract class ReadHandler implements Named {
    private final String name;

    ReadHandler(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    abstract Response read() throws IOException;
}
