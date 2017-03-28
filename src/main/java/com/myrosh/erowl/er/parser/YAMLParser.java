package com.myrosh.erowl.er.parser;

import java.io.Reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.myrosh.erowl.er.Parser;
import com.myrosh.erowl.er.schema.Schema;

/**
 * @author igorm
 *
 * A concrete YAML ER schema Parser implementation
 *
 */
public class YAMLParser implements Parser {

    /* (non-Javadoc)
     * @see com.myrosh.erowl.er.Parser#parse(java.io.Reader)
     */
    public Schema parse(Reader reader) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return (Schema)mapper.readValue(reader, Schema.class);
    }
}
