package com.itzap.elasticsearch.model;

public class DocumentHolder {
    private final String id;
    private final Object document;

    public DocumentHolder(String id, Object document) {
        this.id = id;
        this.document = document;
    }

    public Object getDocument() {
        return document;
    }

    public String getId() {
        return id;
    }
}
