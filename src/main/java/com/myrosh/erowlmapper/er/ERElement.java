package com.myrosh.erowlmapper.er;

import com.myrosh.erowlmapper.Utils;

import java.util.Objects;

/**
 * @author igorm
 *
 * Models an ER schema element
 *
 */
public abstract class ERElement {

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

        ERElement element = (ERElement)that;

        return getUniqueName().equals(element.getUniqueName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUniqueName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + getUniqueName() + "}";
    }
}
