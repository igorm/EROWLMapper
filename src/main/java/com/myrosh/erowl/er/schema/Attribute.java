package com.myrosh.erowl.er.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * @author igorm
 *
 * Models an ER entity/relationship/component attribute
 *
 */
public class Attribute extends Element {

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
     * Component attribute objects
     */
    private List<Attribute> componentAttributes = new ArrayList<Attribute>();

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

    /**
     * @return
     */
    public List<Attribute> getComponentAttributes() {
        return componentAttributes;
    }

    /**
     * @param attribute
     */
    public void addComponentAttribute(Attribute attribute) {
        componentAttributes.add(attribute);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getUniqueName() + ")";
    }
}
