package com.itzap.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itzap.common.exception.IZapException;
import com.itzap.common.model.AbstractDocument;
import com.itzap.elasticsearch.impl.writer.EsConnection;
import com.itzap.elasticsearch.impl.writer.EsDestination;
import com.itzap.elasticsearch.mapping.DynamicTemplates;
import com.itzap.elasticsearch.mapping.FieldProperties;
import com.itzap.elasticsearch.mapping.UpdateMappingRequest;
import com.itzap.elasticsearch.model.IndexDocument;
import com.itzap.http.model.HttpMethod;
import com.itzap.rxjava.command.RunnableCommand;
import io.reactivex.Completable;
import io.reactivex.Observable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.itzap.common.utils.NamedUtils.filterNames;
import static com.itzap.common.utils.NamedUtils.getNameString;
import static com.itzap.common.utils.NamedUtils.getNames;
import static com.itzap.elasticsearch.ElasticSearchUtils.gotoObject;
import static com.itzap.elasticsearch.ElasticSearchUtils.readAsMap;
import static com.itzap.elasticsearch.ElasticSearchUtils.readAsObject;
import static com.itzap.elasticsearch.ElasticSearchUtils.readAsVoid;
import static com.itzap.elasticsearch.ElasticSearchUtils.readQuietly;
import static com.itzap.elasticsearch.ElasticSearchUtils.request;
import static com.itzap.elasticsearch.ElasticSearchUtils.toFieldResourcePath;
import static com.itzap.elasticsearch.ElasticSearchUtils.toMappingResourcePath;
import static com.itzap.elasticsearch.ElasticSearchUtils.writeAsObject;

