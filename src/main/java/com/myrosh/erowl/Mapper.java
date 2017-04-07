package com.myrosh.erowl;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.XSD;

import com.myrosh.erowl.er.schema.Attribute;
import com.myrosh.erowl.er.schema.Entity;
import com.myrosh.erowl.er.schema.ParticipatingEntity;
import com.myrosh.erowl.er.schema.Relationship;
import com.myrosh.erowl.er.schema.Schema;
import com.myrosh.erowl.MappingException;
import com.myrosh.erowl.Utils;

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
    public static final String NS = "http://www.semanticweb.org/ontologies/erowlmapper#";

    /**
     * ER schema
     */
    private Schema schema;

    /**
     * OWL Ontology
     */
    private OntModel model;

    /**
     * @param schema
     * @return
     * @throws MappingException
     */
    public OntModel map(Schema schema) throws MappingException {
        this.schema = schema;
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);

        mapStrongEntities();
        mapWeakEntitiesAndIdentifyingRelationships();
        mapBinaryRelationshipsWithoutAttributes();
        mapBinaryRelationshipsWithAttributes();



        mapTernaryRelationships();

        return model;
    }

    private OntClass mapEntity(Entity entity) throws MappingException {
        // Map the entity
        OntClass entityClass = addClass(entity.getName());
        List<Attribute> keyAttributes = entity.getKeyAttributes();

        if (keyAttributes.size() == 1) {
            Attribute keyAttribute = keyAttributes.get(0);

            if (keyAttribute.isComposite()) {
                // Map the single composite key attribute
                OntClass keyClass = addKeyClass(entityClass);

                for (Attribute attribute : keyAttribute.getComponentAttributes()) {
                    addDatatypeProperty(attribute.getName(), keyClass, true, true);
                }
            } else {
                // Map the single simple key attribute
                addDatatypeProperty(keyAttribute.getName(), entityClass, true, true);
            }
        } else if (keyAttributes.size() > 1) {
            // Map multiple simple key attributes
            OntClass keyClass = addKeyClass(entityClass);

            for (Attribute attribute : keyAttributes) {
                addDatatypeProperty(attribute.getName(), keyClass, true, true);
            }
        }

        for (Attribute nonKeyAttribute : entity.getNonKeyAttributes()) {
            if (nonKeyAttribute.isComposite()) {
                // Map composite attributes
                OntClass compositeAttributeClass = addBClass(
                    entityClass,
                    nonKeyAttribute.getName(),
                    !nonKeyAttribute.isMultivalued(),
                    false,
                    true,
                    true
                );

                for (Attribute attribute : nonKeyAttribute.getComponentAttributes()) {
                    addDatatypeProperty(
                        attribute.getName(),
                        compositeAttributeClass,
                        !attribute.isMultivalued(),
                        false
                    );
                }
            } else {
                // Map simple attributes
                addDatatypeProperty(
                    nonKeyAttribute.getName(),
                    entityClass,
                    !nonKeyAttribute.isMultivalued(),
                    false
                );
            }
        }

        return entityClass;
    }

    private void mapStrongEntities() throws MappingException {
        for (Entity entity : schema.getStrongEntities()) {
            mapEntity(entity);
        }
    }

    private void mapWeakEntitiesAndIdentifyingRelationships() throws MappingException {
        for (Entity entity : schema.getWeakEntities()) {
            Relationship relationship = schema.getIdentifyingRelationship(
                entity.getOwnerName(), entity.getName());

            if (relationship == null) {
                throw new MappingException("There is no identifying relationship between weak"
                    + " entity " + entity.getName() + " and strong entity "
                    + entity.getOwnerName() + ".");
            }

            ParticipatingEntity ownerParticipatingEntity =
                relationship.getParticipatingEntity(entity.getOwnerName());

            OntClass ownerEntityClass = getClass(entity.getOwnerName());
            OntClass weakEntityClass = mapEntity(entity);

            addHasIsOfObjectProperties(
                weakEntityClass.getLocalName(),
                ownerEntityClass,
                weakEntityClass,
                (ownerParticipatingEntity.getMax() == 1),
                (ownerParticipatingEntity.getMin() == 1),
                true,
                true
            );
        }
    }

    private void mapBinaryRelationshipsWithoutAttributes() throws MappingException {
        for (Relationship relationship : schema.getRelationships()) {
            if (!relationship.isIdentifying()
                && relationship.isBinary()
                && relationship.getAttributes().isEmpty()
            ) {
                ParticipatingEntity aParticipatingEntity =
                    relationship.getParticipatingEntities().get(0);
                ParticipatingEntity bParticipatingEntity =
                    relationship.getParticipatingEntities().get(1);

                String inversePropertiesBasename = Utils.capitalizeCleanName(
                    StringUtils.isBlank(bParticipatingEntity.getRole())
                    ? bParticipatingEntity.getName()
                    : bParticipatingEntity.getRole()
                );

                addInverseObjectProperties(
                    Utils.uncapitalizeCleanName(relationship.getName()),
                    inversePropertiesBasename,
                    "",
                    "is",
                    inversePropertiesBasename,
                    "Of",
                    getClass(aParticipatingEntity.getName()),
                    getClass(bParticipatingEntity.getName()),
                    (aParticipatingEntity.getMax() == 1),
                    (aParticipatingEntity.getMin() == 1),
                    (bParticipatingEntity.getMax() == 1),
                    (bParticipatingEntity.getMin() == 1)
                );
            }
        }
    }

    private void mapBinaryRelationshipsWithAttributes() throws MappingException {
        for (Relationship relationship : schema.getRelationships()) {
            if (!relationship.isIdentifying()
                && relationship.isBinary()
                && !relationship.getAttributes().isEmpty()
            ) {
                ParticipatingEntity aParticipatingEntity =
                    relationship.getParticipatingEntities().get(0);
                ParticipatingEntity bParticipatingEntity =
                    relationship.getParticipatingEntities().get(1);


            }
        }
    }

    /**
     * Mapping Rule 5. Map each ternary relationship into a class with three pairs of inverse
     * object properties between participating entity classes and the relationship class. Map
     * participating entities’ cardinality into min and max cardinality restrictions or combine
     * characteristics of a functional property with a min cardinality restriction for object
     * properties pointing from participating entity classes to the relationship class. For their
     * inverse object properties, min and max cardinality should be set to one.
     */
    private void mapTernaryRelationships() {
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

    private OntClass addKeyClass(OntClass aClass) throws MappingException {
        return addBClass(
            aClass,
            aClass.getLocalName() + "Key",
            true,
            true,
            true,
            true
        );
    }

    private OntClass addBClass(
        OntClass aClass,
        String name,
        boolean aIsFunctional,
        boolean aIsMinCardinalityOne,
        boolean bIsFunctional,
        boolean bIsMinCardinalityOne
    ) throws MappingException {
        OntClass bClass = addClass(name);

        addHasIsOfObjectProperties(
            bClass.getLocalName(),
            aClass,
            bClass,
            aIsFunctional,
            aIsMinCardinalityOne,
            bIsFunctional,
            bIsMinCardinalityOne
        );

        return bClass;
    }

    private List<ObjectProperty> addHasIsOfObjectProperties(
        String basename,
        OntClass aClass,
        OntClass bClass,
        boolean aIsFunctional,
        boolean aIsMinCardinalityOne,
        boolean bIsFunctional,
        boolean bIsMinCardinalityOne
    ) throws MappingException {
        return addInverseObjectProperties(
            "has",
            basename,
            "",
            "is",
            basename,
            "Of",
            aClass,
            bClass,
            aIsFunctional,
            aIsMinCardinalityOne,
            bIsFunctional,
            bIsMinCardinalityOne
        );
    }

    private List<ObjectProperty> addInverseObjectProperties(
        String aPrefix,
        String aBasename,
        String aSuffix,
        String bPrefix,
        String bBasename,
        String bSuffix,
        OntClass aClass,
        OntClass bClass,
        boolean aIsFunctional,
        boolean aIsMinCardinalityOne,
        boolean bIsFunctional,
        boolean bIsMinCardinalityOne
    ) throws MappingException {
        ObjectProperty aProperty = addObjectProperty(
            aPrefix,
            aBasename,
            aSuffix,
            aClass,
            bClass,
            null,
            aIsFunctional,
            aIsMinCardinalityOne
        );

        ObjectProperty bProperty = addObjectProperty(
            bPrefix,
            bBasename,
            bSuffix,
            bClass,
            aClass,
            aProperty,
            bIsFunctional,
            bIsMinCardinalityOne
        );

        List<ObjectProperty> properties = new ArrayList<ObjectProperty>();
        properties.add(aProperty);
        properties.add(bProperty);

        return properties;
    }

    private ObjectProperty addObjectProperty(
        String prefix,
        String basename,
        String suffix,
        OntClass domainClass,
        OntClass rangeClass,
        ObjectProperty inverseOfProperty,
        boolean isFunctional,
        boolean isMinCardinalityOne
    ) throws MappingException {
        String name = prefix + Utils.capitalizeCleanName(basename) + suffix;
        String uri = NS + name;

        if (model.getObjectProperty(uri) != null) {
            throw new MappingException("Object property " + name + " already exists.");
        }

        ObjectProperty property =
            inverseOfProperty != null && inverseOfProperty.isFunctionalProperty()
            ? model.createInverseFunctionalProperty(uri, isFunctional)
            : model.createObjectProperty(uri, isFunctional);
        property.addDomain(domainClass);
        property.addRange(rangeClass);

        if (inverseOfProperty != null) {
            property.addInverseOf(inverseOfProperty);
        }

        if (isMinCardinalityOne) {
            domainClass.addSuperClass(model.createMinCardinalityRestriction(null, property, 1));
        }

        return property;
    }

    private DatatypeProperty addDatatypeProperty(
        String basename,
        OntClass domainClass,
        boolean isFunctional,
        boolean isMinCardinalityOne
    ) throws MappingException {
        String name = "has" + Utils.capitalizeCleanName(basename);
        String uri = NS + name;

        if (model.getDatatypeProperty(uri) != null) {
            throw new MappingException("Datatype property " + name + " already exists.");
        }

        DatatypeProperty property = model.createDatatypeProperty(uri, isFunctional);
        property.addDomain(domainClass);
        property.addRange(XSD.xstring);

        if (isMinCardinalityOne) {
            domainClass.addSuperClass(model.createMinCardinalityRestriction(null, property, 1));
        }

        return property;
    }

    private OntClass addClass(String name) throws MappingException {
        name = Utils.capitalizeCleanName(name);
        String uri = NS + name;

        if (model.getOntClass(uri) != null) {
            throw new MappingException("Class " + name + " already exists.");
        }

        return model.createClass(uri);
    }

    private OntClass getClass(String name) throws MappingException {
        name = Utils.capitalizeCleanName(name);
        String uri = NS + name;

        OntClass clazz = model.getOntClass(uri);

        if (clazz == null) {
            throw new MappingException("Class " + name + " does not exists.");
        }

        return clazz;
    }
}
