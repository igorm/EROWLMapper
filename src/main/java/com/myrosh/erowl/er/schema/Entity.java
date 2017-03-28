package com.myrosh.erowl.er.schema;

import java.util.ArrayList;
import java.util.List;

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
    private String owner;

    /**
     * Attribute objects
     */
    private List<Attribute> attributes = new ArrayList<Attribute>();

    /**
     * @param attribute
     */
    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

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
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * @return
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }
}
