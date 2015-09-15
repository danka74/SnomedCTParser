/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.normalform.NormalFormRewriter;

/**
 * @author danka74
 *
 */
public class Flattener implements NormalFormRewriter {

	static Logger logger = Logger
			.getLogger(DistributionNormalFormConverter.class);

	private OWLOntologyManager manager;

	private OWLClassExpression observableEntity;
	private OWLObjectProperty specifiedBy;
	private OWLClassExpression evaluationProcedure;
	private OWLClassExpression featureOfEntity;
	private OWLObjectProperty observes;

	/**
	 * 
	 */
	public Flattener(OWLOntologyManager manager) {
		super();
		this.manager = manager;
		observableEntity = manager.getOWLDataFactory().getOWLClass(
				IRI.create("http://snomed.info/id/363787002"));
		specifiedBy = manager.getOWLDataFactory().getOWLObjectProperty(
				IRI.create("http://snomed.info/id/704346009"));
		evaluationProcedure = manager.getOWLDataFactory().getOWLClass(
				IRI.create("http://snomed.info/id/386053000"));
		featureOfEntity = manager.getOWLDataFactory().getOWLClass(
				IRI.create("http://snomed.info/id/414237002"));
		observes = manager.getOWLDataFactory().getOWLObjectProperty(
				IRI.create("http://snomed.info/id/704347000"));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.normalform.NormalFormRewriter#convertToNormalForm
	 * (org.semanticweb.owlapi.model.OWLClassExpression)
	 */
	@Override
	public OWLClassExpression convertToNormalForm(
			OWLClassExpression inputClassExpression) {

		// filter out non-Observables

		// Observables must be intersections
		if (inputClassExpression.getClassExpressionType() != ClassExpressionType.OBJECT_INTERSECTION_OF)
			return inputClassExpression;

		// Must have 363787002 | Observable entity (observable entity) | as an
		// immediate parent (????)
		OWLObjectIntersectionOf inputAsIntersection = (OWLObjectIntersectionOf) inputClassExpression;
		if (!inputAsIntersection.containsConjunct(observableEntity))
			return inputClassExpression;

		// look for 704346009 | Specified by (attribute) |
		Set<OWLClassExpression> operands = inputAsIntersection.getOperands();
		OWLClassExpression specifiedByValue = null;
		for (OWLClassExpression o1 : operands) {
			if (o1.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM
					&& ((OWLObjectSomeValuesFrom) o1).getProperty() == specifiedBy) {
				specifiedByValue = ((OWLObjectSomeValuesFrom) o1).getFiller();
				break;
			}
		}
		// if there is not specified by attribute return the input
		if (specifiedByValue == null)
			return inputClassExpression;

		// what is the filler of the specified by attribute must be an
		// intersection
		if (specifiedByValue.getClassExpressionType() != ClassExpressionType.OBJECT_INTERSECTION_OF)
			return inputClassExpression;

		// Must have 386053000 | Evaluation procedure (procedure) | in
		// intersection
		if (!((OWLObjectIntersectionOf) specifiedByValue)
				.containsConjunct(evaluationProcedure))
			return inputClassExpression;

		// create the new expression, now that we are reasonably deep into the
		// structure
		Set<OWLClassExpression> flatExpressionSet = new HashSet<OWLClassExpression>();
		flatExpressionSet.add(observableEntity);

		// look for 704347000 | Observes (attribute) |
		Set<OWLClassExpression> operands2 = ((OWLObjectIntersectionOf) specifiedByValue)
				.getOperands();
		OWLClassExpression observesValue = null;
		for (OWLClassExpression o2 : operands2) {

			if (o2.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM
					&& ((OWLObjectSomeValuesFrom) o2).getProperty() == observes) {
				observesValue = ((OWLObjectSomeValuesFrom) o2).getFiller();
			} else if (o2 != evaluationProcedure)
				// add to new flat expression unless it's the evaluation
				// procedure concept
				flatExpressionSet.add(o2);

		}
		// if there is not observes attribute return the input
		if (observesValue == null)
			return inputClassExpression;

		if (observesValue.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
			// look for 704347000 | Observes (attribute) |
			Set<OWLClassExpression> operands3 = ((OWLObjectIntersectionOf) observesValue)
					.getOperands();
			for (OWLClassExpression o3 : operands3) {

				if (o3 != featureOfEntity)
					// add to new flat expression unless it's the evaluation
					// procedure concept
					flatExpressionSet.add(o3);

			}
		}

		
		return manager.getOWLDataFactory().getOWLObjectIntersectionOf(flatExpressionSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.normalform.NormalFormRewriter#isInNormalForm(org
	 * .semanticweb.owlapi.model.OWLClassExpression)
	 */
	@Override
	public boolean isInNormalForm(OWLClassExpression arg0) {
		throw new UnsupportedOperationException(
				"Flattener.isInNormalForm not implemented");
	}

}
