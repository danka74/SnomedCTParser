
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import se.liu.imt.mi.snomedct.expression.*;
import se.liu.imt.mi.snomedct.expression.SCTExpressionParser.expressionOrQuery_return;
import se.liu.imt.mi.snomedct.tools.SCTOWLExpressionBuilder;
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

public class TestSCTExpressionParser {

	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory dataFactory;
	private OWLReasoner reasoner;

	static Logger logger = Logger.getLogger(TestSCTExpressionParser.class);

	@Before
	public void setUp() throws OWLOntologyCreationException {

	}

	@Test
	public void testExpressionParser() throws Exception {
		BufferedReader testCaseReader = new BufferedReader(new FileReader(
				"src/test/resources/sct_test_cases.txt"));

		String strLine;

		manager = OWLManager.createOWLOntologyManager();
		dataFactory = manager.getOWLDataFactory();

		while ((strLine = testCaseReader.readLine()) != null) {
			logger.info(strLine);
			if (strLine.startsWith("#"))
				continue;

			CharStream input = new ANTLRStringStream(strLine);
			SCTExpressionLexer lexer = new SCTExpressionLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			SCTExpressionParser parser = new SCTExpressionParser(tokens);
			expressionOrQuery_return result = parser.expressionOrQuery();

			String sortedResult = buildSortedOutput((Tree) result.getTree());
			// printTree((Tree) result.getTree(), 0);
			logger.info("Sorted: " + sortedResult);

		}

		testCaseReader.close();
	}

	@Test
	public void testConvertToOWL() throws Exception {

		manager = OWLManager.createOWLOntologyManager();

		logger.info("Loading SNOMED CT ontology...");
		ontology = manager.loadOntologyFromOntologyDocument(IRI
				.create("file:///home/daniel/snomed.owl"));
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

		BufferedReader testCaseReader = new BufferedReader(new FileReader(
				"src/test/resources/sct_test_cases.txt"));

		String strLine;
		while ((strLine = testCaseReader.readLine()) != null) {
			logger.info(strLine);
			if (strLine.startsWith("#"))
				continue;

			CharStream input = new ANTLRStringStream(strLine);
			SCTExpressionLexer lexer = new SCTExpressionLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			SCTExpressionParser parser = new SCTExpressionParser(tokens);
			SCTExpressionParser.expression_return result = parser.expression();

			t1 = (new Date()).getTime();

			OWLClass new_pc_concept = dataFactory.getOWLClass(IRI
					.create("http://www.imt.liu.se/mi/snomedct#PC_"
							+ UUID.randomUUID()));

			// translate SCT expression syntax to OWL
			OWLClassExpression e = owlBuilder.translateToOWL((Tree) result
					.getTree());

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

			StringBuffer sb = new StringBuffer();
			logger.info("Parents: ");
			for (Node<OWLClass> n : parents) {
				for (OWLClass c : n.getEntities()) {
					sb.append(c.toString());
					sb.append(' ');
				}
			}
			logger.info(sb.toString());

			sb.setLength(0);
			logger.info("Children: ");
			for (Node<OWLClass> n : children) {
				for (OWLClass c : n.getEntities()) {
					sb.append(c.toString());
					sb.append(' ');
				}
			}
			logger.info(sb.toString());

			sb.setLength(0);
			logger.info("Eq: ");
			for (OWLClass c : equivalentClasses) {
				sb.append(c.toString());
				sb.append(' ');
			}
		}

		testCaseReader.close();

		assertTrue(true);

	}

