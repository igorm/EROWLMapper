package edu.sfsu.ertools.domain.logic.parser;

import java.io.Reader;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;

import edu.sfsu.ertools.domain.Schema;
import edu.sfsu.ertools.domain.logic.Parser;

/**
 * @author igorm
 *
 * A concrete ER schema Parser implementation 
 * 
 */
public class DigesterParser implements Parser {
	
	/**
	 * Digester object
	 */
	private Digester digester;
	
	/**
	 * @param rules
	 */
	public DigesterParser(URL rules) {
		digester = DigesterLoader.createDigester(rules);
	}
	
	/* (non-Javadoc)
	 * @see edu.sfsu.ertools.domain.logic.Parser#parse(java.io.Reader)
	 */
	public Schema parse(Reader reader) throws Exception {
		return (Schema)digester.parse(reader);
	}
}
