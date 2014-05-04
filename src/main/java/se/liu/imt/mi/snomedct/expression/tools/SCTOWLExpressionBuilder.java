/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.tree.Tree;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * Class for building an OWL class expression from a parse tree, output from
 * SNOMED CT ANTLR parser.
 * 
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 * @author Mikael Nyström, mikael.nystrom@liu.se
 */
public class SCTOWLExpressionBuilder {

	public static final String SCTID_IRI = "http://snomed.info/id/";
	public static final String SCTOP_IRI = "http://snomed.info/id/";
	public static final String ROLEGROUP_IRI = "http://snomed.info/id/609096000";

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
	public OWLClassExpression translateToOWL(Tree ast,
			Map<IRI, OWLAnnotation> labels) throws Exception {
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
					for (int i = 0; i < ast.getChildCount(); i++) {
						OWLClassExpression e = translateToOWL(ast.getChild(i),
								labels);
						conceptSet.add(e);
					}
					return dataFactory.getOWLObjectIntersectionOf(conceptSet);
				} else
					return translateToOWL(ast.getChild(0), labels);
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SCTID: {
				IRI iri = IRI.create(SCTID_IRI + ast.getText());
				if (labels != null && ast.getChildCount() > 0)
					labels.put(
							iri,
							dataFactory.getOWLAnnotation(
									dataFactory
											.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL
													.getIRI()),
									dataFactory
											.getOWLLiteral(removeVerticalBars(ast
													.getChild(0).getText()))));
				return dataFactory.getOWLClass(iri);
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SOME: {
				Tree property = ast.getChild(0);
				IRI iri = IRI.create(SCTOP_IRI + property.getText());
				if (labels != null && property.getChildCount() > 0)
					labels.put(
							iri,
							dataFactory.getOWLAnnotation(
									dataFactory
											.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL
													.getIRI()),
									dataFactory
											.getOWLLiteral(removeVerticalBars(property
													.getChild(0).getText()))));
				return dataFactory.getOWLObjectSomeValuesFrom(
						dataFactory.getOWLObjectProperty(iri),
						translateToOWL(ast.getChild(1), labels));
			}
			case se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP: {
				return dataFactory.getOWLObjectSomeValuesFrom(dataFactory
						.getOWLObjectProperty(IRI.create(ROLEGROUP_IRI)),
						translateToOWL(ast.getChild(0), labels));
			}
			default:
				throw new Exception("Undetermined AST node type: "
						+ ast.getType());
			}
		} else
			return null;
	}

	String removeVerticalBars(String input) {
		if(input.startsWith("|")) 
			input = input.substring(1);
		if(input.endsWith("|"))
			input = input.substring(0, input.length()-1 );
		return input;
	}
}
