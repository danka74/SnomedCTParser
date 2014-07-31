/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.normalform.NormalFormRewriter;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import test.TestSNOMEDCTOWLParser;

/**
 * @author daniel
 * 
 */
public class DistributionNormalFormConverter implements NormalFormRewriter {

	static Logger logger = Logger
			.getLogger(DistributionNormalFormConverter.class);

	OWLOntology ontology;
	OWLOntologyManager manager;
	OWLReasoner reasoner;
	Set<OWLOntology> imports;

	/**
	 * The class keeps differentia and only allows adding of non-redundant
	 * 
	 * @author daniel
	 * 
	 */
	private class DifferentiaSet {
		Set<OWLObjectSomeValuesFrom> differentia;

		public Set<OWLObjectSomeValuesFrom> getDifferentiaAsSet() {
			return differentia;
		}

		public DifferentiaSet() {
			differentia = new HashSet<OWLObjectSomeValuesFrom>();
		}

		void add(OWLObjectSomeValuesFrom some) {
			for (OWLObjectSomeValuesFrom e : differentia)
				if (isRedundant(some, e))
					return;
			differentia.add(some);
		}

		void addAll(Set<OWLClassExpression> se) throws Exception {
			for (OWLClassExpression e : se) {
				if (e.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
					add((OWLObjectSomeValuesFrom) e);
				else
					throw new Exception("Non-some expression in set!");
			}
		}

		boolean isRedundant(OWLObjectSomeValuesFrom e1,
				OWLObjectSomeValuesFrom e2) {
			if (e1.equals(e2))
				return true;

			if (isPropertySubsumedBy(e1.getProperty(), e2.getProperty())) {
				OWLClassExpression ef1 = e1.getFiller();
				OWLClassExpression ef2 = e2.getFiller();

				return isRedundant(ef1, ef2);

			}

			return false;
		}

		boolean isRedundant(OWLClassExpression e1, OWLClassExpression e2) {
			if (e1.getClassExpressionType() == ClassExpressionType.OWL_CLASS
					&& e2.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
				return isClassSubsumbedBy((OWLClass) e1, (OWLClass) e2);
			if (e1.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM
					&& e2.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
				return isRedundant((OWLObjectSomeValuesFrom) e1,
						(OWLObjectSomeValuesFrom) e2);
			if (e1.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM
					&& e2.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
				Set<OWLClassExpression> operands2 = ((OWLObjectIntersectionOf) e2)
						.getOperands();
				for (OWLClassExpression ei2 : operands2) {
					if (isRedundant(e1, ei2)) 
						return true;
				}
				return false;
			}
			if (e1.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF
					&& e2.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
				Set<OWLClassExpression> operands1 = ((OWLObjectIntersectionOf) e1)
						.getOperands();
				Set<OWLClassExpression> operands2 = ((OWLObjectIntersectionOf) e2)
						.getOperands();
				for (OWLClassExpression ei1 : operands1) {
					boolean ei1Redundant = false;
					for (OWLClassExpression ei2 : operands2) {
						if (isRedundant(ei1, ei2)) {
							ei1Redundant = true;
							break;
						}
					}
					if (ei1Redundant == false)
						return false;

				}
				return true;
			}

			return false;

		}

		boolean isClassSubsumbedBy(OWLClass c1, OWLClass c2) {
			if (c1.equals(c2) || reasoner.getEquivalentClasses(c1).contains(c2)
					|| reasoner.getSuperClasses(c2, false).containsEntity(c1))
				return true;

			return false;
		}

		boolean isPropertySubsumedBy(OWLObjectPropertyExpression p1,
				OWLObjectPropertyExpression p2) {
			if (p1.equals(p2) || p1.getEquivalentProperties(imports).contains(p2)
					|| p1.getSuperProperties(imports).contains(p2))
				return true;
			return false;
		}
	}

	public DistributionNormalFormConverter(OWLOntology ontology,
			OWLReasoner reasoner) {
		this.ontology = ontology;
		manager = ontology.getOWLOntologyManager();
		this.reasoner = reasoner;
		imports = manager.getImports(ontology);
		imports.add(this.ontology);
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
			OWLClassExpression inputExpression) {

		logger.info("input expression = " + inputExpression.toString());

		// is this correct? or should all attributes be collected as well?
		if (inputExpression.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
			return inputExpression;

		Set<OWLClassExpression> directSupers = new HashSet<OWLClassExpression>();
		directSupers.addAll(reasoner.getSuperClasses(inputExpression, true)
				.getFlattened());

		logger.info("direct supers = " + directSupers.toString());

		DifferentiaSet differentia = new DifferentiaSet();
		// it is (given current SNOMED CT) either a single class or an
		// intersection of class(es) and existential restrictions
		if (inputExpression.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
			for (OWLClassExpression e : ((OWLObjectIntersectionOf) inputExpression)
					.getOperands()) {
				// no need to consider other than existential restrictions
				// (differentia) as genera are already in set of direct supers
				if (e.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
					logger.info("diff = " + e.toString());
					differentia.add((OWLObjectSomeValuesFrom) e);
				}
			}

			try {
				differentia.addAll(collectDifferentia(directSupers));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		directSupers.addAll(differentia.getDifferentiaAsSet());
		OWLObjectIntersectionOf normalForm = manager.getOWLDataFactory()
				.getOWLObjectIntersectionOf(directSupers);

		return normalForm;
	}

	private Set<OWLClassExpression> collectDifferentia(
			Set<OWLClassExpression> classes) {
		return collectDifferentia(classes, "");
	}

	private Set<OWLClassExpression> collectDifferentia(
			Set<OWLClassExpression> classes, String indent) {
		logger.info(indent + "classes = " + classes.toString());
		Set<OWLClassExpression> differentia = new HashSet<OWLClassExpression>();
		for (OWLClassExpression e : classes) {
			logger.info(indent + "class = " + e.toString());
			switch (e.getClassExpressionType()) {
			case OWL_CLASS:
				Set<OWLClassExpression> supers = ((OWLClass) e)
						.getSuperClasses(imports);
				logger.info(indent + "collect, supers = " + supers.toString());
				if (supers.size() > 0)
					differentia
							.addAll(collectDifferentia(supers, indent + "  "));
				Set<OWLClassExpression> eqs = ((OWLClass) e)
						.getEquivalentClasses(imports);
				logger.info(indent + "collect, eqs = " + eqs.toString());
				if (eqs.size() > 0)
					differentia.addAll(collectDifferentia(eqs, indent + "  "));
				break;
			case OBJECT_INTERSECTION_OF:
				Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) e)
						.getOperands();
				logger.info(indent + "collect, intersect ops = "
						+ operands.toString());
				differentia.addAll(collectDifferentia(operands, indent + "  "));
				break;
			case OBJECT_SOME_VALUES_FROM:
				logger.info(indent + "collect, diff = " + e.toString());
				differentia.add(e);
				break;
			default:
				break;
			}
		}
		return differentia;
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
		// TODO Auto-generated method stub
		return false;
	}

}
