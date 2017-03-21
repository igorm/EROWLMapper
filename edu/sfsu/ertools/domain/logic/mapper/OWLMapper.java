package edu.sfsu.ertools.domain.logic.mapper;

import java.io.Writer;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.sfsu.ertools.domain.Attribute;
import edu.sfsu.ertools.domain.Entity;
import edu.sfsu.ertools.domain.ParticipatingEntity;
import edu.sfsu.ertools.domain.Relationship;
import edu.sfsu.ertools.domain.Schema;

/**
 * @author igorm
 *
 * A concrete ER-to-OWL Mapper implementation
 *
 */
public class OWLMapper extends AbstractMapper {

	/**
	 * Ontology language URL constant
	 */
	public static final String ONTOLOGY_LANGUAGE = "http://www.w3.org/TR/owl-features/#term_OWLLite";

	/**
	 * Ontology format constant
	 */
	public static final String ONTOLOGY_FORMAT = "RDF/XML-ABBREV";

	/**
	 * Namespace URL constant
	 */
	public static final String NS = "http://cs.sfsu.edu/owl/ontologies/ertools/#";

	/* (non-Javadoc)
	 * @see edu.sfsu.ertools.domain.logic.Mapper#map(edu.sfsu.ertools.domain.Schema, java.io.Writer)
	 */
	public void map(Schema schema, Writer writer) throws Exception {
		OntModel model = ModelFactory.createOntologyModel(ONTOLOGY_LANGUAGE);

		mapStrongEntities(schema, model);
		mapWeakEntities(schema, model);
		mapBinaryRelationshipsWithoutAttributes(schema, model);
		mapBinaryRelationshipsWithAttributes(schema, model);
		mapTernaryRelationships(schema, model);

		model.write(writer, ONTOLOGY_FORMAT);
	}

	/**
	 * @param schema
	 * @param model
	 *
	 * Mapping Rule 1. Map each entity into a
	 * class. Map each simple attribute into a functional
	 * datatype property. Map each multi-valued attribute
	 * into a datatype property (in OWL simple nonfunctional
	 * datatype and object properties are multivalued
	 * by default). Map each composite attribute into
	 * a separate class with functional datatype properties
	 * corresponding to the composite attribute’s
	 * components; add a functional object property with
	 * range set to the newly created class. Map each simple
	 * key attribute into a functional datatype property with
	 * min cardinality set to one. Map each composite key
	 * attribute into a separate class with functional datatype
	 * properties corresponding to the composite primary
	 * key’s components; add a functional inverse-functional
	 * object property whose range is set to the newly created
	 * class with min cardinality set to one.
	 */
	private void mapStrongEntities(Schema schema, OntModel model) {
		for (Entity entity : schema.getEntities()) {
			if (!entity.isWeak()) {
				OntClass entityClass = model.createClass(NS + entity.getName());

				for (Attribute attribute : entity.getAttributes()) {
					if (attribute.isKey()) {
						// Map the key to a functional datatypeproperty with mincardinality 1
						DatatypeProperty keyAttributeProperty = model.createDatatypeProperty(NS + attribute.getName(), true);
						keyAttributeProperty.addDomain(entityClass);
						entityClass.addSuperClass(model.createMinCardinalityRestriction(null, keyAttributeProperty, 1));
					} else if (attribute.isComposite()) {
						// Map a composite attribute to a class with corresponding attributes
						String compositeAttributeClassName = entity.getName() + upperCaseFirstChar(attribute.getName());
						OntClass compositeAttributeClass = model.createClass(NS + compositeAttributeClassName);
						for (Attribute componentAttribute : attribute.getComponentAttributes()) {
							DatatypeProperty componentAttributeProperty = model.createDatatypeProperty(NS + componentAttribute.getName(), true);
							componentAttributeProperty.addDomain(compositeAttributeClass);
						}
						ObjectProperty hasCompositeAttributeProperty = model.createObjectProperty(NS + "has" + compositeAttributeClassName, true);
						hasCompositeAttributeProperty.addDomain(entityClass);
						hasCompositeAttributeProperty.addRange(compositeAttributeClass);
					} else {
						DatatypeProperty simpleAttributeProperty = model.createDatatypeProperty(NS + attribute.getName(), true);
						simpleAttributeProperty.addDomain(entityClass);
					}
				}
			}
		}
	}

