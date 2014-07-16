SnomedCTParser
==============
ANTLR parser for SNOMED CT Compositional Grammar and some parts of the SNOMED CT Query Specification.

OWL API compliant classes for loading and storing SNOMED CT Compositional Grammar expressions in a tab separated file format:

(\<expression> \t \<label> \n)*

An OWL file containing SNOMED CT will have to be added to src/test/resources directory for the tests, see test.TestSNOMEDCTExpressionParser.testConvertToOWL()

Java heap size might need to be increasd. Running "mvn test -DMAVEN_OPTS=-Xmx6500m" has been succesful on an 8G machine.

http://www.ihtsdo.org/snomed-ct/snomed-docs/snomed-ct-query-specification/

http://www.ihtsdo.org/fileadmin/user_upload/Docs_01/About_IHTSDO/Publications/CompositionalGrammar_20081223.pdf
