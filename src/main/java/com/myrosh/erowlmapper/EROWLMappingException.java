package com.myrosh.erowlmapper;

/**
 * Mapping exception class.
 *
 * @author igorm
 */
public class EROWLMappingException extends Exception {

    /**
     * @param message
     */
    public EROWLMappingException(String message) {
        super("ER-OWL Mapping Error: " + message);
    }
}
