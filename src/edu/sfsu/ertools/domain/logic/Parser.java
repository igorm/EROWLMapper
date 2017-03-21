package edu.sfsu.ertools.domain.logic;

import java.io.Reader;

import edu.sfsu.ertools.domain.Schema;

/**
 * @author igorm
 *
 * A generic Parser contract
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
