package com.myrosh.erowl.er.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * @author igorm
 *
 * Models an ER entity/relationship/component attribute
 *
 */
public class Attribute {

    /**
     * Name
     */
    private String name;

    /**
     * Key
     */
    private boolean key = false;

    /**
     * Component attribute objects
     */
    private List<Attribute> componentAttributes = new ArrayList<Attribute>();

    /**
     * @return
     */
    public boolean isComposite() {
        return !componentAttributes.isEmpty();
    }

    /**
     * @param attribute
     */
    public void addComponentAttribute(Attribute attribute) {
        componentAttributes.add(attribute);
    }

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
    public List<Attribute> getComponentAttributes() {
        return componentAttributes;
    }
}
