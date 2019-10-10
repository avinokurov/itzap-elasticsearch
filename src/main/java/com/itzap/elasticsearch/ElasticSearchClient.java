package com.itzap.elasticsearch;

import com.itzap.common.model.AbstractDocument;
import com.itzap.elasticsearch.mapping.DynamicTemplates;
import com.itzap.elasticsearch.mapping.FieldProperties;
import com.itzap.elasticsearch.model.IndexDocument;
import io.reactivex.Completable;
import io.reactivex.Observable;

import java.util.Map;

public interface ElasticSearchClient {
    Completable healthCheck();

    Observable<Boolean> addIndex(final String indexName);

    Observable<Boolean> addIndexIfMissing(final String indexName);

    Observable<Boolean> doesResourceExist(final String resourcePath);

    Observable<String> findMissingFields(IndexDocument indexDocument, FieldProperties... indexFields);

    Observable<Boolean> addMappingToIndex(IndexDocument indexDocument, Map<String, ?> mappings);

    Observable<Boolean> addMappingTemplatesToIndex(IndexDocument documentType, DynamicTemplates templates);

    Observable<Boolean> addFieldToMapping(IndexDocument indexDocument, FieldProperties... indexFields);

    Observable<Boolean> indexDocument(IndexDocument indexDocument, String docId, Object document);

    Observable<Boolean> updateDocument(IndexDocument indexDocument, String docId, Map<String, Object> values);

    <T extends AbstractDocument> Observable<T> get(String id, IndexDocument indexDocument, Class<T> clazz);
}
