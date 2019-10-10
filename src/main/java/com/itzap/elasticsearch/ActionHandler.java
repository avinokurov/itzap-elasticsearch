package com.itzap.elasticsearch;

import com.itzap.common.Named;
import org.elasticsearch.action.ActionResponse;

import java.io.IOException;

abstract class ActionHandler implements Named {
    private final String name;

    protected ActionHandler(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    abstract ActionResponse read() throws IOException;
}
