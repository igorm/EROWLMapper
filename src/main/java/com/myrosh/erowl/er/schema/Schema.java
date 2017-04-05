package com.myrosh.erowl.er.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.myrosh.erowl.er.InconsistentSchemaException;

/**
 * @author igorm
 *
 * Models an ER schema
 *
 */
public class Schema {

    /**
     * Entity objects
     */
    private List<Entity> entities = new ArrayList<Entity>();

    /**
     * Relationship objects
     */
    private List<Relationship> relationships = new ArrayList<Relationship>();

    Set<String> entityNames = new HashSet<String>();

    Set<String> attributeNames = new HashSet<String>();

    Set<String> relationshipNames = new HashSet<String>();

    /**
     * @return
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * @return
     */
    public Entity getEntity(String name) {
        for (Entity entity : entities) {
            if (entity.getName().equals(name)) {
                return entity;
            }
        }

        return null;
    }

    /**
     * @return
     */
    public List<Entity> getStrongEntities() {
        List<Entity> strongEntities = new ArrayList<Entity>();

        for (Entity entity : entities) {
            if (entity.isStrong()) {
                strongEntities.add(entity);
            }
        }

        return strongEntities;
    }

    /**
     * @return
     */
    public Entity getStrongEntity(String name) {
        for (Entity entity : getStrongEntities()) {
            if (entity.getName().equals(name)) {
                return entity;
            }
        }

        return null;
    }

    /**
     * @return
     */
    public List<Entity> getWeakEntities() {
        List<Entity> weakEntities = new ArrayList<Entity>();

        for (Entity entity : entities) {
            if (entity.isWeak()) {
                weakEntities.add(entity);
            }
        }

        return weakEntities;
    }

    /**
     * @param entity
     */
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    /**
     * @return
     */
    public List<Relationship> getRelationships() {
        return relationships;
    }

