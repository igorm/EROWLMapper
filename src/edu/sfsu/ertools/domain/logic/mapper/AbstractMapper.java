package edu.sfsu.ertools.domain.logic.mapper;

import edu.sfsu.ertools.domain.logic.Mapper;

/**
 * @author igor
 *
 * An abstract Mapper with shared functionality
 *
 */
public abstract class AbstractMapper implements Mapper {
	
	/**
	 * @param string
	 * @return
	 */
	protected String upperCaseFirstChar(String string) {
		char[] chars = string.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}
	
	/**
	 * @param string
	 * @return
	 */
	protected String lowerCaseFirstChar(String string) {
		char[] chars = string.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}
}
