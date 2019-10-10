package com.itzap.elasticsearch;

import com.itzap.common.Named;
import org.elasticsearch.action.DocWriteResponse;

import java.io.IOException;

abstract class WriteHandler implements Named {
    private final String name;

    protected WriteHandler(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    abstract DocWriteResponse write(byte[] data) throws IOException;
}
