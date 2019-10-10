package com.itzap.elasticsearch.impl.writer;

import com.itzap.data.api.Layout;
import com.itzap.elasticsearch.mapping.DynamicTemplates;
import com.itzap.elasticsearch.model.DocumentHolder;
import org.apache.commons.lang3.tuple.Pair;

public interface EsLayout extends Layout<EsField> {
    DynamicTemplates asTemplate();

    DocumentHolder asDocument(Pair[] record);
}
