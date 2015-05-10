SnomedCTParser
==============
ANTLR 4 parser for the draft 2014 SNOMED CT Compositional Grammar.

OWL API 3.x compliant classes for loading and storing SNOMED CT Compositional Grammar statements or expressions.

<<<<<<< HEAD
(\<expression> \t \<label> \n)*

An OWL file containing SNOMED CT will have to be added to src/test/resources directory for the tests, see test.TestSNOMEDCTExpressionParser.testConvertToOWL()

Java heap size might need to be increasd to run tests. Running "mvn test -DMAVEN_OPTS=-Xmx6500m" has been succesful on an 8G machine.

http://www.ihtsdo.org/snomed-ct/snomed-docs/snomed-ct-query-specification/

http://www.ihtsdo.org/fileadmin/user_upload/Docs_01/About_IHTSDO/Publications/CompositionalGrammar_20081223.pdf

There is a branch for the updated compositional grammar here: https://github.com/danka74/SnomedCTParser/tree/CG2014

=======
>>>>>>> branch 'CG2014' of https://github.com/danka74/SnomedCTParser.git
