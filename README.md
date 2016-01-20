SnomedCTParser
==============

[![Build](https://travis-ci.org/danka74/SnomedCTParser.png)](https://travis-ci.org/danka74/SnomedCTParser)

ANTLR 4 parser for the draft 2014 SNOMED CT Compositional Grammar.

OWL API 3.x compliant classes for loading and storing SNOMED CT Compositional Grammar statements or expressions.

File format is a sequence of 
(\<expression> \t \<label> \n)*

###Installation

```
mvn install
```
Additionally, there are some utility methods in the se.liu.imt.mi.snomedct.expression.tools package.
###SNOMEDCTTranslator

Usage:
```
java -cp SnomedCTParser-0.0.4-jar-with-dependencies.jar se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTTranslator <input file> [-f [turtle|owlf|sct] -s <SNOMED CT OWL file> -n [stated|distribution|flat] -l]
```

The input file is any OWL file or a SNOMED CT Compositional grammar file (using the file format described above).

The flag -f selects the format for the output file.

The flag -n selects any processing of the ontology in the input file. "stated" means no processing. "distribution" means that the ontology is classified and that a Distribution Normal Form transformation is applied. [Only applicable to the SNOMED CT Observables model: "flat" means that any classes based on the nested version of the Obseravbles model is translated into the flat version of the Obseravbles model.]

The -s flag is used to supply a SNOMED CT OWL file (or possibly a module) which is imported into the ontology from the input file before classification (if applicable).

The -l flag, when present, labels are added to OWL classes.


