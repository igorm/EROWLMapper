# EROWLMapper

EROWLMapper illustrates an approach to automatically mapping
[ER schemas](http://myrosh.com/er-model-overview) to
[OWL ontologies](http://myrosh.com/owl-web-ontology-language-overview)
presented in [I. Myroshnichenko and M. Murphy, Mapping ER Schemas to OWL Ontologies, IEEE Sixth International Conference on Semantic Computing, Berkeley, CA, 2009](https://www.computer.org/csdl/proceedings/icsc/2009/3800/00/3800a324-abs.html).
The mapping logic is located in the [Mapper](src/main/java/com/myrosh/erowl/Mapper.java) class.

### Input

Input ER schemas are defined using [YAML](https://en.wikipedia.org/wiki/YAML):

```yaml
--- # Person-Car ER Schema
entities:
    -
        name: Person
        attributes:
            -
                name: ssn
                key: true
            -
                name: name
    -
        name: Car
        attributes:
            -
                name: vin
                key: true
            -
                name: make
            -
                name: model
relationships:
    -
        name: Drives
        participatingEntities:
            -
                name: Person
                role: Driver
            -
                name: Car
                role: Vehicle
                min: 1

```

### Output

Output OWL ontologies are serialized using [Apache Jena](https://jena.apache.org) in
[RDF/XML Syntax](http://myrosh.com/owl-web-ontology-language-overview/#Syntaxes).

### Running EROWLMapper

EROWLMapper is using [Apache Maven](https://maven.apache.org). You can package and run the application as follows:

```
mvn package exec:java -Dexec.mainClass="com.myrosh.erowl.App" -Dexec.args="in_er_schema.yml out_ontology.owl" -q
```


