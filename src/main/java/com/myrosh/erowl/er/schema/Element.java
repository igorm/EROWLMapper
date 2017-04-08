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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (that == null) {
            return false;
        }

        if (getClass() != that.getClass()) {
            return false;
        }

        Element element = (Element)that;

        return getUniqueName().equals(element.getUniqueName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getUniqueName() + ")";
    }
}
