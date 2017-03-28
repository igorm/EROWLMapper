package com.myrosh.erowl;

import org.apache.commons.lang3.StringUtils;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.XSD;

import com.myrosh.erowl.er.schema.Attribute;
import com.myrosh.erowl.er.schema.Entity;
import com.myrosh.erowl.er.schema.ParticipatingEntity;
import com.myrosh.erowl.er.schema.Relationship;
import com.myrosh.erowl.er.schema.Schema;

/**
 * @author igorm
 *
 * An ER to OWL Mapper implementation using Apache Jena
 *
 */
public class Mapper {

    /**
     * Namespace URI constant
     */
    public static final String NS = "http://www.semanticweb.org/ontologies/erowl#";

    /**
     * @param schema
     * @return
     * @throws Exception
     */
    public OntModel map(Schema schema) throws Exception {
        OntModel model = ModelFactory.createOntologyModel();

        mapStrongEntities(schema, model);
        mapWeakEntities(schema, model);
        mapBinaryRelationshipsWithoutAttributes(schema, model);
        mapBinaryRelationshipsWithAttributes(schema, model);
        mapTernaryRelationships(schema, model);

        return model;
    }

    /**
     * @param schema
     * @param model
     *
     * Mapping Rule 1. Map each entity into a class. Map each simple attribute into a functional
     * datatype property. Map each multi-valued attribute into a datatype property (in OWL simple
     * nonfunctional datatype and object properties are multivalued by default). Map each composite
     * attribute into a separate class with functional datatype properties corresponding to the
     * composite attribute’s components; add a functional object property with range set to the
     * newly created class. Map each simple key attribute into a functional datatype property with
     * min cardinality set to one. Map each composite key attribute into a separate class with
     * functional datatype properties corresponding to the composite primary key’s components; add
     * a functional inverse-functional object property whose range is set to the newly created
     * class with min cardinality set to one.
     */
    private void mapStrongEntities(Schema schema, OntModel model) {
        for (Entity entity : schema.getEntities()) {
            if (!entity.isWeak()) {
                OntClass entityClass = model.createClass(NS + entity.getName());

                for (Attribute attribute : entity.getAttributes()) {
                    if (attribute.isKey()) {
                        // Map the key to a functional datatypeproperty with mincardinality 1
                        DatatypeProperty keyAttributeProperty = model.createDatatypeProperty(
                            NS + attribute.getName(), true);
                        keyAttributeProperty.addDomain(entityClass);
                        keyAttributeProperty.addRange(XSD.xstring);
                        entityClass.addSuperClass(model.createMinCardinalityRestriction(
                            null, keyAttributeProperty, 1));
                    } else if (attribute.isComposite()) {
                        // Map a composite attribute to a class with corresponding attributes
                        String compositeAttributeClassName = entity.getName()
                            + StringUtils.capitalize(attribute.getName());
                        OntClass compositeAttributeClass = model.createClass(
                            NS + compositeAttributeClassName);

                        for (Attribute componentAttribute : attribute.getComponentAttributes()) {
                            DatatypeProperty componentAttributeProperty = model.createDatatypeProperty(
                                NS + componentAttribute.getName(), true);
                            componentAttributeProperty.addDomain(compositeAttributeClass);
                            componentAttributeProperty.addRange(XSD.xstring);
                        }

                        ObjectProperty hasCompositeAttributeProperty = model.createObjectProperty(
                            NS + "has" + compositeAttributeClassName, true);
                        hasCompositeAttributeProperty.addDomain(entityClass);
                        hasCompositeAttributeProperty.addRange(compositeAttributeClass);
                    } else {
                        DatatypeProperty simpleAttributeProperty = model.createDatatypeProperty(
                            NS + attribute.getName(), true);
                        simpleAttributeProperty.addDomain(entityClass);
                        simpleAttributeProperty.addRange(XSD.xstring);
                    }
                }
            }
        }
    }

