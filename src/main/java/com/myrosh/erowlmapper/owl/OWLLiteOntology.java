package com.myrosh.erowlmapper.owl;

import com.myrosh.erowlmapper.Utils;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.XSD;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author igorm
 *
 * Models an OWL ontology
 *
 */
public class OWLLiteOntology {

    /**
     * Namespace constant
     */
    public static final String NS = "http://www.semanticweb.org/ontologies/erowlmapper#";

    /**
     * Syntax constant
     */
    public static final String SYNTAX = "RDF/XML-ABBREV";

    /**
     * OWL model
     */
    private OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);

    /**
     *
     * @param aClass
     * @return
     * @throws OWLException
     */
    public OntClass addOWLKeyClass(OntClass aClass) throws OWLException {
        return addOWLBClass(
            aClass,
            aClass.getLocalName() + "Key",
            true,
            true,
            true,
            true
        );
    }

    /**
     *
     * @param aClass
     * @param name
     * @param aIsFunctional
     * @param aIsMinCardinalityOne
     * @param bIsFunctional
     * @param bIsMinCardinalityOne
     * @return
     * @throws OWLException
     */
    public OntClass addOWLBClass(
        OntClass aClass,
        String name,
        boolean aIsFunctional,
        boolean aIsMinCardinalityOne,
        boolean bIsFunctional,
        boolean bIsMinCardinalityOne
    ) throws OWLException {
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

    /**
     *
     * @param basename
     * @param aClass
     * @param bClass
     * @param aIsFunctional
     * @param aIsMinCardinalityOne
     * @param bIsFunctional
     * @param bIsMinCardinalityOne
     * @return
     * @throws OWLException
     */
    public List<ObjectProperty> addOWLHasIsOfObjectProperties(
        String basename,
        OntClass aClass,
        OntClass bClass,
        boolean aIsFunctional,
        boolean aIsMinCardinalityOne,
        boolean bIsFunctional,
        boolean bIsMinCardinalityOne
    ) throws OWLException {
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

    /**
     *
     * @param aPrefix
     * @param aBasename
     * @param aSuffix
     * @param bPrefix
     * @param bBasename
     * @param bSuffix
     * @param aClass
     * @param bClass
     * @param aIsFunctional
     * @param aIsMinCardinalityOne
     * @param bIsFunctional
     * @param bIsMinCardinalityOne
     * @return
     * @throws OWLException
     */
    public List<ObjectProperty> addOWLInverseObjectProperties(
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
    ) throws OWLException {
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

    /**
     *
     * @param prefix
     * @param basename
     * @param suffix
     * @param domainClass
     * @param rangeClass
     * @param inverseOfProperty
     * @param isFunctional
     * @param isMinCardinalityOne
     * @return
     * @throws OWLException
     */
    public ObjectProperty addOWLObjectProperty(
        String prefix,
        String basename,
        String suffix,
        OntClass domainClass,
        OntClass rangeClass,
        ObjectProperty inverseOfProperty,
        boolean isFunctional,
        boolean isMinCardinalityOne
    ) throws OWLException {
        String name = prefix + Utils.capitalizeCleanName(basename) + suffix;
        String uri = NS + name;

        if (model.getObjectProperty(uri) != null) {
            throw new OWLException("Object property " + name + " already exists.");
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
                inverseOfProperty.convertToInverseFunctionalProperty();
            }
        }

        if (isMinCardinalityOne) {
            domainClass.addSuperClass(model.createMinCardinalityRestriction(null, property, 1));
        }

        return property;
    }

    /**
     *
     * @param basename
     * @param domainClass
     * @param isFunctional
     * @param isMinCardinalityOne
     * @return
     * @throws OWLException
     */
    public DatatypeProperty addOWLDatatypeProperty(
        String basename,
        OntClass domainClass,
        boolean isFunctional,
        boolean isMinCardinalityOne
    ) throws OWLException {
        String name = "has" + Utils.capitalizeCleanName(basename);
        String uri = NS + name;

        if (model.getDatatypeProperty(uri) != null) {
            throw new OWLException("Datatype property " + name + " already exists.");
        }

        DatatypeProperty property = model.createDatatypeProperty(uri, isFunctional);
        property.addDomain(domainClass);
        property.addRange(XSD.xstring);

        if (isMinCardinalityOne) {
            domainClass.addSuperClass(model.createMinCardinalityRestriction(null, property, 1));
        }

        return property;
    }

    /**
     *
     * @param name
     * @return
     * @throws OWLException
     */
    public OntClass getOWLClass(String name) throws OWLException {
        name = Utils.capitalizeCleanName(name);
        String uri = NS + name;

        OntClass clazz = model.getOntClass(uri);

        if (clazz == null) {
            throw new OWLException("Class " + name + " does not exists.");
        }

        return clazz;
    }

    /**
     *
     * @param name
     * @return
     * @throws OWLException
     */
    public OntClass addOWLClass(String name) throws OWLException {
        name = Utils.capitalizeCleanName(name);
        String uri = NS + name;

        if (model.getOntClass(uri) != null) {
            throw new OWLException("Class " + name + " already exists.");
        }

        return model.createClass(uri);
    }

    /**
     *
     * @param writer
     */
    public void write(Writer writer) {
        model.write(writer, SYNTAX);
    }
}
