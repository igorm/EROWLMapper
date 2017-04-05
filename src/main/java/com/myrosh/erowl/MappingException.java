package com.myrosh.erowl;

/**
 * Mapping exception class.
 *
 * @author igor
 */
public class MappingException extends Exception {

    /**
     * @param message
     */
    public MappingException(String message) {
        super("Mapping error: " + message);
    }
}
