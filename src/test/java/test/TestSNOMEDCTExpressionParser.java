package test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import se.liu.imt.mi.snomedct.expression.*;
import se.liu.imt.mi.snomedct.expression.tools.SCTOWLExpressionBuilder;
import se.liu.imt.mi.snomedct.expression.tools.SCTSortedExpressionBuilder;
import se.liu.imt.mi.snomedct.expression.tools.SnomedCTParser;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxOntologyFormat;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class TestSNOMEDCTExpressionParser {

	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory dataFactory;
	private OWLReasoner reasoner;

	static Logger logger = Logger.getLogger(TestSNOMEDCTExpressionParser.class);

	public static final String PC_IRI = "http://snomed.info/expid/";
	
	public static final String snomedOWLFileName = "/res_StatedOWLF_Core_INT_20140131.owl";

	@Before
	public void setUp() throws OWLOntologyCreationException {

	}

	@Test
	public void testExpressionParser() throws Exception {
		URL testCaseURL = getClass().getResource("/sct_test_cases.txt");
		BufferedReader testCaseReader = new BufferedReader(new FileReader(
				testCaseURL.getFile()));

		String strLine;

		manager = OWLManager.createOWLOntologyManager();
		dataFactory = manager.getOWLDataFactory();

		while ((strLine = testCaseReader.readLine()) != null) {

			if (strLine.startsWith("#"))
				continue;

			String[] strTokens = strLine.split("\t");

			logger.info(strTokens[0]);

			Tree result = SnomedCTParser.parseQuery(strTokens[0]);

			String sortedResult = SCTSortedExpressionBuilder
					.buildSortedExpression(result);

			logger.info("Sorted: " + sortedResult);

			printTree(result, 2);

			assertTrue(sortedResult.equals(strTokens[1]));
		}

		testCaseReader.close();
	}

	@Test
	public void testConvertToOWL() throws Exception {
		manager = OWLManager.createOWLOntologyManager();

		logger.info("Loading SNOMED CT ontology...");
		URL snomedFileURL = getClass().getResource(snomedOWLFileName);
		if(snomedFileURL == null)
			throw new FileNotFoundException("SNOMED CT OWL file '" + snomedOWLFileName + "' not found");
		ontology = manager.loadOntologyFromOntologyDocument(new File(snomedFileURL.getFile()));
		dataFactory = manager.getOWLDataFactory();

		OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
		reasoner = reasonerFactory.createReasoner(ontology);
		long t1 = (new Date()).getTime();
		reasoner.flush();
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

		SCTOWLExpressionBuilder owlBuilder = new SCTOWLExpressionBuilder(
				ontology, dataFactory);

		long interval = (new Date()).getTime() - t1;
		logger.info("Classification time: " + interval + " ms");

		URL testCaseURL = getClass().getResource("/sct_test_cases.txt");
		BufferedReader testCaseReader = new BufferedReader(new FileReader(
				testCaseURL.getFile()));

		String strLine;
		while ((strLine = testCaseReader.readLine()) != null) {
			if (strLine.startsWith("#"))
				continue;

			String[] strTokens = strLine.split("\t");

			logger.info(strTokens[0]);

			CharStream input = new ANTLRStringStream(strTokens[0]);
			SCTExpressionLexer lexer = new SCTExpressionLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			SCTExpressionParser parser = new SCTExpressionParser(tokens);
			SCTExpressionParser.expression_return result = parser.expression();

			t1 = (new Date()).getTime();

			//
			OWLClass new_pc_concept = dataFactory.getOWLClass(IRI.create(PC_IRI
					+ UUID.randomUUID()));

			// translate SCT expression syntax to OWL
			OWLClassExpression e = owlBuilder.translateToOWL(
					(Tree) result.getTree(), null);

			List<OWLOntologyChange> axiomList = new LinkedList<OWLOntologyChange>();
			axiomList.add(new AddAxiom(ontology, dataFactory
					.getOWLEquivalentClassesAxiom(new_pc_concept, e)));
			manager.applyChanges(axiomList);

			reasoner.flush();
			reasoner.precomputeInferences();

			Node<OWLClass> equivalentClasses = reasoner
					.getEquivalentClasses(new_pc_concept);

			NodeSet<OWLClass> parents = reasoner.getSuperClasses(
					new_pc_concept, true);
			NodeSet<OWLClass> children = reasoner.getSubClasses(new_pc_concept,
					true);

			interval = (new Date()).getTime() - t1;

			StringWriter sw = new StringWriter();
			OWLOntologyFormat of = new DLSyntaxOntologyFormat();
			ManchesterOWLSyntaxPrefixNameShortFormProvider ssfp = new ManchesterOWLSyntaxPrefixNameShortFormProvider(
					of); // new SimpleShortFormProvider();
			ManchesterOWLSyntaxObjectRenderer renderer = new ManchesterOWLSyntaxObjectRenderer(
					sw, ssfp);
			if (e.getClass() == uk.ac.manchester.cs.owl.owlapi.OWLClassImpl.class)
				renderer.visit((OWLClass) e);
			else
				renderer.visit((OWLObjectIntersectionOf) e);

			logger.info(sw.toString());

			logger.info("# eq classes: " + equivalentClasses.getSize()
					+ ", time: " + interval + " ms");

			StringBuilder sb = new StringBuilder();
			for (Node<OWLClass> n : parents) {
				for (OWLClass c : n.getEntities()) {
					sb.append(c.toString()).append(' ');
				}
			}
			logger.info("Parents: " + sb.toString());

			sb.setLength(0);
			for (Node<OWLClass> n : children) {
				for (OWLClass c : n.getEntities()) {
					sb.append(c.toString()).append(' ');
				}
			}
			logger.info("Children: " + sb.toString());

			sb.setLength(0);
			for (OWLClass c : equivalentClasses) {
				sb.append(c.toString()).append(' ');
			}
			logger.info("Equivalences: " + sb.toString());
		}

		testCaseReader.close();

		assertTrue(true);

	}

	public static void printTree(Tree tree, int indent) {
		for (int i = 0; i < indent; i++) {
			System.out.print(' ');
		}
		System.out.print(SCTExpressionParser.tokenNames[tree.getType()]);
		System.out.println(": " + tree.toString());

		for (int i = 0; i < tree.getChildCount(); i++) {
			printTree(tree.getChild(i), indent + 2);
		}
	}

}
