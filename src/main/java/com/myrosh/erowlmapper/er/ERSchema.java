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
     * EREntity objects
     */
    private List<EREntity> entities = new ArrayList<EREntity>();

    /**
     * ERRelationship objects
     */
    private List<ERRelationship> relationships = new ArrayList<ERRelationship>();

    /**
     * Unique entities
     */
    private Set<EREntity> uniqueEntities = new HashSet<EREntity>();

    /**
     * Unique relationships
     */
    private Set<ERRelationship> uniqueRelationships = new HashSet<ERRelationship>();

    /**
     * Unique participating entities
     */
    private Set<ERParticipatingEntity> uniqueParticipatingEntities = new HashSet<ERParticipatingEntity>();

    /**
     * Unique attributes
     */
    private Set<ERAttribute> uniqueAttributes = new HashSet<ERAttribute>();

    /**
     * @return
     */
    public List<EREntity> getEntities() {
        return entities;
    }

    /**
     * @return
     */
    public EREntity getEntity(String name) {
        for (EREntity entity : entities) {
            if (entity.getName().equals(name)) {
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
    public EREntity getStrongEntity(String name) {
        for (EREntity entity : getStrongEntities()) {
            if (entity.getName().equals(name)) {
                return entity;
            }
        }

        return null;
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
     * @return
     */
    public EREntity getWeakEntity(String name) {
        for (EREntity entity : getWeakEntities()) {
            if (entity.getName().equals(name)) {
                return entity;
            }
        }

        return null;
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
     * @param aEntityName
     * @param bEntityName
     *
     * @return
     */
    public ERRelationship getIdentifyingRelationship(String aEntityName, String bEntityName) {
        for (ERRelationship relationship : relationships) {
            if (relationship.isIdentifying()
                && relationship.getParticipatingEntity(aEntityName) != null
                && relationship.getParticipatingEntity(bEntityName) != null
            ) {
                return relationship;
            }
        }

        return null;
    }

    /**
     * @param entityName
     *
     * @return
     */
    public List<ERRelationship> getIdentifyingRelationships(String entityName) {
        List<ERRelationship> identifyingRelationships = new ArrayList<ERRelationship>();

        for (ERRelationship relationship : relationships) {
            if (relationship.isIdentifying()
                && relationship.getParticipatingEntity(entityName) != null
            ) {
                identifyingRelationships.add(relationship);
            }
        }

        return identifyingRelationships;
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
                    + " must have at least 2 ParticipatingEntities.");
            }

            if (relationship.getParticipatingEntities().size() > 3) {
                throw new ERSchemaException(relationship
                    + " cannot have more than 3 ParticipatingEntities.");
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

                if (getEntity(participatingEntity.getName()) == null) {
                    throw new ERSchemaException(participatingEntity
                        + " in " + relationship
                        + " refers to EREntity(" + participatingEntity.getName() + ")"
                        + " which does not exist.");
                }
            }

            if (relationship.isIdentifying()) {
                if (!relationship.isBinary()) {
                    throw new ERSchemaException("Identifying " + relationship
                        + " must have exactly 2 ParticipatingEntities.");
                }

                List<ERParticipatingEntity> participatingEntities =
                    relationship.getParticipatingEntities();
                EREntity aEntity = getEntity(participatingEntities.get(0).getName());
                EREntity bEntity = getEntity(participatingEntities.get(1).getName());

                if (!((aEntity.isStrong() && bEntity.isWeak())
                    || (aEntity.isWeak() && bEntity.isStrong()))
                ) {
                    throw new ERSchemaException("Identifying " + relationship
                        + " must have 1 strong ERParticipatingEntity"
                        + " and 1 weak ERParticipatingEntity.");
                }
            }
        }

        for (EREntity entity : getWeakEntities()) {
            if (getIdentifyingRelationships(entity.getName()).size() != 1) {
                throw new ERSchemaException("Weak " + entity
                    + " must have exactly 1 binary identifying ERRelationship with a strong EREntity.");
            }
        }

        for (EREntity entity : entities) {
            validateAttributes(entity.getAttributes());
        }

        for (ERRelationship relationship : relationships) {
            validateAttributes(relationship.getAttributes());
        }
    }

    /**
     * @throws ERSchemaException
     *
     * Checks for schema inconsistencies.
     */
    public void validateAttributes(List<ERAttribute> attributes) throws ERSchemaException {
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
