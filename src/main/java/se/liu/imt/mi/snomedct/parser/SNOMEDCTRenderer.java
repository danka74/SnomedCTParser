/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.io.AbstractOWLRenderer;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLLogicalEntity;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * @author Daniel Karlsson, Linköping University, daniel.karlsson@liu.se
 * 
 */
/**
 * @author daniel
 *
 */
/**
 * @author daniel
 * 
 */
public class SNOMEDCTRenderer extends AbstractOWLRenderer {

	static Logger logger = Logger.getLogger(SNOMEDCTRenderer.class);

	static final IRI roleGroupIRI = IRI
			.create("http://snomed.info/id/609096000");

	public SNOMEDCTRenderer() {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.io.AbstractOWLRenderer#render(org.semanticweb.
	 * owlapi.model.OWLOntology, java.io.Writer)
	 */
	@Override
	public void render(OWLOntology ontology, Writer writer) {
		try {
			writeExpressions(ontology, writer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Method for writing all SNOMED CT Compositional Grammar compliant
	 * expressions from an ontology. Not all OWL expression could be translated
	 * to Compositional Grammar.
	 * 
	 * @param ontology
	 * @param writer
	 */
	private void writeExpressions(OWLOntology ontology, Writer writer) {
		final List<OWLAxiom> axioms = new ArrayList<OWLAxiom>(
				ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES));

		for (OWLAxiom axiom : axioms) {
			try {
				writeExpression((OWLEquivalentClassesAxiom) axiom, ontology,
						writer);
			} catch (OWLRendererException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Method for writing a single expression corresponding to an OWL equivalent
	 * classes axiom.
	 * 
	 * @param axiom
	 * @param ontology
	 * @param writer
	 * @throws IOException
	 *             Thrown if writing fails.
	 * @throws OWLRendererException
	 *             Thrown if the OWL expression cannot be rendered as a
	 *             Compositional Grammar statement.
	 */
	private void writeExpression(OWLEquivalentClassesAxiom axiom,
			OWLOntology ontology, Writer writer) throws IOException,
			OWLRendererException {
		logger.info(axiom.toString());

		// get the operands of the OWL equivalent classes axiom
		List<OWLClassExpression> classExpressions = axiom
				.getClassExpressionsAsList();

		if (classExpressions.size() != 2)
			throw new OWLRendererException(
					"Number of expressions in equivalence axiom != 2");

		// assume that expressions have fixed positions
		OWLClassExpression expressionClass = classExpressions.get(0);
		OWLClassExpression expressionDefinition = classExpressions.get(1);

		if (expressionClass.getClassExpressionType() != ClassExpressionType.OWL_CLASS)
			throw new OWLRendererException(
					"Left-hand side not an OWL class in equivalence axiom: "
							+ expressionClass.getClassExpressionType()
									.toString());

		// typically there will be an intersection of classes at the top of the
		// OWL expression
		if (expressionDefinition.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)
			writeIntersection((OWLObjectIntersectionOf) expressionDefinition,
					ontology, writer);
		// the exception being when there is exactly one class, i.e. a statement
		// of equivalence between two named classes, probably not that common
		else if (expressionDefinition.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
			writeEntity((OWLClass) expressionDefinition, ontology, writer);
		// other expression types are not allowed in Compositional
		// Grammar
		else
			throw new OWLRendererException(
					"Non-allowed expression type in equivalence axiom: "
							+ expressionDefinition.getClassExpressionType()
									.toString());

		// if the expression class has a label, write that label after tab
		String label = getLabel((OWLClass) expressionClass, ontology);
		if (label != null)
			writer.write("\t" + label);

		// expression written, new line
		writer.write('\n');

	}

	/**
	 * Method for writing an intersection expression as it appears in the root
	 * of an expression definition, or if nested, as a filler of an existential
	 * restriction.
	 * 
	 * @param intersection
	 * @param ontology
	 * @param writer
	 * @throws IOException
	 *             Thrown when writing fails
	 * @throws OWLRendererException
	 *             Thrown if the OWL expression cannot be rendered as a
	 *             Compositional Grammar statement.
	 */
	private void writeIntersection(OWLObjectIntersectionOf intersection,
			OWLOntology ontology, Writer writer) throws IOException,
			OWLRendererException {
		Set<OWLClassExpression> operands = intersection.getOperands();
		Set<OWLClass> genera = new HashSet<OWLClass>();
		Set<OWLObjectSomeValuesFrom> differentiae = new HashSet<OWLObjectSomeValuesFrom>();

		// extract genera and differentiae from the OWL intersection operand
		// expressions
		collectGeneraAndDifferentia(operands, genera, differentiae);

		// an expression must have at least one genus according to Compositional
		// Grammar
		if (genera.isEmpty())
			throw new OWLRendererException("No genera in expression");

		// write all genera separated by infix '+'
		boolean first = true;
		for (OWLClass genus : genera) {
			if (first)
				first = false;
			else
				writer.write('+');

			writeEntity(genus, ontology, writer);
		}

		if (!differentiae.isEmpty()) {
			// separate genera and differentiae with a ':'
			writer.write(':');
			writeDifferentiae(differentiae, ontology, writer);
		}

	}

	/**
	 * Method for writing differentiae. Differentia may be role grouped.
	 * 
	 * @param differentiae
	 * @param ontology
	 * @param writer
	 * @throws IOException
	 *             Thrown when writing fails
	 * @throws OWLRendererException
	 *             Thrown if the OWL expression cannot be rendered as a
	 *             Compositional Grammar statement.
	 */
	private void writeDifferentiae(Set<OWLObjectSomeValuesFrom> differentiae,
			OWLOntology ontology, Writer writer) throws IOException,
			OWLRendererException {
		final OWLObjectProperty roleGroupProperty = ontology
				.getOWLOntologyManager().getOWLDataFactory()
				.getOWLObjectProperty(roleGroupIRI);

		boolean first = true;
		for (OWLObjectSomeValuesFrom differentia : differentiae) {
			if (first)
				first = false;
			else
				writer.write(',');

			// if it's a role group...
			if (differentia.getProperty().getNamedProperty()
					.equals(roleGroupProperty)) {
				OWLClassExpression roleGroupContent = differentia.getFiller();
				// it it's a single differentia in the role group
				if (roleGroupContent.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
					writer.write('{');
					writeSomeValuesFrom(
							(OWLObjectSomeValuesFrom) roleGroupContent,
							ontology, writer);
					writer.write('}');
				}
				// if there is an intersection of differentiae in the role
				// group...
				else if (roleGroupContent.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
					writer.write('{');
					OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) roleGroupContent;
					Set<OWLObjectSomeValuesFrom> roleGroupDifferentiae = new HashSet<OWLObjectSomeValuesFrom>();
					for (OWLClassExpression operand : intersection
							.getOperands())
						// only existential restrictions are allowed in role
						// groups, other kinds of expression will cause
						// exception
						if (operand.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
							roleGroupDifferentiae
									.add((OWLObjectSomeValuesFrom) operand);
						else
							throw new OWLRendererException(
									"Non-existential restriction in role group");
					writeDifferentiae(roleGroupDifferentiae, ontology, writer);
					writer.write('}');
				} else
					throw new OWLRendererException(
							"Role group must contain either a single existential restriction or an intersection");
			} else {
				// not a role group...
				writeSomeValuesFrom((OWLObjectSomeValuesFrom) differentia,
						ontology, writer);
			}
		}

	}

	/**
	 * Utility method for extracting genera and differentiae from a set of class
	 * expressions. Genera appear as OWLClass objects, differentiae appear as
	 * existential restrictions. Both could be nested inside intersections
	 * recursively.
	 * 
	 * @param operands
	 * @param genera
	 * @param differentia
	 */
	private void collectGeneraAndDifferentia(Set<OWLClassExpression> operands,
			Set<OWLClass> genera, Set<OWLObjectSomeValuesFrom> differentia) {
		for (OWLClassExpression operand : operands) {
			switch (operand.getClassExpressionType()) {
			case OWL_CLASS:
				genera.add((OWLClass) operand);
				break;
			case OBJECT_INTERSECTION_OF:
				collectGeneraAndDifferentia(
						((OWLObjectIntersectionOf) operand).getOperands(),
						genera, differentia);
				break;
			case OBJECT_SOME_VALUES_FROM:
				differentia.add((OWLObjectSomeValuesFrom) operand);
			default:
				break;
			}
		}
	}

	/**
	 * Method for writing existential restriction expressions. The filler of the
	 * expressions could potentially be a nested expression.
	 * 
	 * @param differentia
	 * @param ontology
	 * @param writer
	 * @throws IOException
	 *             Thrown when writing fails
	 * @throws OWLRendererException
	 *             Thrown if the OWL expression cannot be rendered as a
	 *             Compositional Grammar statement.
	 */
	private void writeSomeValuesFrom(OWLObjectSomeValuesFrom differentia,
			OWLOntology ontology, Writer writer) throws IOException,
			OWLRendererException {
		OWLObjectProperty property = differentia.getProperty()
				.asOWLObjectProperty();
		writeEntity(property, ontology, writer);

		writer.write('=');

		OWLClassExpression filler = differentia.getFiller();
		logger.info("filler = " + filler.toString());
		// if filler is an intersection, then it is a nested expression
		if (filler.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
			writer.write('(');
			writeIntersection((OWLObjectIntersectionOf) filler, ontology,
					writer);
			writer.write(')');
		} else if (filler.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
			writeEntity((OWLClass) filler, ontology, writer);
		else
			throw new OWLRendererException(
					"Type of the filler of an existational restriction can only be a class or an intersection: "
							+ filler.getClassExpressionType().toString());

	}

	/**
	 * Utility method for writing an OWL entity, here an OWL class or object
	 * property, as a SCTID plus label.
	 * 
	 * @param entity
	 * @param ontology
	 * @param writer
	 * @throws IOException
	 *             Thrown when writing fails
	 * @throws OWLRendererException
	 *             Thrown if the OWL expression cannot be rendered as a
	 *             Compositional Grammar statement.
	 */
	private void writeEntity(OWLLogicalEntity entity, OWLOntology ontology,
			Writer writer) throws IOException, OWLRendererException {
		String namespace = entity.getIRI().getNamespace();
		String sctid = extractID(namespace);
		logger.info(sctid);
		writer.write(sctid);

		// only rdfs:label annotations will be written
		String label = getLabel(entity, ontology);
		if (label != null) {
			writer.write("|" + label + "|");
		}

	}

	/**
	 * Utility method for getting a rdfs:label from an OWL entity.
	 * 
	 * @param entity
	 * @param ontology
	 * @return The label as a String.
	 */
	private String getLabel(OWLLogicalEntity entity, OWLOntology ontology) {
		String label = null;
		Set<OWLAnnotation> annotations = entity.getAnnotations(ontology);
		for (OWLAnnotation annotation : annotations) {
			if (annotation.getProperty().getIRI()
					.equals(OWLRDFVocabulary.RDFS_LABEL.getIRI())) {
				OWLLiteral val = (OWLLiteral) annotation.getValue();
				label = val.getLiteral();
			}
		}
		return label;
	}

	/**
	 * Utility method for extracting SCTID from an IRI
	 * 
	 * @param iri
	 * @return The substring after the last '/' in the IRI, the SCTID if the IRI
	 *         is a SNOMED CT URI
	 * @throws OWLRendererException
	 *             Thrown if the IRI ends with '/' and thus cannot be a valid
	 *             SNOMED CT URI
	 */
	String extractID(String iri) throws OWLRendererException {
		if (iri.endsWith("/"))
			throw new OWLRendererException("IRI ends with '/'");
		else
			return iri.substring(iri.lastIndexOf('/') + 1);
	}

}