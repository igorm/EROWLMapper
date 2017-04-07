package com.myrosh.erowl.er.schema;

import com.myrosh.erowl.Utils;

/**
 * @author igorm
 *
 * Models an ER schema element
 *
 */
public abstract class Element {

    /**
     * Name
     */
    private String name;

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public String getUniqueName() {
        return Utils.lowerCaseCleanName(name);
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
