package com.myrosh.erowlmapper.er;

import com.myrosh.erowlmapper.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author igorm
 *
 * Models an ER entity participating in a relationship
 *
 */
public class ERParticipatingEntity extends ERElement {

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
     * @return
     */
    public String getUniqueRole() {
        return Utils.lowerCaseCleanName(role);
    }

    /**
     * @return
     */
    public String getRoleOrName() {
        return StringUtils.isBlank(getUniqueRole()) ? getName() : getRole();
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

        ERParticipatingEntity participatingEntity = (ERParticipatingEntity)that;

        return getUniqueName().equals(participatingEntity.getUniqueName())
            && getUniqueRole().equals(participatingEntity.getUniqueRole());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUniqueName(), getUniqueRole());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + getUniqueName()
            + (StringUtils.isBlank(role) ? "" : "/" + getUniqueRole()) + "}";
    }
}
