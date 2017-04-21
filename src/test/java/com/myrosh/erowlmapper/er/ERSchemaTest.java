package com.myrosh.erowlmapper.er;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * ERSchema unit tests.
 */
public class ERSchemaTest
{
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testEntityWithoutName() throws ERException {
        ERSchema schema = new ERSchema();

        schema.addEntityWithAttributes("", null);

        exception.expect(ERException.class);
        exception.expectMessage("Every EREntity must have a name.");
        schema.validate();
    }

    @Test
    public void testEntityWithDuplicateName() throws ERException {
        ERSchema schema = new ERSchema();
        EREntity entityA = schema.addEntityWithAttributes("EntityA", null);
        EREntity entityB = schema.addEntityWithAttributes("entity a", null);

        exception.expect(ERException.class);
        exception.expectMessage(entityB + " is a duplicate.");
        schema.validate();
    }

    @Test
    public void testRelationshipWithoutName() throws ERException {
        ERSchema schema = new ERSchema();

        schema.addRelationshipWithParticipatingEntitiesAndAttributes("", null, null);

        exception.expect(ERException.class);
        exception.expectMessage("Every ERRelationship must have a name.");
        schema.validate();
    }

    @Test
    public void testRelationshipWithDuplicateName() throws ERException {
        ERSchema schema = new ERSchema();
        ERRelationship relationshipA = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipA", Arrays.asList("EntityA", "EntityB"), null);
        ERRelationship relationshipB = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "relationship a", Arrays.asList("EntityC", "EntityD"), null);

