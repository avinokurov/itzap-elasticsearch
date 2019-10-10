package com.itzap.elasticsearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.itzap.common.Named;
import com.itzap.common.exception.IZapErrorCodes;
import com.itzap.common.exception.IZapException;
import com.itzap.common.utils.NamedUtils;
import com.itzap.elasticsearch.model.IndexDocument;
import com.itzap.http.model.HttpMethod;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.itzap.http.utils.HttpUtils.is2xx;

final class ElasticSearchUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ElasticSearchUtils() {
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> gotoObject(Map<String, Object> source, String... names) {
        if (MapUtils.isEmpty(source) || names == null || names.length == 0) {
            return ImmutableMap.of();
        }

        Map<String, Object> result = source;
        for (String name : names) {
            result = (Map<String, Object>) result.getOrDefault(name, ImmutableMap.of());
            if (MapUtils.isEmpty(result)) {
                return result;
            }
        }

        return result;
    }

    static Request request(HttpMethod method, String url) {
        return new Request(method.getName(), url);
    }

    static Request request(HttpMethod method, String url, Map<String, String> params) {
        Request request = request(method, url);
        request.addParameters(params);

        return request;
    }

    static Request request(HttpMethod method,
                           String url,
                           HttpEntity entity) {
        Request request = request(method, url);
        request.setEntity(entity);

        return request;
    }

    static Request request(HttpMethod method,
                           String url,
                           Map<String, String> params,
                           HttpEntity entity) {
        Request request = request(method, url);
        request.addParameters(params);
        request.setEntity(entity);

        return request;
    }

    static <T> T readAsObject(ReadHandler handler, Class<T> clazz) {
        Response response = null;
        try {
            response = handler.read();
            if (!is2xx(response.getStatusLine().getStatusCode())) {
                throw new IZapException(String.format("Failed to read handler=%s. reason=%s httpCode=%d",
                        handler.getName(),
                        response.getStatusLine().getReasonPhrase(),
                        response.getStatusLine().getStatusCode()));
            }

            return OBJECT_MAPPER.readValue(response.getEntity().getContent(), clazz);
        } catch (IOException e) {
            throw new IZapException(String.format("Failed to read handler=%s",
                    handler.getName()), e);
        } finally {
            if (response != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T readAsObject(ActionHandler handler, Class<T> clazz) {
        ActionResponse response = null;
        try {
            response = handler.read();
            if (response instanceof GetResponse) {
                GetResponse getResponse = (GetResponse) response;
                if (!getResponse.isExists() || getResponse.isSourceEmpty()) {
                    throw new IZapException(String.format("Failed to find %s", handler.getName()),
                            IZapErrorCodes.NOT_FOUND);
                }
                return OBJECT_MAPPER.readValue(getResponse.getSourceAsBytes(), clazz);
            } else if (response instanceof SearchResponse) {

                return (T) response;
            } else {
                throw new UnsupportedOperationException("Operation is not supported");
            }
        } catch (IOException e) {
            throw new IZapException(String.format("Failed to read handler=%s",
                    handler.getName()), e);
        }
    }

    static Map<String, Object> readAsMap(ReadHandler handler) {
        Response response = null;
        try {
            response = handler.read();
            if (!is2xx(response.getStatusLine().getStatusCode())) {
                throw new IZapException(String.format("Failed to read handler=%s. reason=%s httpCode=%d",
                        handler.getName(),
                        response.getStatusLine().getReasonPhrase(),
                        response.getStatusLine().getStatusCode()));
            }

            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };

            return OBJECT_MAPPER.readValue(response.getEntity().getContent(),
                    typeRef);
        } catch (IOException e) {
            throw new IZapException(String.format("Failed to read handler=%s",
                    handler.getName()), e);
        } finally {
            if (response != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    static String readAsObject(ReadHandler handler) {
        Response response = null;
        try {
            response = handler.read();
            if (!is2xx(response.getStatusLine().getStatusCode())) {
                throw new IZapException(String.format("Failed to read handler=%s. reason=%s httpCode=%d",
                        handler.getName(),
                        response.getStatusLine().getReasonPhrase(),
                        response.getStatusLine().getStatusCode()));
            }

            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new IZapException(String.format("Failed to read handler=%s",
                    handler.getName()), e);
        } finally {
            if (response != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    static void readAsVoid(ReadHandler handler) {
        Response response = null;

        try {
            response = handler.read();
            if (!is2xx(response.getStatusLine().getStatusCode())) {
                throw new IZapException(String.format("Failed to read handler=%s. reason=%s httpCode=%d",
                        handler.getName(),
                        response.getStatusLine().getReasonPhrase(),
                        response.getStatusLine().getStatusCode()));
            }
        } catch (IOException e) {
            throw new IZapException(String.format("Failed to read handler=%s",
                    handler.getName()), e);
        } finally {
            if (response != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    static StatusLine readQuietly(ReadHandler handler) {
        Response response = null;
        try {
            response = handler.read();
            return response.getStatusLine();
        } catch (IOException e) {
            throw new IZapException(String.format("Failed to read handler=%s",
                    handler.getName()), e);
        } finally {
            if (response != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    static void writeAsObject(WriteHandler handler, Object object) {
        DocWriteResponse response;
        try {
            byte[] data = OBJECT_MAPPER.writeValueAsBytes(object);
            response = handler.write(data);
            if (!is2xx(response.getShardInfo().status().getStatus())) {
                String reason = Joiner.on(",").join(
                        Stream.of(response.getShardInfo().getFailures())
                                .map(ReplicationResponse.ShardInfo.Failure::reason)
                                .collect(Collectors.toList()));

                throw new IZapException(String.format("Failed to write handler=%s. reason=%s httpCode=%d",
                        handler.getName(),
                        reason,
                        response.getShardInfo().status().getStatus()));
            }
        } catch (IOException e) {
            throw new IZapException(String.format("Failed to write handler=%s",
                    handler.getName()), e);
        }
    }

    static String toIndexResourcePath(IndexDocument index) {
        return "/" + index.getName();
    }

    static String toMappingResourcePath(IndexDocument index) {
        return toIndexResourcePath(index) + "/_mapping";
    }

    static String toFieldResourcePath(IndexDocument index, Named ... fields) {
        String fieldsPath = NamedUtils.getNameString(fields);

        if (Named.BLANK_NAME.equals(fieldsPath)) {
            return Named.BLANK_NAME;
        }

        return toMappingResourcePath(index) + "/field/" + fieldsPath;
    }
}
