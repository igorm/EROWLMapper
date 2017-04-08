package com.myrosh.erowlmapper.er.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.myrosh.erowlmapper.er.ERSchema;
import com.myrosh.erowlmapper.er.ERSchemaParser;

import java.io.Reader;

/**
 * @author igorm
 *
 * A concrete YAML ER schema parser implementation
 *
 */
public class YAMLERSchemaParser implements ERSchemaParser {

    /* (non-Javadoc)
     * @see com.myrosh.erowlmapper.er.ERSchemaParser#parse(java.io.Reader)
     */
    public ERSchema parse(Reader reader) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(reader, ERSchema.class);
    }
}
