package com.myrosh.erowl.er.schema;

import java.util.ArrayList;
import java.util.List;

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
     * @param entity
     */
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    /**
     * @param relationship
     */
    public void addRelationship(Relationship relationship) {
        relationships.add(relationship);
    }

    /**
     * @return
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * @return
     */
    public List<Relationship> getRelationships() {
        return relationships;
    }

    /**
     * @param entityName1
     * @param entityName2
     *
     * TODO More than one relationship may contain the two entities
     * that are being passed in. This method needs to be updated.
     *
     * @return
     */
    public Relationship getRelationship(String entityName1, String entityName2) {
        for (Relationship relationship : relationships) {
            if (relationship.getParticipatingEntity(entityName1) != null &&
                    relationship.getParticipatingEntity(entityName2) != null)
                        return relationship;
        }
        return null;
    }
}
