package com.myrosh.erowlmapper;

import com.myrosh.erowlmapper.er.*;
import com.myrosh.erowlmapper.owl.OWLException;
import com.myrosh.erowlmapper.owl.OWLLiteOntology;
import org.apache.jena.ontology.OntClass;

import java.util.List;

/**
 * @author igorm
 *
 * An ER to OWL mapper
 *
 */
public class EROWLMapper {

    /**
     * ER schema
     */
    private ERSchema schema;

    /**
     * OWL ontology
     */
    private OWLLiteOntology ontology = new OWLLiteOntology();

    /**
     * @param schema
     * @return
     * @throws EROWLException
     */
    public OWLLiteOntology map(ERSchema schema) throws EROWLException, OWLException {
        this.schema = schema;

        mapStrongEntities();
        mapWeakEntitiesAndIdentifyingRelationships();
        mapBinaryRelationshipsWithoutAttributes();
        mapBinaryRelationshipsWithAttributes();
        mapTernaryRelationships();

        return ontology;
    }

    private void mapStrongEntities() throws OWLException {
        for (EREntity entity : schema.getStrongEntities()) {
            mapEntity(entity);
        }
    }

    private void mapWeakEntitiesAndIdentifyingRelationships() throws EROWLException, OWLException {
        for (EREntity weakEntity : schema.getWeakEntities()) {
            ERRelationship relationship = schema.getIdentifyingBinaryRelationship(weakEntity);

            if (relationship == null) {
                throw new EROWLException("Weak " + weakEntity + " does not have exactly 1"
                    + " identifying binary ERRelationship.");
            }

            ERParticipatingEntity aParticipatingEntity =
                relationship.getParticipatingEntitiesExcluding(weakEntity).get(0);
            ERParticipatingEntity bParticipatingEntity =
                relationship.getParticipatingEntity(weakEntity);

            OntClass aClass = ontology.getOWLClass(aParticipatingEntity.getName());
            OntClass bClass = mapEntity(weakEntity);

            ontology.addOWLHasIsOfObjectProperties(
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

    private void mapBinaryRelationshipsWithoutAttributes() throws OWLException {
        for (ERRelationship relationship : schema.getRelationships()) {
            if (!relationship.isIdentifying()
                && relationship.isBinary()
                && relationship.getAttributes().isEmpty()
            ) {
                ERParticipatingEntity aParticipatingEntity =
                    relationship.getParticipatingEntities().get(0);
                ERParticipatingEntity bParticipatingEntity =
                    relationship.getParticipatingEntities().get(1);

                ontology.addOWLHasIsOfObjectProperties(
                    bParticipatingEntity.getRoleOrName(),
                    ontology.getOWLClass(aParticipatingEntity.getName()),
                    ontology.getOWLClass(bParticipatingEntity.getName()),
                    (aParticipatingEntity.getMax() == 1),
                    (aParticipatingEntity.getMin() == 1),
                    (bParticipatingEntity.getMax() == 1),
                    (bParticipatingEntity.getMin() == 1)
                );
            }
        }
    }

    private void mapBinaryRelationshipsWithAttributes() throws OWLException {
        for (ERRelationship relationship : schema.getRelationships()) {
            if (!relationship.isIdentifying()
                && relationship.isBinary()
                && !relationship.getAttributes().isEmpty()
            ) {
                ERParticipatingEntity aParticipatingEntity =
                    relationship.getParticipatingEntities().get(0);
                ERParticipatingEntity bParticipatingEntity =
                    relationship.getParticipatingEntities().get(1);

                OntClass aClass = ontology.getOWLClass(aParticipatingEntity.getName());
                OntClass bClass = ontology.getOWLClass(bParticipatingEntity.getName());

                OntClass relationshipClass = ontology.addOWLBClass(
                    aClass,
                    aParticipatingEntity.getRoleOrName()
                        + bParticipatingEntity.getRoleOrName(),
                    (aParticipatingEntity.getMax() == 1),
                    (aParticipatingEntity.getMin() == 1),
                    true,
                    true
                );

                for (ERAttribute attribute : relationship.getAttributes()) {
                    ontology.addOWLDatatypeProperty(
                        attribute.getName(),
                        relationshipClass,
                        !attribute.isMultivalued(),
                        false
                    );
                }

                ontology.addOWLHasIsOfObjectProperties(
                    relationshipClass.getLocalName(),
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

    private void mapTernaryRelationships() throws OWLException {
        for (ERRelationship relationship : schema.getRelationships()) {
            if (!relationship.isIdentifying() && relationship.isTernary()) {
                ERParticipatingEntity aParticipatingEntity =
                    relationship.getParticipatingEntities().get(0);
                ERParticipatingEntity bParticipatingEntity =
                    relationship.getParticipatingEntities().get(1);
                ERParticipatingEntity cParticipatingEntity =
                    relationship.getParticipatingEntities().get(2);

                OntClass aClass = ontology.getOWLClass(aParticipatingEntity.getName());
                OntClass bClass = ontology.getOWLClass(bParticipatingEntity.getName());
                OntClass cClass = ontology.getOWLClass(cParticipatingEntity.getName());

                OntClass relationshipClass = ontology.addOWLBClass(
                    aClass,
                    aParticipatingEntity.getRoleOrName()
                        + bParticipatingEntity.getRoleOrName()
                        + cParticipatingEntity.getRoleOrName(),
                    (aParticipatingEntity.getMax() == 1),
                    (aParticipatingEntity.getMin() == 1),
                    true,
                    true
                );

                ontology.addOWLHasIsOfObjectProperties(
                    relationshipClass.getLocalName(),
                    bClass,
                    relationshipClass,
                    (bParticipatingEntity.getMax() == 1),
                    (bParticipatingEntity.getMin() == 1),
                    true,
                    true
                );

                ontology.addOWLHasIsOfObjectProperties(
                    relationshipClass.getLocalName(),
                    cClass,
                    relationshipClass,
                    (cParticipatingEntity.getMax() == 1),
                    (cParticipatingEntity.getMin() == 1),
                    true,
                    true
                );
            }
        }
    }

    private OntClass mapEntity(EREntity entity) throws OWLException {
        // Map the entity
        OntClass entityClass = ontology.addOWLClass(entity.getName());
        List<ERAttribute> keyAttributes = entity.getKeyAttributes();

        if (keyAttributes.size() == 1) {
            ERAttribute keyAttribute = keyAttributes.get(0);

            if (keyAttribute.isComposite()) {
                // Map the single composite key attribute
                OntClass keyClass = ontology.addOWLKeyClass(entityClass);

                for (ERAttribute attribute : keyAttribute.getComponentAttributes()) {
                    ontology.addOWLDatatypeProperty(
                        attribute.getName(),
                        keyClass,
                        true,
                        true
                    );
                }
            } else {
                // Map the single simple key attribute
                ontology.addOWLDatatypeProperty(
                    keyAttribute.getName(),
                    entityClass,
                    true,
                    true
                );
            }
        } else if (keyAttributes.size() > 1) {
            // Map multiple simple key attributes
            OntClass keyClass = ontology.addOWLKeyClass(entityClass);

            for (ERAttribute attribute : keyAttributes) {
                ontology.addOWLDatatypeProperty(
                    attribute.getName(),
                    keyClass,
                    true,
                    true
                );
            }
        }

        for (ERAttribute nonKeyAttribute : entity.getNonKeyAttributes()) {
            if (nonKeyAttribute.isComposite()) {
                // Map composite attributes
                OntClass compositeAttributeClass = ontology.addOWLBClass(
                    entityClass,
                    nonKeyAttribute.getName(),
                    !nonKeyAttribute.isMultivalued(),
                    false,
                    true,
                    true
                );

                for (ERAttribute attribute : nonKeyAttribute.getComponentAttributes()) {
                    ontology.addOWLDatatypeProperty(
                        attribute.getName(),
                        compositeAttributeClass,
                        !attribute.isMultivalued(),
                        false
                    );
                }
            } else {
                // Map simple attributes
                ontology.addOWLDatatypeProperty(
                    nonKeyAttribute.getName(),
                    entityClass,
                    !nonKeyAttribute.isMultivalued(),
                    false
                );
            }
        }

        return entityClass;
    }
}