    /**
     * @param schema
     * @param model
     *
     * Mapping Rule 2. Map each weak entity into a class with a functional object property whose
     * range is set to the owner class, min cardinality set to one and an object property in the
     * owner class with range set to the weak entity class. The object properties should be inverses
     * of each other. Map the weak entity’s simple, multi-valued and composite attributes according
     * to the first rule. Map partial key attributes into a separate class with corresponding
     * functional datatype properties whose min cardinality is set to one, a functional object
     * property with the range set to the owner entity class and whose min cardinality is set to
     * one, a functional inverse-functional object property with the range set to the weak entity
     * class and whose min cardinality is set to one; add a functional inverse-functional object
     * property whose range is set to the newly created class and whose min cardinality is set
     * to one.
     */
    private void mapWeakEntities(Schema schema, OntModel model) {
        for (Entity entity : schema.getEntities()) {
            if (entity.isWeak()) {
                // Create the classes: owner, weak, weakkey
                OntClass ownerEntityClass = model.getOntClass(NS + entity.getOwner());
                OntClass weakEntityClass = model.createClass(NS + entity.getName());
                OntClass weakEntityKeyClass = model.createClass(NS + entity.getName() + "Key");

                // Create the owner-to-weak objectproperty
                ObjectProperty hasWeakEntityProperty = model.createObjectProperty(
                    NS + "has" + entity.getName());
                hasWeakEntityProperty.addDomain(ownerEntityClass);
                hasWeakEntityProperty.addRange(weakEntityClass);

                Relationship relationship = schema.getRelationship(entity.getOwner(), entity.getName());
                ParticipatingEntity ownerParticipatingEntity =
                    relationship.getParticipatingEntity(entity.getOwner());

                if (ownerParticipatingEntity.getMin() == 1) {
                    ownerEntityClass.addSuperClass(
                        model.createMinCardinalityRestriction(null, hasWeakEntityProperty, 1));
                }

                if (ownerParticipatingEntity.getMax() == 1) {
                    ownerEntityClass.addSuperClass(
                        model.createMaxCardinalityRestriction(null, hasWeakEntityProperty, 1));
                }

                // Create the weak-to-owner objectproperty
                ObjectProperty isWeakEntityOfProperty = model.createObjectProperty(
                    NS + "is" + entity.getName() + "Of", true);
                isWeakEntityOfProperty.addDomain(weakEntityClass);
                isWeakEntityOfProperty.addRange(ownerEntityClass);
                isWeakEntityOfProperty.addInverseOf(hasWeakEntityProperty);
                weakEntityClass.addSuperClass(
                    model.createMinCardinalityRestriction(null, isWeakEntityOfProperty, 1));

                // Create the weakkey-to-owner functional objectproperty with mincardinality 1
                ObjectProperty hasOwnerEntityProperty = model.createObjectProperty(
                    NS + "has" + entity.getOwner(), true);
                hasOwnerEntityProperty.addDomain(weakEntityKeyClass);
                hasOwnerEntityProperty.addRange(ownerEntityClass);
                weakEntityKeyClass.addSuperClass(
                    model.createMinCardinalityRestriction(null, hasOwnerEntityProperty, 1));

                // Create the weak-to-weakkey inversefunctional functional objectproperty with mincardinality 1
                ObjectProperty hasWeakEntityKeyProperty = model.createInverseFunctionalProperty(
                    NS + "has" + entity.getName() + "Key", true);
                hasWeakEntityKeyProperty.addDomain(weakEntityClass);
                hasWeakEntityKeyProperty.addRange(weakEntityKeyClass);
                weakEntityClass.addSuperClass(
                    model.createMinCardinalityRestriction(null, hasWeakEntityKeyProperty, 1));

                // Create the weakkey-to-weak inversefunctional functional objectproperty with mincardinality 1
                ObjectProperty isWeakEntityKeyOfProperty = model.createInverseFunctionalProperty(
                    NS + "is" + entity.getName() + "KeyOf", true);
                isWeakEntityKeyOfProperty.addDomain(weakEntityKeyClass);
                isWeakEntityKeyOfProperty.addRange(weakEntityClass);
                weakEntityKeyClass.addSuperClass(model.createMinCardinalityRestriction(
                    null, isWeakEntityKeyOfProperty, 1));
                isWeakEntityKeyOfProperty.addInverseOf(hasWeakEntityKeyProperty);

                for (Attribute attribute : entity.getAttributes()) {
                    if (attribute.isKey()) {
                        DatatypeProperty keyAttributeProperty = model.createDatatypeProperty(
                            NS + attribute.getName(), true);
                        keyAttributeProperty.addDomain(weakEntityKeyClass);
                        keyAttributeProperty.addRange(XSD.xstring);
                        weakEntityKeyClass.addSuperClass(
                            model.createMinCardinalityRestriction(null, keyAttributeProperty, 1));
                    } else {
                        DatatypeProperty simpleAttributeProperty = model.createDatatypeProperty(
                            NS + attribute.getName(), true);
                        simpleAttributeProperty.addDomain(weakEntityClass);
                        simpleAttributeProperty.addRange(XSD.xstring);
                    }
                }
            }
        }
    }