	/**
	 * Translates an AST from the parser to an <code>OWLClassExpression</code>.
	 * 
	 * @param ast
	 *            The tree to translate
	 * @return An <code>OWLClassExpression</code>
	 * @throws Exception
	 */
	private OWLClassExpression translateToOWL(Tree ast) throws Exception {
		if (ast != null) {
			switch (ast.getType()) {
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.DIFF:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.GENUS:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.AND:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.TOP_AND: {
				logger.info(SCTExpressionParser.tokenNames[ast.getType()]
						+ ", children=" + ast.getChildCount());

				if (ast.getChildCount() > 1) {
					Set<OWLClassExpression> conceptSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < ast.getChildCount(); i++)
						conceptSet.add(translateToOWL(ast.getChild(i)));
					return dataFactory.getOWLObjectIntersectionOf(conceptSet);
				} else
					return translateToOWL(ast.getChild(0));
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SCTID: {
				// System.out
				// .println("TestSCTExpressionParser.translate(): SCTID "
				// + ast.getText());
				IRI iri = IRI.create("http://www.ihtsdo.org/#SCTID_"
						+ ast.getText());
				if (ontology.containsClassInSignature(iri))
					return dataFactory.getOWLClass(iri);
				else
					throw new Exception("Non-existing SCT concept: " + iri);
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SOME: {
				// System.out.println("TestSCTExpressionParser.translate(): SOME "
				// + ast.getChild(0).getText());
				IRI iri = IRI.create("http://www.ihtsdo.org/#SCTOP_"
						+ ast.getChild(0).getText());
				if (ontology.containsObjectPropertyInSignature(iri))
					return dataFactory.getOWLObjectSomeValuesFrom(
							dataFactory.getOWLObjectProperty(iri),
							translateToOWL(ast.getChild(1)));
				else
					throw new Exception("Non-existing SCT attribute: " + iri);
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP: {
				// System.out
				// .println("TestSCTExpressionParser.translate(): ROLEGROUP");
				return dataFactory.getOWLObjectSomeValuesFrom(dataFactory
						.getOWLObjectProperty(IRI
								.create("http://www.ihtsdo.org/RoleGroup")),
						translateToOWL(ast.getChild(0)));
			}
			default:
				throw new Exception("Undetermined AST node type: "
						+ ast.getType());
			}
		} else
			return null;
	}

	private OWLClassExpression translateToOWL_nocheck(Tree ast)
			throws Exception {
		if (ast != null) {
			switch (ast.getType()) {
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.DIFF:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.GENUS:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.AND:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.TOP_AND: {
				logger.info(SCTExpressionParser.tokenNames[ast.getType()]
						+ ", children=" + ast.getChildCount());

				if (ast.getChildCount() > 1) {
					Set<OWLClassExpression> conceptSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < ast.getChildCount(); i++)
						conceptSet.add(translateToOWL_nocheck(ast.getChild(i)));
					return dataFactory.getOWLObjectIntersectionOf(conceptSet);
				} else
					return translateToOWL_nocheck(ast.getChild(0));
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SCTID: {
				System.out
						.println("TestSCTExpressionParser.translate(): SCTID "
								+ ast.getText());
				IRI iri = IRI.create("http://www.ihtsdo.org/#SCTID_"
						+ ast.getText());
				return dataFactory.getOWLClass(iri);
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SOME: {
				System.out.println("TestSCTExpressionParser.translate(): SOME "
						+ ast.getChild(0).getText());
				IRI iri = IRI.create("http://www.ihtsdo.org/#SCTOP_"
						+ ast.getChild(0).getText());
				return dataFactory.getOWLObjectSomeValuesFrom(
						dataFactory.getOWLObjectProperty(iri),
						translateToOWL_nocheck(ast.getChild(1)));

			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP: {
				System.out
						.println("TestSCTExpressionParser.translate(): ROLEGROUP");
				return dataFactory.getOWLObjectSomeValuesFrom(dataFactory
						.getOWLObjectProperty(IRI
								.create("http://www.ihtsdo.org/#RoleGroup")),
						translateToOWL_nocheck(ast.getChild(0)));
			}
			default:
				throw new Exception("Undetermined AST node type: "
						+ ast.getType());
			}
		} else
			return null;
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

	private String buildSortedOutput(Tree t) {

		String result = "";

		if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.TOP_AND) {

			Tree genera = t.getChild(0);
			TreeSet<String> sortedGenera = new TreeSet<String>();
			for (int i = 0; i < genera.getChildCount(); i++)
				sortedGenera.add(genera.getChild(i).toString());
			for (Iterator<String> i = sortedGenera.iterator(); i.hasNext();) {
				result += i.next();
				if (i.hasNext())
					result += "+";
			}

			Tree diff = t.getChild(1);
			if (diff != null) {
				TreeSet<String> sortedDiff = new TreeSet<String>();
				if (diff.getChildCount() > 0)
					result += ":";

				for (int i = 0; i < diff.getChildCount(); i++)
					sortedDiff.add(buildSortedOutput(diff.getChild(i)));

				for (Iterator<String> i = sortedDiff.iterator(); i.hasNext();) {
					result += i.next();
					if (i.hasNext())
						result += ",";
				}
			}

		}

		if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.AND
				|| t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP) {

			TreeSet<String> sortedList = new TreeSet<String>();

			for (int i = 0; i < t.getChildCount(); i++)
				sortedList.add(buildSortedOutput(t.getChild(i)));

			if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP)
				result += "{";

			for (Iterator<String> i = sortedList.iterator(); i.hasNext();) {
				result += i.next();
				if (i.hasNext())
					result += ",";
			}

			if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP)
				result += "}";
		}

		if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SOME) {
			Tree attr = t.getChild(0);
			Tree value = t.getChild(1);
			if (value.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SCTID) {
				result += attr.toString() + "=" + value.toString();
			} else {
				result += attr.toString() + "=(" + buildSortedOutput(value)
						+ ")";
			}
		}

		return result;
	}

}
