package com.myrosh.erowlmapper;

import java.lang.ClassLoader;

import java.io.FileReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.Assert;

import com.myrosh.erowlmapper.er.ERSchema;
import com.myrosh.erowlmapper.er.ERParser;
import com.myrosh.erowlmapper.er.parser.YAMLERParser;
import com.myrosh.erowlmapper.owl.OWLLiteOntology;

/**
 * EROWLMapper unit tests.
 */
public class EROWLMapperTest
{
    @Test
    public void testMapEntityWithSingleCompositeKeyAttribute() throws Exception {
        Assert.assertTrue(isMappedERMatchingOWL("entity_with_single_composite_key_attribute"));
    }

    @Test
    public void testMapEntityWithSingleSimpleKeyAttribute() throws Exception {
        Assert.assertTrue(isMappedERMatchingOWL("entity_with_single_simple_key_attribute"));
    }

    @Test
    public void testMapEntityWithMultipleSimpleKeyAttributes() throws Exception {
        Assert.assertTrue(isMappedERMatchingOWL("entity_with_multiple_simple_key_attributes"));
    }

    @Test
    public void testMapEntityWithSimpleAttribute() throws Exception {
        Assert.assertTrue(isMappedERMatchingOWL("entity_with_simple_attribute"));
    }

    @Test
    public void testMapEntityWithCompositeAttribute() throws Exception {
        Assert.assertTrue(isMappedERMatchingOWL("entity_with_composite_attribute"));
    }

    @Test
    public void testMapWeakEntitiesAndIdentifyingRelationships() throws Exception {
        Assert.assertTrue(isMappedERMatchingOWL("weak_entity"));
    }

    @Test
    public void testMapBinaryRelationshipsWithoutAttributes() throws Exception {
        Assert.assertTrue(isMappedERMatchingOWL("binary_relationship_without_attributes"));
    }

    @Test
    public void testMapBinaryRelationshipsWithAttributes() throws Exception {
        Assert.assertTrue(isMappedERMatchingOWL("binary_relationship_with_attributes"));
    }

    private boolean isMappedERMatchingOWL(String baseFilename) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();

        String erFilename =  baseFilename + ".yml";
        String erFileResourcePath = classLoader.getResource(erFilename).getPath();

        ERParser parser = new YAMLERParser();
        ERSchema schema = parser.parse(new FileReader(erFileResourcePath));

        schema.validate();

        EROWLMapper mapper = new EROWLMapper();
        OWLLiteOntology ontology = mapper.map(schema);

        StringWriter owlStringWriter = new StringWriter();
        ontology.write(owlStringWriter);
        // ontology.write(new PrintWriter("FOOBAR.owl", "UTF-8"));

        String owlFilename =  baseFilename + ".owl";
        String owlFileResourcePath = classLoader.getResource(owlFilename).getPath();

        String candidateOwl = owlStringWriter.toString();
        String correctOwl = new String(Files.readAllBytes(Paths.get(owlFileResourcePath)), "UTF-8");

        return candidateOwl.equals(correctOwl);
    }
}
