package com.myrosh.erowlmapper.owl;

/**
 * OWL ontology exception class.
 *
 * @author igorm
 */
public class OWLException extends Exception {

    /**
     * @param message
     */
    public OWLException(String message) {
        super("OWL Ontology Error: " + message);
    }
}
