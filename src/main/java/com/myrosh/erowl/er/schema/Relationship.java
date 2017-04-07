package com.myrosh.erowl.er.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

/**
 * @author igorm
 *
 * Models an ER relationship
 *
 */
public class Relationship extends Element {

    /**
     * Identifying flag
     */
    private boolean identifying = false;

    /**
     * Participating entity objects
     */
    private List<ParticipatingEntity> participatingEntities = new ArrayList<ParticipatingEntity>();

    /**
     * Attribute objects
     */
    private List<Attribute> attributes = new ArrayList<Attribute>();

    /**
     * @return
     */
    public boolean isIdentifying() {
        return identifying;
    }

    /**
     * @param identifying
     */
    public void setIdentifying(boolean identifying) {
        this.identifying = identifying;
    }

    /**
     * @return
     */
    public List<ParticipatingEntity> getParticipatingEntities() {
        return participatingEntities;
    }

    /**
     * @param name
     * @return
     */
    public ParticipatingEntity getParticipatingEntity(String name) {
        for (ParticipatingEntity participatingEntity : participatingEntities) {
            if (participatingEntity.getName().equals(name)) {
                return participatingEntity;
            }
        }

        return null;
    }

    /**
     * @param participatingEntity
     */
    public void addParticipatingEntity(ParticipatingEntity participatingEntity) {
        participatingEntities.add(participatingEntity);
    }

    /**
     * @return
     */
    public boolean isBinary() {
        return participatingEntities.size() == 2;
    }

    /**
     * @return
     */
    public boolean isTernary() {
        return participatingEntities.size() == 3;
    }

    /**
     * @return
     */
    public boolean isNary() {
        return participatingEntities.size() > 3;
    }

    /**
     * @return
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * @param attribute
     */
    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    @Override
    public String toString() {
        List<String> participatingEntityStrings = new ArrayList<String>();

        for (ParticipatingEntity participatingEntity : participatingEntities) {
            participatingEntityStrings.add(participatingEntity.toString());
        }

        Collections.sort(participatingEntityStrings);

        return getClass().getSimpleName() + "("
            + StringUtils.join(participatingEntityStrings, ',') + ")";
    }
}
