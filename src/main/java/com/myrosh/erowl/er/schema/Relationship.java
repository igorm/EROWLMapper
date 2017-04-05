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
public class Relationship {

    /**
     * Name
     */
    private String name;

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

        return StringUtils.join(participatingEntityStrings, ',');
    }
}
