package com.myrosh.erowlmapper.er;

import java.util.ArrayList;
import java.util.List;

/**
 * @author igorm
 *
 * Models an ER relationship
 *
 */
public class ERRelationship extends ERElementWithAttributes {

    /**
     * Identifying flag
     */
    private boolean identifying = false;

    /**
     * Participating entities
     */
    private List<ERParticipatingEntity> participatingEntities = new ArrayList<ERParticipatingEntity>();

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
    public List<ERParticipatingEntity> getParticipatingEntities() {
        return participatingEntities;
    }

    /**
     * @param entity
     * @return
     */
    public ERParticipatingEntity getParticipatingEntity(EREntity entity) {
        for (ERParticipatingEntity participatingEntity : participatingEntities) {
            if (participatingEntity.getUniqueName().equals(entity.getUniqueName())) {
                return participatingEntity;
            }
        }

        return null;
    }

    /**
     * @param entity
     * @return
     */
    public List<ERParticipatingEntity> getParticipatingEntitiesExcluding(EREntity entity) {
        List<ERParticipatingEntity> filteredParticipatingEntities = new ArrayList<ERParticipatingEntity>();

        for (ERParticipatingEntity participatingEntity : participatingEntities) {
            if (!participatingEntity.getUniqueName().equals(entity.getUniqueName())) {
                filteredParticipatingEntities.add(participatingEntity);
            }
        }

        return filteredParticipatingEntities;
    }

    /**
     * @param participatingEntity
     */
    public void addParticipatingEntity(ERParticipatingEntity participatingEntity) {
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
}
