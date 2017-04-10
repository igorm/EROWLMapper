package com.myrosh.erowlmapper;

import java.util.List;
import java.util.ArrayList;

import com.myrosh.erowlmapper.er.*;
import org.apache.commons.lang3.StringUtils;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.XSD;

import com.myrosh.erowlmapper.er.ERSchema;

/**
 * @author igorm
 *
 * An ER to OWL mapper implementation using Apache Jena
 *
 */
public class EROWLMapper {

    /**
     * Namespace URI constant
     */
    public static final String NS = "http://www.semanticweb.org/ontologies/erowlmapper#";

    /**
     * ER schema
     */
    private ERSchema schema;

    /**
     * OWL Ontology
     */
    private OntModel model;

    /**
     * @param schema
     * @return
     * @throws EROWLMappingException
     */
    public OntModel map(ERSchema schema) throws EROWLMappingException {
        this.schema = schema;
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);

        mapStrongEntities();
        mapWeakEntitiesAndIdentifyingRelationships();
        mapBinaryRelationshipsWithoutAttributes();
        mapBinaryRelationshipsWithAttributes();



        mapTernaryRelationships();

        return model;
    }

    private OntClass mapEntity(EREntity entity) throws EROWLMappingException {
        // Map the entity
        OntClass entityClass = addOWLClass(entity.getName());
        List<ERAttribute> keyAttributes = entity.getKeyAttributes();

        if (keyAttributes.size() == 1) {
            ERAttribute keyAttribute = keyAttributes.get(0);

            if (keyAttribute.isComposite()) {
                // Map the single composite key attribute
                OntClass keyClass = addOWLKeyClass(entityClass);

                for (ERAttribute attribute : keyAttribute.getComponentAttributes()) {
                    addOWLDatatypeProperty(attribute.getName(), keyClass, true, true);
                }
            } else {
                // Map the single simple key attribute
                addOWLDatatypeProperty(keyAttribute.getName(), entityClass, true, true);
            }
        } else if (keyAttributes.size() > 1) {
            // Map multiple simple key attributes
            OntClass keyClass = addOWLKeyClass(entityClass);

            for (ERAttribute attribute : keyAttributes) {
                addOWLDatatypeProperty(attribute.getName(), keyClass, true, true);
            }
        }

        for (ERAttribute nonKeyAttribute : entity.getNonKeyAttributes()) {
            if (nonKeyAttribute.isComposite()) {
                // Map composite attributes
                OntClass compositeAttributeClass = addOWLBClass(
                    entityClass,
                    nonKeyAttribute.getName(),
                    !nonKeyAttribute.isMultivalued(),
                    false,
                    true,
                    true
                );

                for (ERAttribute attribute : nonKeyAttribute.getComponentAttributes()) {
                    addOWLDatatypeProperty(
                        attribute.getName(),
                        compositeAttributeClass,
                        !attribute.isMultivalued(),
                        false
                    );
                }
            } else {
                // Map simple attributes
                addOWLDatatypeProperty(
                    nonKeyAttribute.getName(),
                    entityClass,
                    !nonKeyAttribute.isMultivalued(),
                    false
                );
            }
        }

        return entityClass;
    }

    private void mapStrongEntities() throws EROWLMappingException {
        for (EREntity entity : schema.getStrongEntities()) {
            mapEntity(entity);
        }
    }

    private void mapWeakEntitiesAndIdentifyingRelationships() throws EROWLMappingException {
        for (EREntity weakEntity : schema.getWeakEntities()) {
            ERRelationship relationship = schema.getIdentifyingBinaryRelationship(weakEntity);

            if (relationship == null) {
                throw new EROWLMappingException("Weak " + weakEntity + " does not have exactly 1"
                    + " identifying binary ERRelationship.");
            }

            ERParticipatingEntity aParticipatingEntity =
                relationship.getParticipatingEntitiesExcluding(weakEntity).get(0);
            ERParticipatingEntity bParticipatingEntity =
                relationship.getParticipatingEntity(weakEntity);

            OntClass aClass = getOWLClass(aParticipatingEntity.getName());
            OntClass bClass = mapEntity(weakEntity);

            addOWLHasIsOfObjectProperties(
                bParticipatingEntity.getRoleOrName(),
                aClass,
                bClass,
                (aParticipatingEntity.getMax() == 1),
                (aParticipatingEntity.getMin() == 1),
                true,
                true
            );
        }
    }

    private void mapBinaryRelationshipsWithoutAttributes() throws EROWLMappingException {
        for (ERRelationship relationship : schema.getRelationships()) {
            if (!relationship.isIdentifying()
                && relationship.isBinary()
                && relationship.getAttributes().isEmpty()
            ) {
                ERParticipatingEntity aParticipatingEntity =
                    relationship.getParticipatingEntities().get(0);
                ERParticipatingEntity bParticipatingEntity =
                    relationship.getParticipatingEntities().get(1);

                addOWLHasIsOfObjectProperties(
                    bParticipatingEntity.getRoleOrName(),
                    getOWLClass(aParticipatingEntity.getName()),
                    getOWLClass(bParticipatingEntity.getName()),
                    (aParticipatingEntity.getMax() == 1),
                    (aParticipatingEntity.getMin() == 1),
                    (bParticipatingEntity.getMax() == 1),
                    (bParticipatingEntity.getMin() == 1)
                );
            }
        }
    }

    private void mapBinaryRelationshipsWithAttributes() throws EROWLMappingException {
        for (ERRelationship relationship : schema.getRelationships()) {
            if (!relationship.isIdentifying()
                && relationship.isBinary()
                && !relationship.getAttributes().isEmpty()
            ) {
                ERParticipatingEntity aParticipatingEntity =
                    relationship.getParticipatingEntities().get(0);
                ERParticipatingEntity bParticipatingEntity =
                    relationship.getParticipatingEntities().get(1);

                OntClass aClass = getOWLClass(aParticipatingEntity.getName());
                OntClass bClass = getOWLClass(bParticipatingEntity.getName());

                String relationshipClassName = aParticipatingEntity.getRoleOrName()
                    + bParticipatingEntity.getRoleOrName();
                OntClass relationshipClass = addOWLBClass(
                    aClass,
                    relationshipClassName,
                    (aParticipatingEntity.getMax() == 1),
                    (aParticipatingEntity.getMin() == 1),
                    true,
                    true
                );

                for (ERAttribute attribute : relationship.getAttributes()) {
                    addOWLDatatypeProperty(
                        attribute.getName(),
                        relationshipClass,
                        !attribute.isMultivalued(),
                        false
                    );
                }

                addOWLHasIsOfObjectProperties(
                    relationshipClassName,
                    bClass,
                    relationshipClass,
                    (bParticipatingEntity.getMax() == 1),
                    (bParticipatingEntity.getMin() == 1),
                    true,
                    true
                );
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
        for (ERRelationship relationship : schema.getRelationships()) {
            if (relationship.getParticipatingEntities().size() == 3) {
                ERParticipatingEntity firstParticipatingEntity = relationship.getParticipatingEntities().get(0);
                ERParticipatingEntity secondParticipatingEntity = relationship.getParticipatingEntities().get(1);
                ERParticipatingEntity thirdParticipatingEntity = relationship.getParticipatingEntities().get(2);

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

    private OntClass addOWLKeyClass(OntClass aClass) throws EROWLMappingException {
        return addOWLBClass(
            aClass,
            aClass.getLocalName() + "Key",
            true,
            true,
            true,
            true
        );
    }

    private OntClass addOWLBClass(
        OntClass aClass,
        String name,
        boolean aIsFunctional,
        boolean aIsMinCardinalityOne,
        boolean bIsFunctional,
        boolean bIsMinCardinalityOne
    ) throws EROWLMappingException {
        OntClass bClass = addOWLClass(name);

        addOWLHasIsOfObjectProperties(
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

    private List<ObjectProperty> addOWLHasIsOfObjectProperties(
        String basename,
        OntClass aClass,
        OntClass bClass,
        boolean aIsFunctional,
        boolean aIsMinCardinalityOne,
        boolean bIsFunctional,
        boolean bIsMinCardinalityOne
    ) throws EROWLMappingException {
        return addOWLInverseObjectProperties(
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

    private List<ObjectProperty> addOWLInverseObjectProperties(
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
    ) throws EROWLMappingException {
        ObjectProperty aProperty = addOWLObjectProperty(
            aPrefix,
            aBasename,
            aSuffix,
            aClass,
            bClass,
            null,
            aIsFunctional,
            aIsMinCardinalityOne
        );

        ObjectProperty bProperty = addOWLObjectProperty(
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

    private ObjectProperty addOWLObjectProperty(
        String prefix,
        String basename,
        String suffix,
        OntClass domainClass,
        OntClass rangeClass,
        ObjectProperty inverseOfProperty,
        boolean isFunctional,
        boolean isMinCardinalityOne
    ) throws EROWLMappingException {
        String name = prefix + Utils.capitalizeCleanName(basename) + suffix;
        String uri = NS + name;

        if (model.getObjectProperty(uri) != null) {
            throw new EROWLMappingException("Object property " + name + " already exists.");
        }

        ObjectProperty property =
            inverseOfProperty != null && inverseOfProperty.isFunctionalProperty()
            ? model.createInverseFunctionalProperty(uri, isFunctional)
            : model.createObjectProperty(uri, isFunctional);
        property.addDomain(domainClass);
        property.addRange(rangeClass);

        if (inverseOfProperty != null) {
            property.addInverseOf(inverseOfProperty);

            if (property.isFunctionalProperty()) {
                ((OntProperty)inverseOfProperty).convertToInverseFunctionalProperty();
            }
        }

        if (isMinCardinalityOne) {
            domainClass.addSuperClass(model.createMinCardinalityRestriction(null, property, 1));
        }

        return property;
    }

    private DatatypeProperty addOWLDatatypeProperty(
        String basename,
        OntClass domainClass,
        boolean isFunctional,
        boolean isMinCardinalityOne
    ) throws EROWLMappingException {
        String name = "has" + Utils.capitalizeCleanName(basename);
        String uri = NS + name;

        if (model.getDatatypeProperty(uri) != null) {
            throw new EROWLMappingException("Datatype property " + name + " already exists.");
        }

        DatatypeProperty property = model.createDatatypeProperty(uri, isFunctional);
        property.addDomain(domainClass);
        property.addRange(XSD.xstring);

        if (isMinCardinalityOne) {
            domainClass.addSuperClass(model.createMinCardinalityRestriction(null, property, 1));
        }

        return property;
    }

    private OntClass addOWLClass(String name) throws EROWLMappingException {
        name = Utils.capitalizeCleanName(name);
        String uri = NS + name;

        if (model.getOntClass(uri) != null) {
            throw new EROWLMappingException("Class " + name + " already exists.");
        }

        return model.createClass(uri);
    }

    private OntClass getOWLClass(String name) throws EROWLMappingException {
        name = Utils.capitalizeCleanName(name);
        String uri = NS + name;

        OntClass clazz = model.getOntClass(uri);

        if (clazz == null) {
            throw new EROWLMappingException("Class " + name + " does not exists.");
        }

        return clazz;
    }
}