        exception.expect(ERException.class);
        exception.expectMessage(relationshipB + " is a duplicate.");
        schema.validate();
    }

    @Test
    public void testRelationshipWithOneParticipatingEntity() throws ERException {
        ERSchema schema = new ERSchema();
        ERRelationship relationship = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipA", Arrays.asList("EntityA"), null);

        exception.expect(ERException.class);
        exception.expectMessage(relationship + " must have at least 2 ERParticipatingEntities.");
        schema.validate();
    }

    @Test
    public void testRelationshipWithFourParticipatingEntity() throws ERException {
        ERSchema schema = new ERSchema();
        ERRelationship relationship = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipA", Arrays.asList("EntityA", "EntityB", "EntityC", "EntityD"), null);

        exception.expect(ERException.class);
        exception.expectMessage(relationship + " cannot have more than 3 ERParticipatingEntities.");
        schema.validate();
    }

    @Test
    public void testParticipatingEntityWithoutName() throws ERException {
        ERSchema schema = new ERSchema();
        ERRelationship relationship = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipA", Arrays.asList("EntityA", "EntityB"), null);
        ERParticipatingEntity participatingEntity = relationship.getParticipatingEntities().get(0);
        participatingEntity.setName("");

        exception.expect(ERException.class);
        exception.expectMessage("Every ERParticipatingEntity must have a name.");
        schema.validate();
    }

    @Test
    public void testParticipatingEntityWithDuplicateName() throws ERException {
        ERSchema schema = new ERSchema();
        ERRelationship relationshipA = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipA", Arrays.asList("EntityA", "EntityB"), null);
        ERRelationship relationshipB = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipB", Arrays.asList("EntityC", "EntityD"), null);
        ERParticipatingEntity participatingEntity = relationshipB.getParticipatingEntities().get(0);
        participatingEntity.setName("EntityA");

        exception.expect(ERException.class);
        exception.expectMessage(participatingEntity + " in " + relationshipB + " is a duplicate.");
        schema.validate();
    }

    @Test
    public void testParticipatingEntityWithInvalidName() throws ERException {
        ERSchema schema = new ERSchema();
        ERRelationship relationshipA = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipA", Arrays.asList("EntityA", "EntityB"), null);
        ERRelationship relationshipB = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipB", Arrays.asList("EntityC", "EntityD"), null);
        ERParticipatingEntity participatingEntity = relationshipB.getParticipatingEntities().get(0);
        participatingEntity.setName("EntityE");

        exception.expect(ERException.class);
        exception.expectMessage(participatingEntity + " in " + relationshipB
            + " refers to EREntity{" + participatingEntity.getUniqueName() + "}"
            + " which does not exist.");
        schema.validate();
    }

    @Test
    public void testTernaryIdentifyingRelationship() throws ERException {
        ERSchema schema = new ERSchema();
        ERRelationship relationship = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipA", Arrays.asList("EntityA", "EntityB", "EntityC"), null);
        relationship.setIdentifying(true);

        exception.expect(ERException.class);
        exception.expectMessage("Identifying " + relationship
            + " must have exactly 2 ERParticipatingEntities.");
        schema.validate();
    }

    @Test
    public void testIdentifyingRelationshipWithoutWeakEntity() throws ERException {
        ERSchema schema = new ERSchema();
        ERRelationship relationship = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipA", Arrays.asList("EntityA", "EntityB"), null);
        relationship.setIdentifying(true);

        exception.expect(ERException.class);
        exception.expectMessage("Identifying " + relationship
            + " must have 1 strong ERParticipatingEntity"
            + " and 1 weak ERParticipatingEntity.");
        schema.validate();
    }

    @Test
    public void testIdentifyingRelationshipWithAttributes() throws ERException {
        ERSchema schema = new ERSchema();
        ERRelationship relationship = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipA", Arrays.asList("EntityA", "EntityB"), Arrays.asList("attributeA"));
        relationship.setIdentifying(true);
        schema.getEntities().get(0).setWeak(true);

        exception.expect(ERException.class);
        exception.expectMessage("Identifying " + relationship + " cannot have attributes.");
        schema.validate();
    }

    @Test
    public void testWeakEntityWithTwoIdentifyingRelationships() throws ERException {
        ERSchema schema = new ERSchema();
        ERRelationship relationshipA = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipA", Arrays.asList("EntityA", "EntityB"), null);
        relationshipA.setIdentifying(true);
        EREntity weakEntity = schema.getEntities().get(0);
        weakEntity.setWeak(true);

        ERRelationship relationshipB = schema.addRelationshipWithParticipatingEntitiesAndAttributes(
            "RelationshipB", Arrays.asList("EntityC", "EntityD"), null);
        relationshipB.setIdentifying(true);
        ERParticipatingEntity participatingEntity = relationshipB.getParticipatingEntities().get(0);
        participatingEntity.setName(weakEntity.getName());
        participatingEntity.setRole("Duplicate");

        exception.expect(ERException.class);
        exception.expectMessage("Weak " + weakEntity + " must have exactly 1 identifying"
            + " binary ERRelationship with a strong EREntity.");
        schema.validate();
    }

    @Test
    public void testAttributeWithoutName() throws ERException {
        ERSchema schema = new ERSchema();

        schema.addEntityWithAttributes("EntityA", Arrays.asList(""));

        exception.expect(ERException.class);
        exception.expectMessage("Every ERAttribute must have a name.");
        schema.validate();
    }

    @Test
    public void testAttributeWithDuplicateName() throws ERException {
        ERSchema schema = new ERSchema();
        EREntity entity = schema.addEntityWithAttributes(
            "EntityA", Arrays.asList("attributeA", "attribute a"));
        ERAttribute attribute = entity.getAttributes().get(0);

        exception.expect(ERException.class);
        exception.expectMessage(attribute + " is a duplicate.");
        schema.validate();
    }

    @Test
    public void testMultivaluedKeyAttribute() throws ERException {
        ERSchema schema = new ERSchema();
        EREntity entity = schema.addEntityWithAttributes("EntityA", Arrays.asList("attributeA"));
        ERAttribute attribute = entity.getAttributes().get(0);
        attribute.setKey(true);
        attribute.setMultivalued(true);

        exception.expect(ERException.class);
        exception.expectMessage("Key " + attribute + " cannot be multivalued.");
        schema.validate();
    }

    @Test
    public void testCompositeAttributeWithDuplicateName() throws ERException {
        ERSchema schema = new ERSchema();
        EREntity entity = schema.addEntityWithAttributes("EntityA", Arrays.asList("entity a"));
        ERAttribute attribute = entity.getAttributes().get(0);
        attribute.setComposite(true);

        exception.expect(ERException.class);
        exception.expectMessage("Composite " + attribute + " cannot have the same name as "
            + entity + ".");
        schema.validate();
    }

    @Test
    public void testComponentAttributeWithoutName() throws ERException {
        ERSchema schema = new ERSchema();
        EREntity entity = schema.addEntityWithAttributes("EntityA", Arrays.asList("attributeA"));
        ERAttribute attribute = entity.getAttributes().get(0);
        attribute.setComposite(true);
        attribute.addAttribute(new ERAttribute());

        exception.expect(ERException.class);
        exception.expectMessage("Every ERAttribute must have a name.");
        schema.validate();
    }

    @Test
    public void testComponentAttributeWithDuplicateName() throws ERException {
        ERSchema schema = new ERSchema();
        EREntity entity = schema.addEntityWithAttributes("EntityA", Arrays.asList("attributeA"));
        ERAttribute attribute = entity.getAttributes().get(0);
        attribute.setComposite(true);

        ERAttribute componentAttribute = new ERAttribute();
        componentAttribute.setName("attribute a");
        attribute.addAttribute(componentAttribute);

        exception.expect(ERException.class);
        exception.expectMessage(componentAttribute + " is a duplicate.");
        schema.validate();
    }

    @Test
    public void testKeyComponentAttribute() throws ERException {
        ERSchema schema = new ERSchema();
        EREntity entity = schema.addEntityWithAttributes("EntityA", Arrays.asList("attributeA"));
        ERAttribute attribute = entity.getAttributes().get(0);
        attribute.setComposite(true);

        ERAttribute componentAttribute = new ERAttribute();
        componentAttribute.setName("attributeB");
        componentAttribute.setKey(true);
        attribute.addAttribute(componentAttribute);

        exception.expect(ERException.class);
        exception.expectMessage("Component " + componentAttribute + " cannot be a key.");
        schema.validate();
    }

    @Test
    public void testCompositeComponentAttribute() throws ERException {
        ERSchema schema = new ERSchema();
        EREntity entity = schema.addEntityWithAttributes("EntityA", Arrays.asList("attributeA"));
        ERAttribute attribute = entity.getAttributes().get(0);
        attribute.setComposite(true);

        ERAttribute componentAttribute = new ERAttribute();
        componentAttribute.setName("attributeB");
        componentAttribute.setComposite(true);
        attribute.addAttribute(componentAttribute);

        exception.expect(ERException.class);
        exception.expectMessage("Component " + componentAttribute + " cannot be composite.");
        schema.validate();
    }
}
