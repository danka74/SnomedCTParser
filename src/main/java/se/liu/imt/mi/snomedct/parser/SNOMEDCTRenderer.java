/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLLogicalEntity;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * @author Daniel Karlsson, Linköping University, daniel.karlsson@liu.se
 * @author Kent Spackman, IHTSDO, ksp@ihtsdo.org
 * 
 */
public class SNOMEDCTRenderer extends AbstractOWLRenderer {

	static Logger logger = Logger.getLogger(SNOMEDCTRenderer.class);

	static final IRI roleGroupIRI = IRI
			.create("http://snomed.info/id/609096000");

	private boolean labels = true;

	public SNOMEDCTRenderer() {
		this(true);
	}

	public SNOMEDCTRenderer(boolean labels) {
		super();
		this.labels = labels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.io.AbstractOWLRenderer#render(org.semanticweb.
	 * owlapi.model.OWLOntology, java.io.Writer)
	 */
	@Override
	public void render(OWLOntology ontology, Writer writer)
			throws OWLRendererException {
		// check that the ontology is in the OWL 2 EL profile, OWL 2 EL profile
		// is still more expressive than SNOMED CT Compositional Grammar
		// commented out to test specific criteria when rendering
		// TODO: check performance of profile checker
		// TODO: remove try block?
		// OWL2ELProfile profile = new OWL2ELProfile();
		// OWLProfileReport report = profile.checkOntology(ontology);
		// if(!report.isInProfile())
		// throw new OWLRendererException("Ontology not in OWL 2 EL profile");
		try {
			writeExpressions(ontology, writer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Method for writing all SNOMED CT Compositional Grammar compliant
	 * expressions from an ontology. Not all OWL (or OWL 2 EL) expressions can
	 * be translated to Compositional Grammar. The compositional grammar is
	 * extended to allow rendering of SubclassOf axioms.
	 * 
	 * @param ontology
	 * @param writer
	 * @throws IOException
	 *             Thrown if writing fails.
	 */
	private void writeExpressions(OWLOntology ontology, Writer writer)
			throws IOException {

		// iterate over all logical axioms, so far only equivalent classes and
		// subclass axioms are rendered
		for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
			if (axiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES) {
				try {
					writeEquivalentExpression(
							(OWLEquivalentClassesAxiom) axiom, ontology, writer);
				} catch (OWLRendererException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF) {
				try {
					writeSubclassExpression((OWLSubClassOfAxiom) axiom,
							ontology, writer);
				} catch (OWLRendererException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	private void writeEquivalentExpression(OWLEquivalentClassesAxiom axiom,
			OWLOntology ontology, Writer writer) throws IOException,
			OWLRendererException {
		logger.debug(axiom.toString());

		// create local writer to ensure that content isn't written if the
		// expression has errors
		StringWriter localWriter = new StringWriter();

		// get the operands of the OWL equivalent classes axiom
		List<OWLClassExpression> classExpressions = axiom
				.getClassExpressionsAsList();

		if (classExpressions.size() != 2)
			throw new OWLRendererException(
					"Number of expressions in equivalence axiom != 2");

		// assume that expressions have fixed positions
		OWLClassExpression expressionClass = classExpressions.get(0);
		OWLClassExpression expressionDefinition = classExpressions.get(1);

		// TODO: Should be allowed?
		if (expressionClass.getClassExpressionType() != ClassExpressionType.OWL_CLASS)
			throw new OWLRendererException(
					"Left-hand side not an OWL class in equivalence axiom: "
							+ expressionClass.getClassExpressionType()
									.toString());
		
		localWriter.write('(');
		writeEntity((OWLClass)expressionClass, ontology, localWriter);

		// write "===" for fully defined (or equivalent classes axiom)
		localWriter.write(")===(");

		// typically there will be an intersection of classes at the top of the
		// OWL expression
		if (expressionDefinition.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)
			writeIntersection((OWLObjectIntersectionOf) expressionDefinition,
					ontology, localWriter);
		// the exception being when there is exactly one class, i.e. a statement
		// of equivalence between two named classes, probably not that common
		else if (expressionDefinition.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
			writeEntity((OWLClass) expressionDefinition, ontology, localWriter);
		// other expression types are not allowed in Compositional
		// Grammar
		else
			throw new OWLRendererException(
					"Non-allowed expression type in equivalence axiom: "
							+ expressionDefinition.getClassExpressionType()
									.toString());

		localWriter.write(')');

		// expression is finally written, new line
		writer.write(localWriter.toString() + '\n');

	}

	/**
	 * Method for writing a single expression corresponding to an OWL subclassof
	 * axiom.
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
	private void writeSubclassExpression(OWLSubClassOfAxiom axiom,
			OWLOntology ontology, Writer writer) throws IOException,
			OWLRendererException {
		logger.debug(axiom.toString());

		// create local writer to ensure that content isn't written if the
		// expression has errors
		StringWriter localWriter = new StringWriter();

		// get the operands of the OWL subclassof axiom
		OWLClassExpression expressionClass = axiom.getSubClass();
		OWLClassExpression expressionDefinition = axiom.getSuperClass();

		if (expressionClass.getClassExpressionType() != ClassExpressionType.OWL_CLASS)
			throw new OWLRendererException(
					"Left-hand side not an OWL class in subclassof axiom: "
							+ expressionClass.getClassExpressionType()
									.toString());

		localWriter.write('(');
		writeEntity((OWLClass)expressionClass, ontology, localWriter);
		
		// write "<<<" for primitive (or subclassof axiom)
		localWriter.write(")<<<(");

		// typically there will be an intersection of classes at the top of the
		// OWL expression
		if (expressionDefinition.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)
			writeIntersection((OWLObjectIntersectionOf) expressionDefinition,
					ontology, localWriter);
		// when there is exactly one class, i.e. a statement
		// of subclass relationship between two named classes
		else if (expressionDefinition.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
			writeEntity((OWLClass) expressionDefinition, ontology, localWriter);
		// other expression types are not allowed in Compositional
		// Grammar
		else
			throw new OWLRendererException(
					"Non-allowed expression type in subclassof axiom: "
							+ expressionDefinition.getClassExpressionType()
									.toString());

		localWriter.write(')');

		// expression is finally written, new line
		writer.write(localWriter.toString() + '\n');

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
									"Role group can only directly contain existential restrictions: "
											+ operand.getClassExpressionType()
													.toString());
					writeDifferentiae(roleGroupDifferentiae, ontology, writer);
					writer.write('}');
				} else
					throw new OWLRendererException(
							"Role group must contain either a single existential restriction or an intersection: "
									+ roleGroupContent.getClassExpressionType()
											.toString());
			} else {
				// not a role group...
				writeSomeValuesFrom((OWLObjectSomeValuesFrom) differentia,
						ontology, writer);
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
		logger.debug("filler = " + filler.toString());
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
					"Type of the filler of an existentional restriction can only be a class or an intersection: "
							+ filler.getClassExpressionType().toString());

	}

	/**
	 * Utility method for extracting genera and differentiae from a set of class
	 * expressions. Genera appear as OWLClass objects, differentiae appear as
	 * existential restrictions. Both could be nested inside intersections
	 * recursively.
	 * 
	 * @param operands
	 * @param genera
	 *            Set of OWL classes, changed through side effects
	 * @param differentia
	 *            Set of OWL existential restrictions, changed through side
	 *            effects
	 * @throws OWLRendererException
	 *             Thrown if the OWL expression cannot be rendered as a
	 *             Compositional Grammar statement.
	 */
	private void collectGeneraAndDifferentia(Set<OWLClassExpression> operands,
			Set<OWLClass> genera, Set<OWLObjectSomeValuesFrom> differentia)
			throws OWLRendererException {
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
				break;
			default:
				throw new OWLRendererException("Class expression not allowed: "
						+ operand.getClassExpressionType().toString());
			}
		}
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
		String sctid = extractID(entity.getIRI().toString());
		logger.debug(sctid);
		writer.write(sctid);

		if (labels) {
			// only rdfs:label annotations will be written
			String label = getLabel(entity, ontology);
			if (label != null) {
				writer.write("|" + label + "|");
			}
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
		else {
			String sctid = iri.substring(iri.lastIndexOf('/') + 1);
			// if (!isPositiveInteger(sctid))
			// throw new OWLRendererException(
			// "SCTID part of IRI is non-numeric");
			// else
			return sctid;
		}
	}

	public static boolean isPositiveInteger(String str) {
		for (char c : str.toCharArray())
			if (!Character.isDigit(c))
				return false;
		return true;
	}

}
