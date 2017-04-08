package com.myrosh.erowlmapper;

import com.myrosh.erowlmapper.er.ERSchema;
import com.myrosh.erowlmapper.er.ERSchemaParser;
import com.myrosh.erowlmapper.er.parser.YAMLERSchemaParser;
import org.apache.jena.ontology.OntModel;

import java.io.FileReader;
import java.io.PrintWriter;

/**
 * @author igorm
 *
 * A launcher class for erowl
 *
 */
public class App
{
    public static void main(String[] args)
    {
        ERSchemaParser parser = new YAMLERSchemaParser();
        EROWLMapper mapper = new EROWLMapper();

        try {
            ERSchema schema = parser.parse(new FileReader(args[0]));
            schema.validate();

            OntModel model = mapper.map(schema);

            model.write(new PrintWriter(args[1], "UTF-8"), "RDF/XML-ABBREV");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