	/**
	 * @param schema
	 * @param model
	 *
	 * Mapping Rule 2. Map each weak entity
	 * into a class with a functional object property whose
	 * range is set to the owner class, min cardinality set to
	 * one and an object property in the owner class with
	 * range set to the weak entity class. The object
	 * properties should be inverses of each other. Map the
	 * weak entity’s simple, multi-valued and composite
	 * attributes according to the first rule. Map partial key
	 * attributes into a separate class with corresponding
	 * functional datatype properties whose min cardinality is
	 * set to one, a functional object property with the range
	 * set to the owner entity class and whose min cardinality
	 * is set to one, a functional inverse-functional object
	 * property with the range set to the weak entity class and
	 * whose min cardinality is set to one; add a functional
	 * inverse-functional object property whose range is set to
	 * the newly created class and whose min cardinality is
	 * set to one.
	 */
	private void mapWeakEntities(Schema schema, OntModel model) {
		for (Entity entity : schema.getEntities()) {
			if (entity.isWeak()) {
				// Create the classes: owner, weak, weakkey
				OntClass ownerEntityClass = model.getOntClass(NS + entity.getOwner());
				OntClass weakEntityClass = model.createClass(NS + entity.getName());
				OntClass weakEntityKeyClass = model.createClass(NS + entity.getName() + "Key");

				// Create the owner-to-weak objectproperty
				ObjectProperty hasWeakEntitiesProperty = model.createObjectProperty(NS + "has" + entity.getName() + "s");
				hasWeakEntitiesProperty.addDomain(ownerEntityClass);
				hasWeakEntitiesProperty.addRange(weakEntityClass);

				Relationship relationship = schema.getRelationship(entity.getOwner(), entity.getName());
				ParticipatingEntity ownerParticipatingEntity = relationship.getParticipatingEntity(entity.getOwner());
				if (ownerParticipatingEntity.getMin() == 1)
					ownerEntityClass.addSuperClass(model.createMinCardinalityRestriction(null, hasWeakEntitiesProperty, 1));
				if (ownerParticipatingEntity.getMax() == 1)
					ownerEntityClass.addSuperClass(model.createMaxCardinalityRestriction(null, hasWeakEntitiesProperty, 1));

				// Create the weak-to-owner objectproperty
				ObjectProperty isWeakEntityOfProperty = model.createObjectProperty(NS + "is" + entity.getName() + "Of", true);
				isWeakEntityOfProperty.addDomain(weakEntityClass);
				isWeakEntityOfProperty.addRange(ownerEntityClass);
				isWeakEntityOfProperty.addInverseOf(hasWeakEntitiesProperty);
				weakEntityClass.addSuperClass(model.createMinCardinalityRestriction(null, isWeakEntityOfProperty, 1));

				// Create the weakkey-to-owner functional objectproperty with mincardinality 1
				ObjectProperty hasOwnerEntityProperty = model.createObjectProperty(NS + "has" + entity.getOwner(), true);
				hasOwnerEntityProperty.addDomain(weakEntityKeyClass);
				hasOwnerEntityProperty.addRange(ownerEntityClass);
				weakEntityKeyClass.addSuperClass(model.createMinCardinalityRestriction(null, hasOwnerEntityProperty, 1));

				// Create the weak-to-weakkey inversefunctional functional objectproperty with mincardinality 1
				ObjectProperty hasWeakEntityKeyProperty = model.createInverseFunctionalProperty(NS + "has" + entity.getName() + "Key", true);
				hasWeakEntityKeyProperty.addDomain(weakEntityClass);
				hasWeakEntityKeyProperty.addRange(weakEntityKeyClass);
				weakEntityClass.addSuperClass(model.createMinCardinalityRestriction(null, hasWeakEntityKeyProperty, 1));

				// Create the weakkey-to-weak inversefunctional functional objectproperty with mincardinality 1
				ObjectProperty isWeakEntityKeyOfProperty = model.createInverseFunctionalProperty(NS + "is" + entity.getName() + "KeyOf", true);
				isWeakEntityKeyOfProperty.addDomain(weakEntityKeyClass);
				isWeakEntityKeyOfProperty.addRange(weakEntityClass);
				weakEntityKeyClass.addSuperClass(model.createMinCardinalityRestriction(null, isWeakEntityKeyOfProperty, 1));
				isWeakEntityKeyOfProperty.addInverseOf(hasWeakEntityKeyProperty);

				for (Attribute attribute : entity.getAttributes()) {
					if (attribute.isKey()) {
						DatatypeProperty keyAttributeProperty = model.createDatatypeProperty(NS + attribute.getName(), true);
						keyAttributeProperty.addDomain(weakEntityKeyClass);
						weakEntityKeyClass.addSuperClass(model.createMinCardinalityRestriction(null, keyAttributeProperty, 1));
					} else {
						DatatypeProperty simpleAttributeProperty = model.createDatatypeProperty(NS + attribute.getName(), true);
						simpleAttributeProperty.addDomain(weakEntityClass);
					}
				}
			}
		}
	}

