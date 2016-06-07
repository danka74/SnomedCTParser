package test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;
import se.liu.imt.mi.snomedct.parser.OWLVisitor;
import se.liu.imt.mi.snomedct.parser.SortedExpressionVisitor;

public class TestSNOMEDCTExpressionParser {

	static Logger logger = Logger.getLogger(TestSNOMEDCTExpressionParser.class);

	@Before
	public void setUp() {

	}

	@Test
	public void testExpressionParser() throws Exception {
		//org.antlr.v4.runtime.misc.TestRig.main(new String[] {"SNOMEDCTExpression", "axiom", "-gui", "/sct_test_cases.txt"});

		
		URL testCaseURL = getClass().getResource("/sct_test_cases.txt");
		BufferedReader testCaseReader = new BufferedReader(new FileReader(
				testCaseURL.getFile()));

		String strLine;

		OWLOntologyManager manager = OWLManager
				.createOWLOntologyManager();
		OWLOntology ontology = manager.createOntology();
		
		while ((strLine = testCaseReader.readLine()) != null) {

			if (strLine.startsWith("#"))
				continue;

			String[] strTokens = strLine.split("\t");

			logger.info(strTokens[0]);

			ParseTree tree = SNOMEDCTParserUtil.parseExpression(strTokens[0]);

			OWLVisitor visitor = new OWLVisitor(ontology, null);			
			OWLObject o = visitor.visit(tree);
			logger.info(o.toString());
			logger.info(visitor.getLabels().toString());
			
			SortedExpressionVisitor sortVisitor = new SortedExpressionVisitor();
			String sortedExpression = sortVisitor.visit(tree);
			logger.info(sortedExpression);

			assertTrue(sortedExpression.equals(strTokens[1]));
		}

		testCaseReader.close();
	}

//	@Test
//	public void testConvertToOWL() throws Exception {
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//
//		logger.info("Loading SNOMED CT ontology...");
//		String snomedOWLFileName ="/home/danka74/Documents/SCT/SnomedCT_RF2Release_INT_20160131/Resources/StatedRelationshipsToOwlKRSS/snomedct_owlf.owl";
//		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(
//				snomedOWLFileName));
//		OWLDataFactory dataFactory = manager.getOWLDataFactory();
//
//		OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
//		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
//		long t1 = (new Date()).getTime();
//		reasoner.flush();
//		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
//
//		long interval = (new Date()).getTime() - t1;
//		logger.info("Classification time: " + interval + " ms");
//
//		URL testCaseURL = getClass().getResource("/sct_test_cases.txt");
//		BufferedReader testCaseReader = new BufferedReader(new FileReader(
//				testCaseURL.getFile()));
//
//		String strLine;
//		while ((strLine = testCaseReader.readLine()) != null) {
//			if (strLine.startsWith("#"))
//				continue;
//
//			String[] strTokens = strLine.split("\t");
//
//			logger.info(strTokens[0]);
//
//			ParseTree result = SNOMEDCTParserUtil.parseExpression(strTokens[0]);
//
//			t1 = (new Date()).getTime();
//
//			//
//			OWLClass new_pc_concept = dataFactory.getOWLClass(IRI.create(SNOMEDCTParserUtil.PC_IRI
//					+ UUID.randomUUID()));
//
//			SNOMEDCTParserUtil.parseExpressionToOWLAxiom(result, ontology, new_pc_concept, false);
//
//			reasoner.flush();
//			reasoner.precomputeInferences();
//
//			Node<OWLClass> equivalentClasses = reasoner
//					.getEquivalentClasses(new_pc_concept);
//
//			NodeSet<OWLClass> parents = reasoner.getSuperClasses(
//					new_pc_concept, true);
//			NodeSet<OWLClass> children = reasoner.getSubClasses(new_pc_concept,
//					true);
//
//			interval = (new Date()).getTime() - t1;
//
//			logger.info("# eq classes: " + equivalentClasses.getSize()
//					+ ", time: " + interval + " ms");
//
//			StringBuilder sb = new StringBuilder();
//			for (Node<OWLClass> n : parents) {
//				for (OWLClass c : n.getEntities()) {
//					sb.append(c.toString()).append(' ');
//				}
//			}
//			logger.info("Parents: " + sb.toString());
//
//			sb.setLength(0);
//			for (Node<OWLClass> n : children) {
//				for (OWLClass c : n.getEntities()) {
//					sb.append(c.toString()).append(' ');
//				}
//			}
//			logger.info("Children: " + sb.toString());
//
//			sb.setLength(0);
//			for (OWLClass c : equivalentClasses) {
//				sb.append(c.toString()).append(' ');
//			}
//			logger.info("Equivalences: " + sb.toString());
//		}
//
//		testCaseReader.close();
//
//		assertTrue(true);
//
//	}
//
//	public static void printTree(Tree tree, int indent) {
//		for (int i = 0; i < indent; i++) {
//			System.out.print(' ');
//		}
//		System.out.print(tree.toString());
//		System.out.println(": " + tree.toString());
//
//		for (int i = 0; i < tree.getChildCount(); i++) {
//			printTree(tree.getChild(i), indent + 2);
//		}
//	}

}
