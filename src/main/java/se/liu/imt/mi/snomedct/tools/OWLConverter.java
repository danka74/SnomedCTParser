/**
 * 
 */
package se.liu.imt.mi.snomedct.tools;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import se.liu.imt.mi.snomedct.expression.SCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SCTExpressionParser;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxOntologyFormat;

/**
 * @author danka
 * 
 */
public class OWLConverter {

	static private OWLOntologyManager manager;
	static private OWLDataFactory dataFactory;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		manager = OWLManager.createOWLOntologyManager();
		dataFactory = manager.getOWLDataFactory();

		CharStream input = new ANTLRStringStream(args[0]);
		SCTExpressionLexer lexer = new SCTExpressionLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SCTExpressionParser parser = new SCTExpressionParser(tokens);
		SCTExpressionParser.expression_return result = null;
		try {
			result = parser.expression();
		} catch (RecognitionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		OWLClassExpression e = null;
		try {
			if(result != null)
				e = translateToOWL((Tree) result.getTree());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			
			return;
		}

		StringWriter sw = new StringWriter();
		OWLOntologyFormat of = new DLSyntaxOntologyFormat();
		ManchesterOWLSyntaxPrefixNameShortFormProvider ssfp = new ManchesterOWLSyntaxPrefixNameShortFormProvider(
				of); // new SimpleShortFormProvider();
		ManchesterOWLSyntaxObjectRenderer renderer = new ManchesterOWLSyntaxObjectRenderer(
				sw, ssfp);
		renderer.visit((OWLObjectIntersectionOf) e);
		
		System.out.println(sw.toString());

	}

	/**
	 * Translates an AST from the parser to an <code>OWLClassExpression</code>.
	 * 
	 * @param ast
	 *            The tree to translate
	 * @return An <code>OWLClassExpression</code>
	 * @throws Exception
	 */
	private static OWLClassExpression translateToOWL(Tree ast) throws Exception {
		if (ast != null) {
			switch (ast.getType()) {
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.DIFF:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.GENUS:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.AND:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.TOP_AND: {
//				System.out.println("TestSCTExpressionParser.translate: AND");
				if (ast.getChildCount() > 1) {
					Set<OWLClassExpression> conceptSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < ast.getChildCount(); i++)
						conceptSet.add(translateToOWL(ast.getChild(i)));
					return dataFactory.getOWLObjectIntersectionOf(conceptSet);
				} else
					return translateToOWL(ast.getChild(0));
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SCTID: {
//				System.out.println("TestSCTExpressionParser.translate: SCTID "
//						+ ast.getText());
				IRI iri = IRI.create("http://www.ihtsdo.org/SCT_"
						+ ast.getText());
				return dataFactory.getOWLClass(iri);
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SOME: {
//				System.out.println("TestSCTExpressionParser.translate: SOME "
//						+ ast.getChild(0).getText());
				IRI iri = IRI.create("http://www.ihtsdo.org/SCT_"
						+ ast.getChild(0).getText());
				return dataFactory.getOWLObjectSomeValuesFrom(
						dataFactory.getOWLObjectProperty(iri),
						translateToOWL(ast.getChild(1)));
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP: {
//				System.out
//						.println("TestSCTExpressionParser.translate: ROLEGROUP");
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

}
