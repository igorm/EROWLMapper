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
     * @return
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * @return
     */
    public List<Entity> getStrongEntities() {
        List<Entity> strongEntities = new ArrayList<Entity>();

        for (Entity entity : entities) {
            if (!entity.isWeak()) {
                strongEntities.add(entity);
            }
        }

        return strongEntities;
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
     * @param relationship
     */
    public void addRelationship(Relationship relationship) {
        relationships.add(relationship);
    }

    /**
     * @throws Exception
     *
     * Checks for schema inconsistencies.
     */
    public void validate() throws InconsistentSchemaException {
        Set<String> entityNames = new HashSet<String>();
        Set<String> attributeNames = new HashSet<String>();

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

            for (Attribute attribute : entity.getAttributes()) {
                if (StringUtils.isBlank(attribute.getName())) {
                    throw new InconsistentSchemaException("Every attribute must have a name.");
                }

                if (attributeNames.contains(attribute.getName())) {
                    throw new InconsistentSchemaException("Attribute " + attribute.getName()
                        + " is a duplicate.");
                } else {
                    attributeNames.add(attribute.getName());
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
                }
            }
        }
    }
}
