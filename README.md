SnomedCTParser
==============

[![Build](https://travis-ci.org/danka74/SnomedCTParser.png)](https://travis-ci.org/danka74/SnomedCTParser) [![Dependency Status](https://www.versioneye.com/user/projects/56a1586c89a03205c400010e/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56a1586c89a03205c400010e)

ANTLR 4 parser for the draft 2014 SNOMED CT Compositional Grammar.

OWL API 4.x compliant classes for loading and storing SNOMED CT Compositional Grammar statements or expressions.

File format is a sequence of 
Compositionl Grammar statements.

###Installation

#### From source
```
mvn install
```

#### From Maven Central
```
<dependency>
    <groupId>se.liu.imt.mi.snomedct</groupId>
    <artifactId>SnomedCTParser</artifactId>
    <version>0.3</version>
</dependency>
```
Additionally, there are some utility methods in the se.liu.imt.mi.snomedct.expression.tools package.
###SNOMEDCTTranslator

Usage:
```
java -jar SnomedCTParser-0.3-jar-with-dependencies.jar <input file> [-f [turtle|owlf|sct] -s <SNOMED CT OWL file> -n [stated|distribution|flat] -l]
```

The input file is any OWL file or a SNOMED CT Compositional grammar file (using the file format described above).

The flag -f selects the format for the output file.

The flag -n selects any processing of the ontology in the input file. "stated" means no processing. "distribution" means that the ontology is classified and that a Distribution Normal Form transformation is applied. [Only applicable to the SNOMED CT Observables model: "flat" means that any classes based on the nested version of the Obseravbles model is translated into the flat version of the Obseravbles model.]

The -s flag is used to supply a SNOMED CT OWL file (or possibly a module) which is imported into the ontology from the input file before classification (if applicable).

The -l flag, when present, labels are added to OWL classes/Compositional Grammar expressions.

###SNOMEDCTSignatureGenerator

Usage:
```
java -cp SnomedCTParser-0.3-jar-with-dependencies.jar se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTSignatureGenerator <input file> [-f <format>] [<output file>]
```

The input file is a Compositional Grammar file as e.g. generated by the tool above.

The flag -f selects the format for the output file, either:
* *url* for a file with lines on the form "http:<i></i>//snomed.org/id/&lt;SCTID&gt;"
* *|* for a file with lines on the form "&lt;SCTID&gt;|"