	/**
	 * @param schema
	 * @param model
	 *
	 * Mapping Rule 3. Map each binary
	 * relationship without attributes into a pair of object
	 * properties in the two classes corresponding to the two
	 * participating entities. Map participating entities’
	 * cardinality into min and max cardinality restrictions or
	 * combine characteristics of a functional property with a
	 * min cardinality restriction.
	 *
	 * TODO Update this method not to map identifying relationships
	 * which have already been processed during weak entities' mapping.
	 */
	private void mapBinaryRelationshipsWithoutAttributes(Schema schema, OntModel model) {
		for (Relationship relationship : schema.getRelationships()) {
			if (relationship.getParticipatingEntities().size() == 2 && relationship.getAttributes().isEmpty()) {
				ParticipatingEntity firstParticipatingEntity = relationship.getParticipatingEntities().get(0);
				ParticipatingEntity secondParticipatingEntity = relationship.getParticipatingEntities().get(1);

				OntClass firstEntityClass = model.getOntClass(NS + firstParticipatingEntity.getName());
				OntClass secondEntityClass = model.getOntClass(NS + secondParticipatingEntity.getName());

				// Create the first-to-second objectproperty
				ObjectProperty hasSecondEntitiesProperty = model.createObjectProperty(NS + "has" + secondParticipatingEntity.getName() + "s");
				hasSecondEntitiesProperty.addDomain(firstEntityClass);
				hasSecondEntitiesProperty.addRange(secondEntityClass);
				if (firstParticipatingEntity.getMin() == 1)
					firstEntityClass.addSuperClass(model.createMinCardinalityRestriction(null, hasSecondEntitiesProperty, 1));
				if (firstParticipatingEntity.getMax() == 1)
					firstEntityClass.addSuperClass(model.createMaxCardinalityRestriction(null, hasSecondEntitiesProperty, 1));

				// Create the second-to-first objectproperty
				ObjectProperty isSecondEntityOfProperty = model.createObjectProperty(NS + "is" + secondParticipatingEntity.getName() + "Of");
				isSecondEntityOfProperty.addDomain(secondEntityClass);
				isSecondEntityOfProperty.addRange(firstEntityClass);
				if (secondParticipatingEntity.getMin() == 1)
					secondEntityClass.addSuperClass(model.createMinCardinalityRestriction(null, isSecondEntityOfProperty, 1));
				if (secondParticipatingEntity.getMax() == 1)
					secondEntityClass.addSuperClass(model.createMaxCardinalityRestriction(null, isSecondEntityOfProperty, 1));
				isSecondEntityOfProperty.addInverseOf(hasSecondEntitiesProperty);
			}
		}
	}

