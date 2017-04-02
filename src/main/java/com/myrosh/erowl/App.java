package com.myrosh.erowl;

import java.io.FileReader;
import java.io.PrintWriter;

import com.myrosh.erowl.er.schema.Schema;
import com.myrosh.erowl.er.Parser;
import com.myrosh.erowl.er.parser.YAMLParser;

import org.apache.jena.ontology.OntModel;

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
        Parser parser = new YAMLParser();
        Mapper mapper = new Mapper();

        try {
            Schema schema = parser.parse(new FileReader(args[0]));
            schema.validate();

            OntModel model = mapper.map(schema);

            model.write(new PrintWriter(args[1], "UTF-8"), "RDF/XML-ABBREV");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
