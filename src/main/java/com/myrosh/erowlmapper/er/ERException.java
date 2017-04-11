package com.myrosh.erowlmapper.er;

/**
 * ER schema exception class.
 *
 * @author igorm
 */
public class ERException extends Exception {

    /**
     * @param message
     */
    public ERException(String message) {
        super("ER Schema Error: " + message);
    }
}