    /**
     * @param schema
     * @param model
     *
     * Mapping Rule 3. Map each binary relationship without attributes into a pair of object
     * properties in the two classes corresponding to the two participating entities. Map
     * participating entities’ cardinality into min and max cardinality restrictions or combine
     * characteristics of a functional property with a min cardinality restriction.
     *
     * TODO Update this method not to map identifying relationships which have already been
     * processed during weak entities' mapping.
     */
    private void mapBinaryRelationshipsWithoutAttributes(Schema schema, OntModel model) {
        for (Relationship relationship : schema.getRelationships()) {
            if (relationship.getParticipatingEntities().size() == 2
                && relationship.getAttributes().isEmpty()
            ) {
                ParticipatingEntity firstParticipatingEntity = relationship.getParticipatingEntities().get(0);
                ParticipatingEntity secondParticipatingEntity = relationship.getParticipatingEntities().get(1);

                OntClass firstEntityClass = model.getOntClass(NS + firstParticipatingEntity.getName());
                OntClass secondEntityClass = model.getOntClass(NS + secondParticipatingEntity.getName());

                // Create the first-to-second objectproperty
                ObjectProperty hasSecondEntityProperty = model.createObjectProperty(
                    NS + "has" + secondParticipatingEntity.getName());
                hasSecondEntityProperty.addDomain(firstEntityClass);
                hasSecondEntityProperty.addRange(secondEntityClass);

                if (firstParticipatingEntity.getMin() == 1) {
                    firstEntityClass.addSuperClass(
                        model.createMinCardinalityRestriction(null, hasSecondEntityProperty, 1));
                }

                if (firstParticipatingEntity.getMax() == 1) {
                    firstEntityClass.addSuperClass(
                        model.createMaxCardinalityRestriction(null, hasSecondEntityProperty, 1));
                }

                // Create the second-to-first objectproperty
                ObjectProperty isSecondEntityOfProperty = model.createObjectProperty(
                    NS + "is" + secondParticipatingEntity.getName() + "Of");
                isSecondEntityOfProperty.addDomain(secondEntityClass);
                isSecondEntityOfProperty.addRange(firstEntityClass);

                if (secondParticipatingEntity.getMin() == 1) {
                    secondEntityClass.addSuperClass(
                        model.createMinCardinalityRestriction(null, isSecondEntityOfProperty, 1));
                }

                if (secondParticipatingEntity.getMax() == 1) {
                    secondEntityClass.addSuperClass(
                        model.createMaxCardinalityRestriction(null, isSecondEntityOfProperty, 1));
                }

                isSecondEntityOfProperty.addInverseOf(hasSecondEntityProperty);
            }
        }
    }

