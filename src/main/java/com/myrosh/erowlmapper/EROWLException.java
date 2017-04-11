package com.myrosh.erowlmapper;

/**
 * Mapping exception class.
 *
 * @author igorm
 */
public class EROWLException extends Exception {

    /**
     * @param message
     */
    public EROWLException(String message) {
        super("ER-OWL Mapping Error: " + message);
    }
}
