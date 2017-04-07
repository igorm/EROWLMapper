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

    /**
     * Unique entity names
     */
    Set<String> entityNames = new HashSet<String>();

    /**
     * Unique attribute names
     */
    Set<String> attributeNames = new HashSet<String>();

    /**
     * Unique relationship names
     */
    Set<String> relationshipNames = new HashSet<String>();

    /**
     * Unique relationship string representations
     */
    Set<String> relationshipStrings = new HashSet<String>();

    /**
     * Unique participating entity string representations
     */
    Set<String> participatingEntityStrings = new HashSet<String>();

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
            if (StringUtils.isBlank(entity.getUniqueName())) {
                throw new InconsistentSchemaException("Every Entity must have a name.");
            }

            if (entityNames.contains(entity.getUniqueName())) {
                throw new InconsistentSchemaException(entity + " is a duplicate.");
            } else {
                entityNames.add(entity.getUniqueName());
            }

            if (entity.isWeak()) {
                if (StringUtils.isBlank(entity.getUniqueOwnerName())) {
                    throw new InconsistentSchemaException("Weak " + entity
                        + " must have an owner name.");
                }

                if (getStrongEntity(entity.getOwnerName()) == null) {
                    throw new InconsistentSchemaException("Weak " + entity
                        + " refers to owner Entity(" + entity.getOwnerName() + ")"
                        + " which does not exist or is not a strong Entity.");
                }
            }
        }

        for (Relationship relationship : relationships) {
            if (StringUtils.isBlank(relationship.getUniqueName())) {
                throw new InconsistentSchemaException("Every Relationship must have a name.");
            }

            if (relationshipNames.contains(relationship.getUniqueName())) {
                throw new InconsistentSchemaException(
                    "Relationship(" + relationship.getUniqueName() + ") is a duplicate.");
            } else {
                relationshipNames.add(relationship.getUniqueName());
            }

            if (relationship.getParticipatingEntities().size() < 2) {
                throw new InconsistentSchemaException(
                    "Relationship(" + relationship.getUniqueName() + ")"
                    + " must have at least 2 participating entities.");
            }

            if (relationship.getParticipatingEntities().size() > 3) {
                throw new InconsistentSchemaException(
                    "Relationship(" + relationship.getUniqueName() + ")"
                    + " cannot have more than 3 participating entities.");
            }

            for (ParticipatingEntity participatingEntity : relationship.getParticipatingEntities()) {
                if (StringUtils.isBlank(participatingEntity.getUniqueName())) {
                    throw new InconsistentSchemaException(
                        "Every ParticipatingEntity must have a name.");
                }

                if (getEntity(participatingEntity.getName()) == null) {
                    throw new InconsistentSchemaException(participatingEntity
                        + " in Relationship(" + relationship.getUniqueName() + ")"
                        + " refers to Entity(" + participatingEntity.getName() + ")"
                        + " which does not exist.");
                }

                if (participatingEntityStrings.contains(participatingEntity.toString())) {
                    throw new InconsistentSchemaException(participatingEntity
                        + " in Relationship(" + relationship.getUniqueName()+ ")"
                        + " is a duplicate. Please specify a role.");
                } else {
                    participatingEntityStrings.add(participatingEntity.toString());
                }
            }

            if (relationship.isIdentifying()) {
                if (!relationship.isBinary()) {
                    throw new InconsistentSchemaException(
                        "Identifying Relationship(" + relationship.getUniqueName() + ")"
                        + " must have exactly 2 ParticipatingEntities.");
                }

                List<ParticipatingEntity> participatingEntities =
                    relationship.getParticipatingEntities();
                Entity aEntity = getEntity(participatingEntities.get(0).getName());
                Entity bEntity = getEntity(participatingEntities.get(1).getName());

                if (!((aEntity.isStrong() && bEntity.isWeak())
                    || (aEntity.isWeak() && bEntity.isStrong()))
                ) {
                    throw new InconsistentSchemaException(
                        "Identifying Relationship(" + relationship.getUniqueName() + ")"
                        + " must have 1 strong ParticipatingEntity"
                        + " and 1 weak ParticipatingEntity.");
                }
            }

            if (relationshipStrings.contains(relationship.toString())) {
                throw new InconsistentSchemaException(relationship
                    + " is a duplicate based on its ParticipatingEntities and their roles.");
            } else {
                relationshipStrings.add(relationship.toString());
            }
        }

        for (Entity entity : getWeakEntities()) {
            if (getIdentifyingRelationships(entity.getName()).size() != 1) {
                throw new InconsistentSchemaException("Weak " + entity
                    + " must have exactly 1 identifying Relationship with a strong Entity.");
            }
        }

        for (Entity entity : entities) {
            validateAttributes(entity.getAttributes());
        }

        for (Relationship relationship : relationships) {
            validateAttributes(relationship.getAttributes());
        }
    }

    /**
     * @throws InconsistentSchemaException
     *
     * Checks for schema inconsistencies.
     */
    public void validateAttributes(List<Attribute> attributes) throws InconsistentSchemaException {
        for (Attribute attribute : attributes) {
            if (StringUtils.isBlank(attribute.getUniqueName())) {
                throw new InconsistentSchemaException("Every Attribute must have a name.");
            }

            if (attributeNames.contains(attribute.getUniqueName())) {
                throw new InconsistentSchemaException(attribute + " is a duplicate.");
            } else {
                attributeNames.add(attribute.getUniqueName());
            }

            if (attribute.isKey() && attribute.isMultivalued()) {
                throw new InconsistentSchemaException("Key " + attribute
                    + " cannot be multivalued.");
            }

            if (attribute.isComposite() && entityNames.contains(attribute.getUniqueName())) {
                throw new InconsistentSchemaException("Composite " + attribute
                    + " cannot have the same name as an entity.");
            }

            for (Attribute componentAttribute : attribute.getComponentAttributes()) {
                if (StringUtils.isBlank(componentAttribute.getUniqueName())) {
                    throw new InconsistentSchemaException("Every Attribute must have a name.");
                }

                if (attributeNames.contains(componentAttribute.getUniqueName())) {
                    throw new InconsistentSchemaException(componentAttribute + " is a duplicate.");
                } else {
                    attributeNames.add(componentAttribute.getUniqueName());
                }

                if (componentAttribute.isKey()) {
                    throw new InconsistentSchemaException("Component " + attribute
                        + " cannot be a key.");
                }

                if (componentAttribute.isComposite()) {
                    throw new InconsistentSchemaException("Component " + attribute
                        + " cannot be composite.");
                }
            }
        }
    }
}