    /**
     * @param schema
     * @param model
     *
     * Mapping Rule 4. Map each binary relationship with attributes into a class with datatype
     * properties corresponding to the relationship attributes and two pairs of inverse object
     * properties between participating entity classes and the relationship class. Map participating
     * entities’ cardinality into min and max cardinality restrictions or combine characteristics
     * of a functional property with a min cardinality restriction for object properties pointing
     * from participating entity classes to the relationship class. For their inverse object
     * properties, min and max cardinality should be set to one.
     */
    private void mapBinaryRelationshipsWithAttributes(Schema schema, OntModel model) {
        for (Relationship relationship : schema.getRelationships()) {
            if (relationship.getParticipatingEntities().size() == 2
                && !relationship.getAttributes().isEmpty()
            ) {
                ParticipatingEntity firstParticipatingEntity = relationship.getParticipatingEntities().get(0);
                ParticipatingEntity secondParticipatingEntity = relationship.getParticipatingEntities().get(1);

                String firstEntityClassName = firstParticipatingEntity.getName();
                String secondEntityClassName = secondParticipatingEntity.getName();
                String relationshipClassName = firstParticipatingEntity.getName()
                    + secondParticipatingEntity.getName();

                OntClass firstEntityClass = model.getOntClass(NS + firstEntityClassName);
                OntClass secondEntityClass = model.getOntClass(NS + secondEntityClassName);
                OntClass relationshipClass = model.createClass(NS + relationshipClassName);

                for (Attribute attribute : relationship.getAttributes()) {
                    DatatypeProperty attributeProperty = model.createDatatypeProperty(
                        NS + attribute.getName(), true);
                    attributeProperty.addDomain(relationshipClass);
                    attributeProperty.addRange(XSD.xstring);
                }

                // Create the first-to-relationship objectproperty
                ObjectProperty firstEntityHasRelationshipsProperty = model.createObjectProperty(
                    NS + StringUtils.uncapitalize(firstEntityClassName) + "Has" + relationshipClassName);
                firstEntityHasRelationshipsProperty.addDomain(firstEntityClass);
                firstEntityHasRelationshipsProperty.addRange(relationshipClass);

                if (firstParticipatingEntity.getMin() == 1) {
                    firstEntityClass.addSuperClass(model.createMinCardinalityRestriction(
                        null, firstEntityHasRelationshipsProperty, 1));
                }

                if (firstParticipatingEntity.getMax() == 1) {
                    firstEntityClass.addSuperClass(model.createMaxCardinalityRestriction(
                        null, firstEntityHasRelationshipsProperty, 1));
                }

                // Create the relationship-to-first functional objectproperty with min and max cardinality 1
                ObjectProperty isRelationshipOfFirstEntityProperty = model.createObjectProperty(
                    NS + "is" + relationshipClassName + "Of" + firstEntityClassName, true);
                isRelationshipOfFirstEntityProperty.addDomain(relationshipClass);
                isRelationshipOfFirstEntityProperty.addRange(firstEntityClass);
                relationshipClass.addSuperClass(model.createMinCardinalityRestriction(
                    null, isRelationshipOfFirstEntityProperty, 1));
                isRelationshipOfFirstEntityProperty.addInverseOf(firstEntityHasRelationshipsProperty);

                // Create the second-to-relationship objectproperty
                ObjectProperty secondEntityHasRelationshipsProperty = model.createObjectProperty(
                    NS + StringUtils.uncapitalize(secondEntityClassName) + "Has" + relationshipClassName);
                secondEntityHasRelationshipsProperty.addDomain(secondEntityClass);
                secondEntityHasRelationshipsProperty.addRange(relationshipClass);

                if (secondParticipatingEntity.getMin() == 1) {
                    secondEntityClass.addSuperClass(model.createMinCardinalityRestriction(
                        null, secondEntityHasRelationshipsProperty, 1));
                }

                if (secondParticipatingEntity.getMax() == 1) {
                    secondEntityClass.addSuperClass(model.createMaxCardinalityRestriction(
                        null, secondEntityHasRelationshipsProperty, 1));
                }

                // Create the relationship-to-second functional objectproperty with min and max cardinality 1
                ObjectProperty isRelationshipOfSecondEntityProperty = model.createObjectProperty(
                    NS + "is" + relationshipClassName + "Of" + secondEntityClassName, true);
                isRelationshipOfSecondEntityProperty.addDomain(relationshipClass);
                isRelationshipOfSecondEntityProperty.addRange(secondEntityClass);
                relationshipClass.addSuperClass(model.createMinCardinalityRestriction(
                    null, isRelationshipOfSecondEntityProperty, 1));
                relationshipClass.addSuperClass(model.createMaxCardinalityRestriction(
                    null, isRelationshipOfSecondEntityProperty, 1));
                isRelationshipOfSecondEntityProperty.addInverseOf(secondEntityHasRelationshipsProperty);
            }
        }
    }

