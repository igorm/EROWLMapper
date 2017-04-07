package com.myrosh.erowl.er.schema;

import org.apache.commons.lang3.StringUtils;

import com.myrosh.erowl.Utils;

/**
 * @author igorm
 *
 * Models an ER entity participating in a relationship
 *
 */
public class ParticipatingEntity extends Element {

    /**
     * Role name
     */
    private String role;

    /**
     * Min cardinality constraint
     */
    private int min = -1;

    /**
     * Max cardinality constraint
     */
    private int max = -1;

    /**
     * @return
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return
     */
    public int getMin() {
        return min;
    }

    /**
     * @param min
     */
    public void setMin(int min) {
        this.min = min;
    }

    /**
     * @return
     */
    public int getMax() {
        return max;
    }

    /**
     * @param max
     */
    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "("
            + getUniqueName()
            + (StringUtils.isBlank(role) ? "" : "/" + Utils.lowerCaseCleanName(role))
        + ")";
    }
}