	/**
	 * @param schema
	 * @param model
	 *
	 * Mapping Rule 4. Map each binary
	 * relationship with attributes into a class with datatype
	 * properties corresponding to the relationship attributes
	 * and two pairs of inverse object properties between
	 * participating entity classes and the relationship class.
	 * Map participating entities’ cardinality into min and
	 * max cardinality restrictions or combine characteristics
	 * of a functional property with a min cardinality
	 * restriction for object properties pointing from
	 * participating entity classes to the relationship class.
	 * For their inverse object properties, min and max
	 * cardinality should be set to one.
	 */
	private void mapBinaryRelationshipsWithAttributes(Schema schema, OntModel model) {
		for (Relationship relationship : schema.getRelationships()) {
			if (relationship.getParticipatingEntities().size() == 2 && !relationship.getAttributes().isEmpty()) {
				ParticipatingEntity firstParticipatingEntity = relationship.getParticipatingEntities().get(0);
				ParticipatingEntity secondParticipatingEntity = relationship.getParticipatingEntities().get(1);

				String firstEntityClassName = firstParticipatingEntity.getName();
				String secondEntityClassName = secondParticipatingEntity.getName();
				String relationshipClassName = firstParticipatingEntity.getName()+secondParticipatingEntity.getName();

				OntClass firstEntityClass = model.getOntClass(NS + firstEntityClassName);
				OntClass secondEntityClass = model.getOntClass(NS + secondEntityClassName);
				OntClass relationshipClass = model.createClass(NS + relationshipClassName);

				for (Attribute attribute : relationship.getAttributes()) {
					DatatypeProperty attributeProperty = model.createDatatypeProperty(NS + attribute.getName(), true);
					attributeProperty.addDomain(relationshipClass);
				}

				// Create the first-to-relationship objectproperty
				ObjectProperty firstEntityHasRelationshipsProperty = model.createObjectProperty(NS + lowerCaseFirstChar(firstEntityClassName) + "Has" + relationshipClassName + "s");
				firstEntityHasRelationshipsProperty.addDomain(firstEntityClass);
				firstEntityHasRelationshipsProperty.addRange(relationshipClass);
				if (firstParticipatingEntity.getMin() == 1)
					firstEntityClass.addSuperClass(model.createMinCardinalityRestriction(null, firstEntityHasRelationshipsProperty, 1));
				if (firstParticipatingEntity.getMax() == 1)
					firstEntityClass.addSuperClass(model.createMaxCardinalityRestriction(null, firstEntityHasRelationshipsProperty, 1));

				// Create the relationship-to-first functional objectproperty with min and max cardinality 1
				ObjectProperty isRelationshipOfFirstEntityProperty = model.createObjectProperty(NS + "is" + relationshipClassName + "Of" + firstEntityClassName, true);
				isRelationshipOfFirstEntityProperty.addDomain(relationshipClass);
				isRelationshipOfFirstEntityProperty.addRange(firstEntityClass);
				relationshipClass.addSuperClass(model.createMinCardinalityRestriction(null, isRelationshipOfFirstEntityProperty, 1));
				isRelationshipOfFirstEntityProperty.addInverseOf(firstEntityHasRelationshipsProperty);

				// Create the second-to-relationship objectproperty
				ObjectProperty secondEntityHasRelationshipsProperty = model.createObjectProperty(NS + lowerCaseFirstChar(secondEntityClassName) + "Has" + relationshipClassName + "s");
				secondEntityHasRelationshipsProperty.addDomain(secondEntityClass);
				secondEntityHasRelationshipsProperty.addRange(relationshipClass);
				if (secondParticipatingEntity.getMin() == 1)
					secondEntityClass.addSuperClass(model.createMinCardinalityRestriction(null, secondEntityHasRelationshipsProperty, 1));
				if (secondParticipatingEntity.getMax() == 1)
					secondEntityClass.addSuperClass(model.createMaxCardinalityRestriction(null, secondEntityHasRelationshipsProperty, 1));

				// Create the relationship-to-second functional objectproperty with min and max cardinality 1
				ObjectProperty isRelationshipOfSecondEntityProperty = model.createObjectProperty(NS + "is" + relationshipClassName + "Of" + secondEntityClassName, true);
				isRelationshipOfSecondEntityProperty.addDomain(relationshipClass);
				isRelationshipOfSecondEntityProperty.addRange(secondEntityClass);
				relationshipClass.addSuperClass(model.createMinCardinalityRestriction(null, isRelationshipOfSecondEntityProperty, 1));
				relationshipClass.addSuperClass(model.createMaxCardinalityRestriction(null, isRelationshipOfSecondEntityProperty, 1));
				isRelationshipOfSecondEntityProperty.addInverseOf(secondEntityHasRelationshipsProperty);
			}
		}
	}

