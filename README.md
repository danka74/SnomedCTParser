SnomedCTParser
==============

[![Build](https://travis-ci.org/danka74/SnomedCTParser.png)](https://travis-ci.org/danka74/SnomedCTParser)

ANTLR 4 parser for the draft 2014 SNOMED CT Compositional Grammar.

OWL API 3.x compliant classes for loading and storing SNOMED CT Compositional Grammar statements or expressions.

File format is a tab-separated text file:
(\<expression> \t \<label> \n)*

###Installation

```
mvn install
```
Additionally, there are some utility methods in the se.liu.imt.mi.snomedct.expression.tools package.
###SNOMEDCTTranslator

Usage:
```
java -cp SnomedCTParser-0.0.3-jar-with-dependencies.jar se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTGraph <input file> [-f [turtle|owlf|sct] -s <SNOMED CT OWL file> -n [stated|distribution|flat] -l]
```

The input file is any OWL file or a SNOMED CT Compositional grammar file (using the file format described above).

The flag -f selects the format for the output file.

The flag -n selects any processing of the ontology in the input file. "stated" means no processing. "distribution" means that the ontology is classified and that a Distribution Normal Form transformation is applied. [Only applicable to the SNOMED CT Observables model: "flat" means that any classes based on the nested version of the Obseravbles model is tranlated to the flat version of the Obseravbles model.]

The -s flag is used to supply a SNOMED CT OWL file (or possibly a module) which is imported into the ontology from the input file before classification (if applicable).

The -l flag, when present, labels are added to OWL classes.

###SNOMEDCTGraph

Usage:
```
java -cp SnomedCTParser-0.0.3-jar-with-dependencies.jar se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTGraph <input file> [-s <SNOMED CT concepts release file> -o <output file> -f]
```
The input file is a text file with a single compositional grammar statement, e.g.
```
(276885007 | Core body temperature (observable entity) |)
===
(363787002|Observable entity (observable entity)|:
    704346009|Specified by (attribute)|=(
        386053000|Evaluation procedure (procedure)|:
            704327008|Direct site (attribute)|=42859004|Tympanic membrane structure (body structure)|,
            704347000|Observes (attribute)|=(
                123456789|Feature of entity  (qualifier value)|:
                    704318007|Property type (attribute)|= 123456789|Temperature (qualifier value)|,
                    704319004|Inheres in (attribute)|=278826002|Body internal region (body structure)|
            )
    )
)
```
Note that you need a full statement, now only a single concept on the left hand side.

The `<SNOMED CT concepts release file>` is a snapshot concept file from the release, used to be able to determine if concepts are primitive or fully defined (differently colored boxes), can be omitted

the `<output file>` is used for providing a name for the resulting SVG file. If omitted the filename is the same as input but with a .svg extension.

`-f` makes fully defined default, e.g. if there is no corresponding concept ID in the SNOMED CT concept file.
