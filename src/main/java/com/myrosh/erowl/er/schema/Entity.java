package com.myrosh.erowl.er.schema;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author igorm
 *
 * Models an ER entity
 *
 */
public class Entity {

    /**
     * Name
     */
    private String name;

    /**
     * Weak flag
     */
    private boolean weak = false;

    /**
     * Owner name
     */
    private String ownerName;

    /**
     * Attribute objects
     */
    private List<Attribute> attributes = new ArrayList<Attribute>();

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public boolean isWeak() {
        return weak;
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
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * @param ownerName
     */
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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
