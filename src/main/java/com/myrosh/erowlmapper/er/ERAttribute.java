package com.myrosh.erowlmapper.er;

import java.util.ArrayList;
import java.util.List;

/**
 * @author igorm
 *
 * Models an ER entity/relationship/component attribute
 *
 */
public class ERAttribute extends ERElementWithAttributes {

    /**
     * Key flag
     */
    private boolean key = false;

    /**
     * Composite flag
     */
    private boolean composite = false;

    /**
     * Multivalued flag
     */
    private boolean multivalued = false;

    /**
     * @return
     */
    public boolean isKey() {
        return key;
    }

    /**
     * @param key
     */
    public void setKey(boolean key) {
        this.key = key;
    }

    /**
     * @return
     */
    public boolean isComposite() {
        return composite;
    }

    /**
     * @param composite
     */
    public void setComposite(boolean composite) {
        this.composite = composite;
    }

    /**
     * @return
     */
    public boolean isMultivalued() {
        return multivalued;
    }

    /**
     * @param multivalued
     */
    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }
}