public class ElasticSearchClientImpl implements ElasticSearchClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchClientImpl.class);

    private final RestClient admin;
    private final RestHighLevelClient client;
    private final EsDestination destination;

    public ElasticSearchClientImpl(EsDestination destination) {
        this.admin = ((EsConnection)destination.getConnection()).getAdmin();
        this.client = ((EsConnection)destination.getConnection()).getClient();
        this.destination = destination;
    }

    @Override
    public Completable healthCheck() {
        return new RunnableCommand<Void>("cmd-healthCheck") {
            @Override
            protected Void run() {
                Map<String, String> params = new HashMap<>();
                params.put("wait_for_status", destination.getHealthColor());
                params.put("timeout", destination.getHealthTypeoutSec() + "s");

                try {
                    admin.performRequest(request(HttpMethod.GET, "/_cluster/health", params));
                    LOGGER.info("Elastic search is healthy");
                    return null;
                } catch (IOException e) {
                    throw new IZapException("Elastic search health check failed", e);
                }
            }
        }.toCompletable();
    }

    @Override
    public Observable<Boolean> addIndex(String indexName) {
        return new RunnableCommand<Boolean>("cmd-addIndex") {
            @Override
            protected Boolean run() {
                StatusLine statusLine = readQuietly(new ReadHandler("addIndex") {
                    @Override
                    Response read() throws IOException {
                        return admin.performRequest(request(HttpMethod.PUT, "/" + indexName));
                    }
                });
                return statusLine.getStatusCode() == HttpStatus.SC_OK;
            }
        }.toObservable();
    }

    @Override
    public Observable<Boolean> addIndexIfMissing(String indexName) {
        return doesResourceExist("/" + indexName)
                .flatMap(exists -> exists ? Observable.just(true) : addIndex(indexName));
    }

    @Override
    public Observable<Boolean> doesResourceExist(String resourcePath) {
        return new RunnableCommand<Boolean>("cmd-doesResourceExist") {
            @Override
            protected Boolean run() {
                StatusLine statusLine = readQuietly(new ReadHandler("doesResourceExist") {
                    @Override
                    Response read() throws IOException {
                        return admin.performRequest(request(HttpMethod.HEAD, resourcePath));
                    }
                });
                return statusLine.getStatusCode() == HttpStatus.SC_OK;
            }
        }.toObservable();
    }

    @Override
    public Observable<String> findMissingFields(IndexDocument indexDocument, FieldProperties... indexFields) {
        return new RunnableCommand<List<String>>("cmd-findMissingFields") {

            @Override
            protected List<String> run() {
                List<String> names = getNames(indexFields);

                String resourcePath = toFieldResourcePath(indexDocument, indexFields);

                Map<String, Object> results = readAsMap(new ReadHandler("findMissingFields") {
                    @Override
                    Response read() throws IOException {
                        return admin.performRequest(request(HttpMethod.GET, resourcePath));
                    }
                });
                Map<String, Object> fields = gotoObject(results, indexDocument.getName(),
                        "mappings");

                return names.stream()
                        .filter(n -> !fields.containsKey(n))
                        .collect(Collectors.toList());
            }
        }
                .toObservable()
                .flatMap(Observable::fromIterable);
    }

    @Override
    public Observable<Boolean> addMappingToIndex(IndexDocument documentType, Map<String, ?> mappings) {
        LOGGER.info("Adding mapping to index '{}'...",
                documentType.getName());

        return doesResourceExist(toMappingResourcePath(documentType))
                .map(exists -> {
                    if (exists) {
                        LOGGER.info("Mapping '{}' already exists", documentType.getName());
                        return false;
                    } else {
                        readAsVoid(new ReadHandler("addMappingToIndex") {
                            @Override
                            Response read() throws IOException {
                                byte[] mappingSource = OBJECT_MAPPER.writeValueAsBytes(mappings);

                                HttpEntity entity = new NByteArrayEntity(mappingSource, ContentType.APPLICATION_JSON);

                                Response response = admin.performRequest(request(HttpMethod.PUT,
                                        toMappingResourcePath(documentType), entity));

                                LOGGER.info("Added '{}' mapping", documentType.getName());
                                return response;
                            }
                        });
                        return true;
                    }
                });
    }

    @Override
    public Observable<Boolean> addMappingTemplatesToIndex(IndexDocument documentType, DynamicTemplates templates) {
        LOGGER.info("Adding mapping templates to index '{}'...", documentType.getName());

        return new RunnableCommand<Boolean>("cmd-addMappingTemplatesToIndex") {

            @Override
            protected Boolean run() {
                readAsVoid(new ReadHandler("addMappingTemplatesToIndex") {
                    @Override
                    Response read() throws IOException {
                        byte[] mappingSource = OBJECT_MAPPER.writeValueAsBytes(templates);

                        HttpEntity entity = new NByteArrayEntity(mappingSource, ContentType.APPLICATION_JSON);
                        Response response = admin.performRequest(request(HttpMethod.PUT,
                                toMappingResourcePath(documentType), entity));
                        LOGGER.info("Added '{}' mapping templates", documentType.getName());
                        return response;
                    }
                });
                return true;
            }
        }.toObservable();
    }

    @Override
    public Observable<Boolean> addFieldToMapping(IndexDocument documentType, FieldProperties... indexFields) {
        if (indexFields == null || indexFields.length == 0) {
            LOGGER.warn("Updating mapping for index '{}' skipped. No fields provided",
                    documentType.getName());
            return Observable.just(false);
        }

        LOGGER.info("Updating mapping for index '{}' adding field '{}'...",
                documentType.getName(), getNameString(indexFields));
        return findMissingFields(documentType, indexFields)
                .collectInto(new HashSet<String>(), HashSet::add)
                .toObservable()
                .map(fields -> {
                    if (!fields.isEmpty()) {
                        readAsVoid(new ReadHandler("addFieldToMapping") {
                            @Override
                            Response read() throws IOException {
                                UpdateMappingRequest request = UpdateMappingRequest.fromFields(filterNames(indexFields,
                                        f -> fields.contains(f.getName())));
                                HttpEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(request),
                                        ContentType.APPLICATION_JSON);
                                Response response = admin.performRequest(request(HttpMethod.PUT,
                                        toMappingResourcePath(documentType), entity));
                                LOGGER.info("Updated '{}' mapping", documentType.getName());
                                return response;
                            }
                        });
                        return true;
                    } else {
                        LOGGER.info("Mapping '{}' does not exists", documentType.getName());
                        return false;
                    }
                });
    }

    @Override
    public Observable<Boolean> indexDocument(IndexDocument documentType, String docId, Object document) {
        return new RunnableCommand<Boolean>("cmd-indexDocument") {

            @Override
            protected Boolean run() {
                writeAsObject(new WriteHandler("indexDocument_" + documentType.getName()) {
                    @Override
                    DocWriteResponse write(byte[] data) throws IOException {
                        IndexRequest request = new IndexRequest();
                        return client.index(request
                                        .index(documentType.getName())
                                        .id(docId)
                                        .source(data, XContentType.JSON),
                                RequestOptions.DEFAULT);
                    }
                }, document);
                return true;
            }
        }.toObservable();
    }

    @Override
    public Observable<Boolean> updateDocument(IndexDocument documentType, String docId, Map<String, Object> values) {
        return new RunnableCommand<Boolean>("cmd-updateDocument") {

            @Override
            protected Boolean run() {
                writeAsObject(new WriteHandler("update" + documentType + "_Document_" + documentType.getName()) {
                    @Override
                    DocWriteResponse write(byte[] data) throws IOException {
                        UpdateRequest request = new UpdateRequest()
                                .id(docId)
                                .doc(values, XContentType.JSON)
                                .retryOnConflict(destination.getRetry());
                        return client.update(request, RequestOptions.DEFAULT);
                    }
                }, values);
                return true;
            }
        }.toObservable();
    }

    @Override
    public <T extends AbstractDocument>  Observable<T> get(String id, IndexDocument type, Class<T> clazz) {
        return new RunnableCommand<T>("cmd-get") {

            @Override
            protected T run() {
                return readAsObject(new ActionHandler("get_" + type.getName() + "_ById_" + id) {
                    @Override
                    ActionResponse read() throws IOException {
                        GetRequest request = new GetRequest()
                                .id(id);
                        return client.get(request, RequestOptions.DEFAULT);
                    }
                }, clazz);
            }
        }.toObservable();
    }
}
