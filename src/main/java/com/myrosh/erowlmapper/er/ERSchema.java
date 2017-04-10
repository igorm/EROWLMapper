package com.myrosh.erowlmapper.er;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author igorm
 *
 * Models an ER schema
 *
 */
public class ERSchema {

    /**
     * Entities
     */
    private List<EREntity> entities = new ArrayList<EREntity>();

    /**
     * Relationships
     */
    private List<ERRelationship> relationships = new ArrayList<ERRelationship>();

    /**
     * @return
     */
    public List<EREntity> getEntities() {
        return entities;
    }

    /**
     * @param participatingEntity
     * @return
     */
    public EREntity getEntity(ERParticipatingEntity participatingEntity) {
        for (EREntity entity : entities) {
            if (entity.getUniqueName().equals(participatingEntity.getUniqueName())) {
                return entity;
            }
        }

        return null;
    }

    /**
     * @return
     */
    public List<EREntity> getStrongEntities() {
        List<EREntity> strongEntities = new ArrayList<EREntity>();

        for (EREntity entity : entities) {
            if (entity.isStrong()) {
                strongEntities.add(entity);
            }
        }

        return strongEntities;
    }

    /**
     * @return
     */
    public List<EREntity> getWeakEntities() {
        List<EREntity> weakEntities = new ArrayList<EREntity>();

        for (EREntity entity : entities) {
            if (entity.isWeak()) {
                weakEntities.add(entity);
            }
        }

        return weakEntities;
    }

    /**
     * @param entity
     */
    public void addEntity(EREntity entity) {
        entities.add(entity);
    }

    /**
     * @return
     */
    public List<ERRelationship> getRelationships() {
        return relationships;
    }

    /**
     * @param weakEntity
     *
     * @return
     */
    public ERRelationship getIdentifyingBinaryRelationship(EREntity weakEntity) {
        List<ERRelationship> identifyingRelationships = new ArrayList<ERRelationship>();

        for (ERRelationship relationship : relationships) {
            if (relationship.isIdentifying()
                && relationship.isBinary()
                && relationship.getParticipatingEntity(weakEntity) != null
            ) {
                identifyingRelationships.add(relationship);
            }
        }

        return identifyingRelationships.size() == 1 ? identifyingRelationships.get(0) : null;
    }

    /**
     * @param relationship
     */
    public void addRelationship(ERRelationship relationship) {
        relationships.add(relationship);
    }

