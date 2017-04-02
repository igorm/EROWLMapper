package com.myrosh.erowl.er;

/**
 * Inconsistent schema exception class.
 *
 * @author igor
 */
public class InconsistentSchemaException extends Exception {

    /**
     * @param message
     */
    public InconsistentSchemaException(String message) {
        super("Inconsistent schema: " + message);
    }
}
