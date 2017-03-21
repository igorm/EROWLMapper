package edu.sfsu.ertools.domain.logic;

import java.io.Writer;

import edu.sfsu.ertools.domain.Schema;

/**
 * @author igorm
 * 
 * A generic Mapper contract
 *
 */
public interface Mapper {
	
	/**
	 * @param schema
	 * @param writer
	 * @throws Exception
	 */
	void map(Schema schema, Writer writer) throws Exception;
}
