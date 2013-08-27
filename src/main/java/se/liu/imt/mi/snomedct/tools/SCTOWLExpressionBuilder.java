/**
 * 
 */
package se.liu.imt.mi.snomedct.tools;

import java.util.HashSet;
import java.util.Set;

import org.antlr.runtime.tree.Tree;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 * @author Mikael Nyström, mikael.nystrom@liu.se
 */
public class SCTOWLExpressionBuilder {

	/**
	 * @param ontology
	 * @param dataFactory
	 */
	public SCTOWLExpressionBuilder(OWLOntology ontology,
			OWLDataFactory dataFactory) {
		super();
		this.ontology = ontology;
		this.dataFactory = dataFactory;
	}

	private OWLOntology ontology;
	private OWLDataFactory dataFactory;

	/**
	 * Translates an AST from the parser to an <code>OWLClassExpression</code>.
	 * 
	 * @param ast
	 *            The tree to translate
	 * @return An <code>OWLClassExpression</code>
	 * @throws Exception
	 */
	public OWLClassExpression translateToOWL(Tree ast) throws Exception {
		if (ast != null) {
			switch (ast.getType()) {
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.DIFF:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.GENUS:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.AND:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.TOP_AND: {
				// System.out.println("TestSCTExpressionParser.translate(): AND");
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
								.create("http://www.ihtsdo.org/#RoleGroup")),
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
