package com.myrosh.erowl.er;

import java.io.Reader;

import com.myrosh.erowl.er.schema.Schema;

/**
 * @author igorm
 *
 * A generic ER schema Parser contract
 *
 */
public interface Parser {

    /**
     * @param reader
     * @return
     * @throws Exception
     */
    Schema parse(Reader reader) throws Exception;
}
