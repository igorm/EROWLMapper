package com.myrosh.erowlmapper.er;

import java.util.ArrayList;
import java.util.List;

/**
 * @author igorm
 *
 * Models an ER entity
 *
 */
public class EREntity extends ERElement {

    /**
     * Weak flag
     */
    private boolean weak = false;

    /**
     * ERAttribute objects
     */
    private List<ERAttribute> attributes = new ArrayList<ERAttribute>();

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
    public List<ERAttribute> getAttributes() {
        return attributes;
    }

    /**
     * @return
     */
    public List<ERAttribute> getKeyAttributes() {
        List<ERAttribute> keyAttributes = new ArrayList<ERAttribute>();

        for (ERAttribute attribute : attributes) {
            if (attribute.isKey()) {
                keyAttributes.add(attribute);
            }
        }

        return keyAttributes;
    }

    /**
     * @return
     */
    public List<ERAttribute> getNonKeyAttributes() {
        List<ERAttribute> nonKeyAttributes = new ArrayList<ERAttribute>();

        for (ERAttribute attribute : attributes) {
            if (!attribute.isKey()) {
                nonKeyAttributes.add(attribute);
            }
        }

        return nonKeyAttributes;
    }

    /**
     * @param attribute
     */
    public void addAttribute(ERAttribute attribute) {
        attributes.add(attribute);
    }
}
