package com.myrosh.erowlmapper.er;

import java.io.Reader;

/**
 * @author igorm
 *
 * A generic ER schema parser contract
 *
 */
public interface ERParser {

    /**
     * @param reader
     * @return
     * @throws Exception
     */
    ERSchema parse(Reader reader) throws Exception;
}