    /**
     * @param schema
     * @param model
     *
     * Mapping Rule 5. Map each ternary relationship into a class with three pairs of inverse
     * object properties between participating entity classes and the relationship class. Map
     * participating entities’ cardinality into min and max cardinality restrictions or combine
     * characteristics of a functional property with a min cardinality restriction for object
     * properties pointing from participating entity classes to the relationship class. For their
     * inverse object properties, min and max cardinality should be set to one.
     */
    private void mapTernaryRelationships(Schema schema, OntModel model) {
        for (Relationship relationship : schema.getRelationships()) {
            if (relationship.getParticipatingEntities().size() == 3) {
                ParticipatingEntity firstParticipatingEntity = relationship.getParticipatingEntities().get(0);
                ParticipatingEntity secondParticipatingEntity = relationship.getParticipatingEntities().get(1);
                ParticipatingEntity thirdParticipatingEntity = relationship.getParticipatingEntities().get(2);

                String firstEntityClassName = firstParticipatingEntity.getName();
                String secondEntityClassName = secondParticipatingEntity.getName();
                String thirdEntityClassName = thirdParticipatingEntity.getName();
                String relationshipClassName = firstParticipatingEntity.getName()
                    + secondParticipatingEntity.getName();

                OntClass firstEntityClass = model.getOntClass(NS + firstEntityClassName);
                OntClass secondEntityClass = model.getOntClass(NS + secondEntityClassName);
                OntClass thirdEntityClass = model.getOntClass(NS + thirdEntityClassName);
                OntClass relationshipClass = model.createClass(NS + relationshipClassName);

                // Create the first-to-relationship objectproperty
                ObjectProperty firstEntityHasRelationshipsProperty = model.createObjectProperty(
                    NS + StringUtils.uncapitalize(firstEntityClassName) + "Has" + relationshipClassName);
                firstEntityHasRelationshipsProperty.addDomain(firstEntityClass);
                firstEntityHasRelationshipsProperty.addRange(relationshipClass);

                if (firstParticipatingEntity.getMin() == 1) {
                    firstEntityClass.addSuperClass(model.createMinCardinalityRestriction(
                        null, firstEntityHasRelationshipsProperty, 1));
                }

                if (firstParticipatingEntity.getMax() == 1) {
                    firstEntityClass.addSuperClass(model.createMaxCardinalityRestriction(
                        null, firstEntityHasRelationshipsProperty, 1));
                }

                // Create the relationship-to-first functional objectproperty with min and max cardinality 1
                ObjectProperty isRelationshipOfFirstEntityProperty = model.createObjectProperty(
                    NS + "is" + relationshipClassName + "Of" + firstEntityClassName, true);
                isRelationshipOfFirstEntityProperty.addDomain(relationshipClass);
                isRelationshipOfFirstEntityProperty.addRange(firstEntityClass);
                relationshipClass.addSuperClass(model.createMinCardinalityRestriction(
                    null, isRelationshipOfFirstEntityProperty, 1));
                isRelationshipOfFirstEntityProperty.addInverseOf(firstEntityHasRelationshipsProperty);

                // Create the second-to-relationship objectproperty
                ObjectProperty secondEntityHasRelationshipsProperty = model.createObjectProperty(
                    NS + StringUtils.uncapitalize(secondEntityClassName) + "Has" + relationshipClassName);
                secondEntityHasRelationshipsProperty.addDomain(secondEntityClass);
                secondEntityHasRelationshipsProperty.addRange(relationshipClass);

                if (secondParticipatingEntity.getMin() == 1) {
                    secondEntityClass.addSuperClass(model.createMinCardinalityRestriction(
                        null, secondEntityHasRelationshipsProperty, 1));
                }

                if (secondParticipatingEntity.getMax() == 1) {
                    secondEntityClass.addSuperClass(model.createMaxCardinalityRestriction(
                        null, secondEntityHasRelationshipsProperty, 1));
                }

                // Create the relationship-to-second functional objectproperty with min and max cardinality 1
                ObjectProperty isRelationshipOfSecondEntityProperty = model.createObjectProperty(
                    NS + "is" + relationshipClassName + "Of" + secondEntityClassName, true);
                isRelationshipOfSecondEntityProperty.addDomain(relationshipClass);
                isRelationshipOfSecondEntityProperty.addRange(secondEntityClass);
                relationshipClass.addSuperClass(model.createMinCardinalityRestriction(
                    null, isRelationshipOfSecondEntityProperty, 1));
                isRelationshipOfSecondEntityProperty.addInverseOf(secondEntityHasRelationshipsProperty);

                // Create the third-to-relationship objectproperty
                ObjectProperty thirdEntityHasRelationshipsProperty = model.createObjectProperty(
                    NS + StringUtils.uncapitalize(thirdEntityClassName) + "Has" + relationshipClassName);
                thirdEntityHasRelationshipsProperty.addDomain(thirdEntityClass);
                thirdEntityHasRelationshipsProperty.addRange(relationshipClass);

                if (secondParticipatingEntity.getMin() == 1) {
                    thirdEntityClass.addSuperClass(model.createMinCardinalityRestriction(
                        null, thirdEntityHasRelationshipsProperty, 1));
                }

                if (secondParticipatingEntity.getMax() == 1) {
                    thirdEntityClass.addSuperClass(model.createMaxCardinalityRestriction(
                        null, thirdEntityHasRelationshipsProperty, 1));
                }

                // Create the relationship-to-third functional objectproperty with min and max cardinality 1
                ObjectProperty isRelationshipOfThirdEntityProperty = model.createObjectProperty(
                    NS + "is" + relationshipClassName + "Of" + thirdEntityClassName, true);
                isRelationshipOfThirdEntityProperty.addDomain(relationshipClass);
                isRelationshipOfThirdEntityProperty.addRange(thirdEntityClass);
                relationshipClass.addSuperClass(model.createMinCardinalityRestriction(
                    null, isRelationshipOfThirdEntityProperty, 1));
                isRelationshipOfThirdEntityProperty.addInverseOf(thirdEntityHasRelationshipsProperty);
            }
        }
    }
}
