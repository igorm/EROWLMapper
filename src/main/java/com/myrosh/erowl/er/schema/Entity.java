package com.myrosh.erowl.er.schema;

import java.util.ArrayList;
import java.util.List;

import com.myrosh.erowl.Utils;

/**
 * @author igorm
 *
 * Models an ER entity
 *
 */
public class Entity extends Element {

    /**
     * Weak flag
     */
    private boolean weak = false;

    /**
     * Attribute objects
     */
    private List<Attribute> attributes = new ArrayList<Attribute>();

    /**
     * @return
     */
    public boolean isWeak() {
        return weak;
    }

    /**
     * @return
     */
    public boolean isStrong() {
        return !weak;
    }

    /**
     * @param weak
     */
    public void setWeak(boolean weak) {
        this.weak = weak;
    }

    /**
     * @return
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * @return
     */
    public List<Attribute> getKeyAttributes() {
        List<Attribute> keyAttributes = new ArrayList<Attribute>();

        for (Attribute attribute : attributes) {
            if (attribute.isKey()) {
                keyAttributes.add(attribute);
            }
        }

        return keyAttributes;
    }

    /**
     * @return
     */
    public List<Attribute> getNonKeyAttributes() {
        List<Attribute> nonKeyAttributes = new ArrayList<Attribute>();

        for (Attribute attribute : attributes) {
            if (!attribute.isKey()) {
                nonKeyAttributes.add(attribute);
            }
        }

        return nonKeyAttributes;
    }

    /**
     * @param attribute
     */
    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }
}