    /**
     * @param aEntityName
     * @param bEntityName
     *
     * @return
     */
    public Relationship getIdentifyingRelationship(String aEntityName, String bEntityName) {
        for (Relationship relationship : relationships) {
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
    public List<Relationship> getIdentifyingRelationships(String entityName) {
        List<Relationship> identifyingRelationships = new ArrayList<Relationship>();

        for (Relationship relationship : relationships) {
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
    public void addRelationship(Relationship relationship) {
        relationships.add(relationship);
    }

    /**
     * @throws InconsistentSchemaException
     *
     * Checks for schema inconsistencies.
     */
    public void validate() throws InconsistentSchemaException {
        for (Entity entity : entities) {
            if (StringUtils.isBlank(entity.getName())) {
                throw new InconsistentSchemaException("Every entity must have a name.");
            }

            if (entityNames.contains(entity.getName())) {
                throw new InconsistentSchemaException("Entity " + entity.getName()
                    + " is a duplicate.");
            } else {
                entityNames.add(entity.getName());
            }

            if (entity.isWeak()) {
                if (StringUtils.isBlank(entity.getOwnerName())) {
                    throw new InconsistentSchemaException("Weak entity " + entity.getName()
                        + " must have an owner name.");
                }

                if (getStrongEntity(entity.getOwnerName()) == null) {
                    throw new InconsistentSchemaException("Weak entity " + entity.getName()
                        + " refers to owner entity " + entity.getOwnerName()
                        + " which does not exist or is not strong.");
                }
            }

            validateAttributes(entity.getAttributes());
        }

        Set<String> relationshipStrings = new HashSet<String>();

        for (Relationship relationship : relationships) {
            if (StringUtils.isBlank(relationship.getName())) {
                throw new InconsistentSchemaException("Every relationship must have a name.");
            }

            if (relationshipNames.contains(relationship.getName())) {
                throw new InconsistentSchemaException("Relationship " + relationship.getName()
                    + " is a duplicate.");
            } else {
                relationshipNames.add(relationship.getName());
            }

            if (relationship.getParticipatingEntities().size() < 2) {
                throw new InconsistentSchemaException("Relationship " + relationship.getName()
                    + " must have at least 2 participating entities.");
            }

            if (relationship.getParticipatingEntities().size() > 3) {
                throw new InconsistentSchemaException("Relationship " + relationship.getName()
                    + " must have at most 3 participating entities.");
            }

            for (ParticipatingEntity participatingEntity : relationship.getParticipatingEntities()) {
                if (StringUtils.isBlank(participatingEntity.getName())) {
                    throw new InconsistentSchemaException(
                        "Every participating entity must have a name.");
                }

                if (getEntity(participatingEntity.getName()) == null) {
                    throw new InconsistentSchemaException(
                        "Participating entity refers to entity " + participatingEntity.getName()
                        + " which does not exist.");
                }
            }

            if (relationship.isIdentifying()) {
                if (relationship.getParticipatingEntities().size() != 2) {
                    throw new InconsistentSchemaException("Identifying relationship "
                        + relationship.getName() + " must have exactly 2 participating entities.");
                }

                List<ParticipatingEntity> participatingEntities =
                    relationship.getParticipatingEntities();
                Entity aEntity = getEntity(participatingEntities.get(0).getName());
                Entity bEntity = getEntity(participatingEntities.get(1).getName());

                if (!((aEntity.isStrong() && bEntity.isWeak())
                    || (aEntity.isWeak() && bEntity.isStrong()))
                ) {
                    throw new InconsistentSchemaException("Identifying relationship "
                        + relationship.getName() + " must have 1 strong participating entity"
                        + " and 1 weak participating entity.");
                }
            }

            if (relationshipStrings.contains(relationship.toString())) {
                throw new InconsistentSchemaException("Relationship " + relationship.getName()
                    + " is a duplicate based on its participating entities and their roles.");
            } else {
                relationshipStrings.add(relationship.toString());
            }

            validateAttributes(relationship.getAttributes());
        }

        for (Entity entity : getWeakEntities()) {
            if (getIdentifyingRelationships(entity.getName()).size() != 1) {
                throw new InconsistentSchemaException("Weak entity " + entity.getName()
                    + " must have exactly 1 identifying relationship with a strong entity.");
            }
        }
    }

    /**
     * @throws InconsistentSchemaException
     *
     * Checks for schema inconsistencies.
     */
    public void validateAttributes(List<Attribute> attributes) throws InconsistentSchemaException {
        for (Attribute attribute : attributes) {
            if (StringUtils.isBlank(attribute.getName())) {
                throw new InconsistentSchemaException("Every attribute must have a name.");
            }

            if (attributeNames.contains(attribute.getName())) {
                throw new InconsistentSchemaException("Attribute " + attribute.getName()
                    + " is a duplicate.");
            } else {
                attributeNames.add(attribute.getName());
            }

            if (attribute.isKey() && attribute.isMultivalued()) {
                throw new InconsistentSchemaException("Key attribute " + attribute.getName()
                    + " cannot be multivalued.");
            }

            for (Attribute componentAttribute : attribute.getComponentAttributes()) {
                if (StringUtils.isBlank(componentAttribute.getName())) {
                    throw new InconsistentSchemaException("Every attribute must have a name.");
                }

                if (attributeNames.contains(componentAttribute.getName())) {
                    throw new InconsistentSchemaException(
                        "Attribute " + componentAttribute.getName() + " is a duplicate.");
                } else {
                    attributeNames.add(componentAttribute.getName());
                }

                if (componentAttribute.isKey()) {
                    throw new InconsistentSchemaException("Component attribute "
                        + attribute.getName() + " cannot be a key.");
                }

                if (componentAttribute.isComposite()) {
                    throw new InconsistentSchemaException("Component attribute "
                        + attribute.getName() + " cannot be composite.");
                }
            }
        }
    }
}
