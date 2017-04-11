package com.myrosh.erowlmapper;

import com.myrosh.erowlmapper.er.ERParser;
import com.myrosh.erowlmapper.er.ERSchema;
import com.myrosh.erowlmapper.er.parser.YAMLERParser;
import com.myrosh.erowlmapper.owl.OWLLiteOntology;

import java.io.FileReader;
import java.io.PrintWriter;

/**
 * @author igorm
 *
 * A launcher class for EROWLMapper
 *
 */
public class App
{
    public static void main(String[] args)
    {
        ERParser parser = new YAMLERParser();
        EROWLMapper mapper = new EROWLMapper();

        try {
            ERSchema schema = parser.parse(new FileReader(args[0]));
            schema.validate();

            OWLLiteOntology ontology = mapper.map(schema);
            ontology.write(new PrintWriter(args[1], "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
