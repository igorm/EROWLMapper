package com.myrosh.erowlmapper.er;

import java.util.ArrayList;
import java.util.List;

/**
 * @author igorm
 *
 * Models an ER schema element with attributes
 *
 */
public abstract class ERElementWithAttributes extends ERElement {

    /**
     * Attributes
     */
    private List<ERAttribute> attributes = new ArrayList<ERAttribute>();

    public List<ERAttribute> getAttributes() {
        return attributes;
    }

    /**
     * @param attribute
     */
    public void addAttribute(ERAttribute attribute) {
        attributes.add(attribute);
    }
}