    /**
     * @throws ERSchemaException
     *
     * Checks for schema inconsistencies.
     */
    public void validate() throws ERSchemaException {
        Set<EREntity> uniqueEntities = new HashSet<EREntity>();
        Set<ERRelationship> uniqueRelationships = new HashSet<ERRelationship>();
        Set<ERParticipatingEntity> uniqueParticipatingEntities = new HashSet<ERParticipatingEntity>();

        for (EREntity entity : entities) {
            if (StringUtils.isBlank(entity.getUniqueName())) {
                throw new ERSchemaException("Every EREntity must have a name.");
            }

            if (uniqueEntities.contains(entity)) {
                throw new ERSchemaException(entity + " is a duplicate.");
            } else {
                uniqueEntities.add(entity);
            }
        }

        for (ERRelationship relationship : relationships) {
            if (StringUtils.isBlank(relationship.getUniqueName())) {
                throw new ERSchemaException("Every ERRelationship must have a name.");
            }

            if (uniqueRelationships.contains(relationship)) {
                throw new ERSchemaException(relationship + " is a duplicate.");
            } else {
                uniqueRelationships.add(relationship);
            }

            if (relationship.getParticipatingEntities().size() < 2) {
                throw new ERSchemaException(relationship
                    + " must have at least 2 ERParticipatingEntities.");
            }

            if (relationship.getParticipatingEntities().size() > 3) {
                throw new ERSchemaException(relationship
                    + " cannot have more than 3 ERParticipatingEntities.");
            }

            for (ERParticipatingEntity participatingEntity : relationship.getParticipatingEntities()) {
                if (StringUtils.isBlank(participatingEntity.getUniqueName())) {
                    throw new ERSchemaException(
                        "Every ERParticipatingEntity must have a name.");
                }

                if (uniqueParticipatingEntities.contains(participatingEntity)) {
                    throw new ERSchemaException(participatingEntity
                        + " in " + relationship + " is a duplicate.");
                } else {
                    uniqueParticipatingEntities.add(participatingEntity);
                }

                if (getEntity(participatingEntity) == null) {
                    throw new ERSchemaException(participatingEntity
                        + " in " + relationship
                        + " refers to EREntity(" + participatingEntity.getUniqueName() + ")"
                        + " which does not exist.");
                }
            }

            if (relationship.isIdentifying()) {
                if (!relationship.isBinary()) {
                    throw new ERSchemaException("Identifying " + relationship
                        + " must have exactly 2 ERParticipatingEntities.");
                }

                List<ERParticipatingEntity> participatingEntities =
                    relationship.getParticipatingEntities();
                EREntity aEntity = getEntity(participatingEntities.get(0));
                EREntity bEntity = getEntity(participatingEntities.get(1));

                if (!((aEntity.isStrong() && bEntity.isWeak())
                    || (aEntity.isWeak() && bEntity.isStrong()))
                ) {
                    throw new ERSchemaException("Identifying " + relationship
                        + " must have 1 strong ERParticipatingEntity"
                        + " and 1 weak ERParticipatingEntity.");
                }

                if (!relationship.getAttributes().isEmpty()) {
                    throw new ERSchemaException("Identifying " + relationship
                        + " cannot have attributes.");
                }
            }
        }

        for (EREntity weakEntity : getWeakEntities()) {
            if (getIdentifyingBinaryRelationship(weakEntity) == null) {
                throw new ERSchemaException("Weak " + weakEntity
                    + " must have exactly 1 identifying binary ERRelationship with a strong EREntity.");
            }
        }

        List<ERAttribute> attributes = new ArrayList<ERAttribute>();
        Set<ERAttribute> uniqueAttributes = new HashSet<ERAttribute>();

        for (EREntity entity : entities) {
            attributes.addAll(entity.getAttributes());
        }

        for (ERRelationship relationship : relationships) {
            attributes.addAll(relationship.getAttributes());
        }

        for (ERAttribute attribute : attributes) {
            if (StringUtils.isBlank(attribute.getUniqueName())) {
                throw new ERSchemaException("Every ERAttribute must have a name.");
            }

            if (uniqueAttributes.contains(attribute)) {
                throw new ERSchemaException(attribute + " is a duplicate.");
            } else {
                uniqueAttributes.add(attribute);
            }

            if (attribute.isKey() && attribute.isMultivalued()) {
                throw new ERSchemaException("Key " + attribute
                    + " cannot be multivalued.");
            }

            if (attribute.isComposite()) {
                for (EREntity entity : uniqueEntities) {
                    if (entity.getUniqueName().equals(attribute.getUniqueName())) {
                        throw new ERSchemaException("Composite " + attribute
                            + " cannot have the same name as " + entity + ".");
                    }
                }
            }

            for (ERAttribute componentAttribute : attribute.getComponentAttributes()) {
                if (StringUtils.isBlank(componentAttribute.getUniqueName())) {
                    throw new ERSchemaException("Every ERAttribute must have a name.");
                }

                if (uniqueAttributes.contains(componentAttribute)) {
                    throw new ERSchemaException(componentAttribute + " is a duplicate.");
                } else {
                    uniqueAttributes.add(componentAttribute);
                }

                if (componentAttribute.isKey()) {
                    throw new ERSchemaException("Component " + attribute
                        + " cannot be a key.");
                }

                if (componentAttribute.isComposite()) {
                    throw new ERSchemaException("Component " + attribute
                        + " cannot be composite.");
                }
            }
        }
    }
}
