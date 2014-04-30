/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.util.HashSet;
import java.util.Set;

import org.antlr.runtime.tree.Tree;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Class for building an OWL class expression from a parse tree, output from
 * SNOMED CT ANTLR parser.
 * 
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 * @author Mikael Nyström, mikael.nystrom@liu.se
 */
public class SCTOWLExpressionBuilder {
	
	@SuppressWarnings("unused")
	public static final String PC_IRI = "http://www.imt.liu.se/mi/snomedct#PC_";
	public static final String SCTID_IRI = "http://www.ihtsdo.org/#SCTID_";
	public static final String SCTOP_IRI = "http://www.ihtsdo.org/#SCTOP_";
	public static final String ROLEGROUP_IRI = "http://www.ihtsdo.org/#RoleGroup";

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
			// There are many ways of expression intersection in the SNOMED CT
			// compositional grammar!
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.DIFF:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.GENUS:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.AND:
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.TOP_AND: {
				if (ast.getChildCount() > 1) {
					Set<OWLClassExpression> conceptSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < ast.getChildCount(); i++)
						conceptSet.add(translateToOWL(ast.getChild(i)));
					return dataFactory.getOWLObjectIntersectionOf(conceptSet);
				} else
					return translateToOWL(ast.getChild(0));
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SCTID: {
				IRI iri = IRI.create(SCTID_IRI + ast.getText());
				//if (ontology.containsClassInSignature(iri))
					return dataFactory.getOWLClass(iri);
				//else
					//return dataFactory.getOWLClass(iri);
					//throw new Exception("Non-existing SCT concept: " + iri);
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SOME: {
				IRI iri = IRI.create(SCTOP_IRI + ast.getChild(0).getText());
				//if (ontology.containsObjectPropertyInSignature(iri))
					return dataFactory.getOWLObjectSomeValuesFrom(
							dataFactory.getOWLObjectProperty(iri),
							translateToOWL(ast.getChild(1)));
				//else
				//	throw new Exception("Non-existing SCT attribute: " + iri);
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP: {
				return dataFactory.getOWLObjectSomeValuesFrom(dataFactory
						.getOWLObjectProperty(IRI
								.create(ROLEGROUP_IRI)),
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
