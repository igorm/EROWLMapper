package com.myrosh.erowlmapper.er;

/**
 * ER schema exception class.
 *
 * @author igorm
 */
public class ERSchemaException extends Exception {

    /**
     * @param message
     */
    public ERSchemaException(String message) {
        super("ER Schema Error: " + message);
    }
}
