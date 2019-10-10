package com.itzap.elasticsearch.impl.writer;

import com.itzap.data.api.Field;
import com.itzap.elasticsearch.mapping.MappingTypes;

public interface EsField extends Field {
    boolean isIndex();

    MappingTypes getMappingType();
}