	/**
	 * @param schema
	 * @param model
	 *
	 * Mapping Rule 5. Map each ternary
	 * relationship into a class with three pairs of inverse
	 * object properties between participating entity classes
	 * and the relationship class. Map participating entities’
	 * cardinality into min and max cardinality restrictions or
	 * combine characteristics of a functional property with a
	 * min cardinality restriction for object properties
	 * pointing from participating entity classes to the
	 * relationship class. For their inverse object properties,
	 * min and max cardinality should be set to one.
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
				String relationshipClassName = firstParticipatingEntity.getName()+secondParticipatingEntity.getName();

				OntClass firstEntityClass = model.getOntClass(NS + firstEntityClassName);
				OntClass secondEntityClass = model.getOntClass(NS + secondEntityClassName);
				OntClass thirdEntityClass = model.getOntClass(NS + thirdEntityClassName);
				OntClass relationshipClass = model.createClass(NS + relationshipClassName);

				// Create the first-to-relationship objectproperty
				ObjectProperty firstEntityHasRelationshipsProperty = model.createObjectProperty(NS + lowerCaseFirstChar(firstEntityClassName) + "Has" + relationshipClassName + "s");
				firstEntityHasRelationshipsProperty.addDomain(firstEntityClass);
				firstEntityHasRelationshipsProperty.addRange(relationshipClass);
				if (firstParticipatingEntity.getMin() == 1)
					firstEntityClass.addSuperClass(model.createMinCardinalityRestriction(null, firstEntityHasRelationshipsProperty, 1));
				if (firstParticipatingEntity.getMax() == 1)
					firstEntityClass.addSuperClass(model.createMaxCardinalityRestriction(null, firstEntityHasRelationshipsProperty, 1));

				// Create the relationship-to-first functional objectproperty with min and max cardinality 1
				ObjectProperty isRelationshipOfFirstEntityProperty = model.createObjectProperty(NS + "is" + relationshipClassName + "Of" + firstEntityClassName, true);
				isRelationshipOfFirstEntityProperty.addDomain(relationshipClass);
				isRelationshipOfFirstEntityProperty.addRange(firstEntityClass);
				relationshipClass.addSuperClass(model.createMinCardinalityRestriction(null, isRelationshipOfFirstEntityProperty, 1));
				isRelationshipOfFirstEntityProperty.addInverseOf(firstEntityHasRelationshipsProperty);

				// Create the second-to-relationship objectproperty
				ObjectProperty secondEntityHasRelationshipsProperty = model.createObjectProperty(NS + lowerCaseFirstChar(secondEntityClassName) + "Has" + relationshipClassName + "s");
				secondEntityHasRelationshipsProperty.addDomain(secondEntityClass);
				secondEntityHasRelationshipsProperty.addRange(relationshipClass);
				if (secondParticipatingEntity.getMin() == 1)
					secondEntityClass.addSuperClass(model.createMinCardinalityRestriction(null, secondEntityHasRelationshipsProperty, 1));
				if (secondParticipatingEntity.getMax() == 1)
					secondEntityClass.addSuperClass(model.createMaxCardinalityRestriction(null, secondEntityHasRelationshipsProperty, 1));

				// Create the relationship-to-second functional objectproperty with min and max cardinality 1
				ObjectProperty isRelationshipOfSecondEntityProperty = model.createObjectProperty(NS + "is" + relationshipClassName + "Of" + secondEntityClassName, true);
				isRelationshipOfSecondEntityProperty.addDomain(relationshipClass);
				isRelationshipOfSecondEntityProperty.addRange(secondEntityClass);
				relationshipClass.addSuperClass(model.createMinCardinalityRestriction(null, isRelationshipOfSecondEntityProperty, 1));
				isRelationshipOfSecondEntityProperty.addInverseOf(secondEntityHasRelationshipsProperty);

				// Create the third-to-relationship objectproperty
				ObjectProperty thirdEntityHasRelationshipsProperty = model.createObjectProperty(NS + lowerCaseFirstChar(thirdEntityClassName) + "Has" + relationshipClassName + "s");
				thirdEntityHasRelationshipsProperty.addDomain(thirdEntityClass);
				thirdEntityHasRelationshipsProperty.addRange(relationshipClass);
				if (secondParticipatingEntity.getMin() == 1)
					thirdEntityClass.addSuperClass(model.createMinCardinalityRestriction(null, thirdEntityHasRelationshipsProperty, 1));
				if (secondParticipatingEntity.getMax() == 1)
					thirdEntityClass.addSuperClass(model.createMaxCardinalityRestriction(null, thirdEntityHasRelationshipsProperty, 1));

				// Create the relationship-to-third functional objectproperty with min and max cardinality 1
				ObjectProperty isRelationshipOfThirdEntityProperty = model.createObjectProperty(NS + "is" + relationshipClassName + "Of" + thirdEntityClassName, true);
				isRelationshipOfThirdEntityProperty.addDomain(relationshipClass);
				isRelationshipOfThirdEntityProperty.addRange(thirdEntityClass);
				relationshipClass.addSuperClass(model.createMinCardinalityRestriction(null, isRelationshipOfThirdEntityProperty, 1));
				isRelationshipOfThirdEntityProperty.addInverseOf(thirdEntityHasRelationshipsProperty);
			}
		}
	}
}
