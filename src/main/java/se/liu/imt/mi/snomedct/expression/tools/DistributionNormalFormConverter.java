/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.normalform.NormalFormRewriter;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Class for implementing a SNOMED CT Distribution Normal Form converter. Some
 * assumptions are made: 1. Only EL++ is used (i.e. only existential
 * restrictions, intersections (conjunctions) etc.) 2. Restrictions are added
 * from bottom up, i.e. the most specific restrictions are added first
 * 
 * References: SNOMED CT Technical Implementation Guide
 * www.snomed.org/tig?t=nfg_normalForm_definition_eg_fullyDefAttrValue
 * www.snomed
 * .org/tig?t=amg2_definition_altModelView_altInferred_attribute_nonRedundant
 * 
 * SPACKMAN, Kent A. Normal forms for description logic expressions of clinical
 * concepts in SNOMED RT. In: Proceedings of the AMIA Symposium. American
 * Medical Informatics Association, 2001. p. 627.
 * 
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 * 
 */
public class DistributionNormalFormConverter implements NormalFormRewriter {

//	static Logger logger = Logger
//			.getLogger(DistributionNormalFormConverter.class);

	OWLOntology ontology;
	OWLOntologyManager manager;
	OWLReasoner reasoner;
	Set<OWLOntology> imports;

	/**
	 * The class keeps differentia (existential restrictions) and only allows
	 * adding of non-redundant restrictions
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

		/**
		 * Adds an restriction to the set of differentia only if it is
		 * non-redundant in relation to current members of the set
		 * 
		 * @param existRestriction
		 *            The restriction to add
		 */
		void add(OWLObjectSomeValuesFrom existRestriction) {
			// add a restriction only if it's not redundant in relation to any
			// member of the current differentia set
			for (OWLObjectSomeValuesFrom diff : differentia)
				if (isRedundant(existRestriction, diff))
					return;
			differentia.add(existRestriction);
		}

		/**
		 * Adds a set of restrictions to the set of differentia
		 * 
		 * @param expression
		 *            Set the set of restrictions to add
		 * @throws Exception
		 *             Thrown if the set contains anything but existential
		 *             restrictions
		 */
		void addAll(Set<OWLClassExpression> expressionSet) throws Exception {
			for (OWLClassExpression expr : expressionSet) {
				// there should only be existential restrictions in the
				// differentia set
				if (expr.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
					add((OWLObjectSomeValuesFrom) expr);
				else
					throw new Exception(
							"Only existential restrictions allowed in the set!");
			}
		}

		/**
		 * Checks if restriction 1 is redundant in relation to restriction 2.
		 * This is a non-symmetrical function!
		 * 
		 * @param restrict1
		 *            Restriction 1
		 * @param restrict2
		 *            Restriction 2
		 * @return Returns true iff restriction 1 is redundant in relation to
		 *         restriction 2.
		 */
		boolean isRedundant(OWLObjectSomeValuesFrom restrict1,
				OWLObjectSomeValuesFrom restrict2) {
			// an existential restriction is redundant in relation to another if
			// the two restrictions are equal
			if (restrict1.equals(restrict2))
				return true;

			// if the former object property is subsumed by the latter, the
			// fillers are compared for redundancy
			if (isPropertySubsumedBy(restrict1.getProperty(),
					restrict2.getProperty())) {
				OWLClassExpression filler1 = restrict1.getFiller();
				OWLClassExpression filler2 = restrict2.getFiller();

				return isRedundant(filler1, filler2);

			}

			// if the restrictions are not equal and the object property of
			// restriction 1 does is not subsumed by the object property of
			// restriction 2, then return false
			return false;
		}

		/**
		 * Checks if expression 1 is redundant in relation to expression 2. This
		 * is a non-symmetrical function!
		 * 
		 * @param expr1
		 *            Expression 1
		 * @param expr2
		 *            Expression 2
		 * @return Returns true iff expression 1 is redundant in relation to
		 *         expression 2.
		 */
		boolean isRedundant(OWLClassExpression expr1, OWLClassExpression expr2) {
			// there are four cases:
			// 1. both expressions are classes, then class 1 is redundant if it
			// is subsumed by class 2
			if (expr1.getClassExpressionType() == ClassExpressionType.OWL_CLASS
					&& expr2.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
				return isClassSubsumedBy((OWLClass) expr1, (OWLClass) expr2);
			// 2. both expressions are existential restrictions, then call the
			// above function
			if (expr1.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM
					&& expr2.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
				return isRedundant((OWLObjectSomeValuesFrom) expr1,
						(OWLObjectSomeValuesFrom) expr2);
			// 3. if experssion 1 is a existential restriction and expression 2
			// is an intersection, then the restriction is redundant if it is
			// redundant to at least one of the expressions of the intersection
			if (expr1.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM
					&& expr2.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
				Set<OWLClassExpression> operands2 = ((OWLObjectIntersectionOf) expr2)
						.getOperands();
				for (OWLClassExpression exprIndex2 : operands2) {
					if (isRedundant(expr1, exprIndex2))
						return true;
				}
				return false;
			}
			// 4. if both expressions are intersections, then intersection 1 is
			// redundant if each expression of intersection 1 is redundant to
			// some expression in intersection 2
			if (expr1.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF
					&& expr2.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
				Set<OWLClassExpression> operands1 = ((OWLObjectIntersectionOf) expr1)
						.getOperands();
				Set<OWLClassExpression> operands2 = ((OWLObjectIntersectionOf) expr2)
						.getOperands();
				for (OWLClassExpression subExpr1 : operands1) {
					boolean subExpr1Redundant = false;
					for (OWLClassExpression subExpr2 : operands2) {
						if (isRedundant(subExpr1, subExpr2)) {
							subExpr1Redundant = true;
							break;
						}
					}
					if (subExpr1Redundant == false)
						return false;

				}
				return true;
			}

			return false;

		}

		/**
		 * Checks if class 1 is subsumed by (or is equivalent to) class 2
		 * 
		 * @param class1
		 *            Class 1
		 * @param class2
		 *            Class 2
		 * @return Returns true iff class 1 is subsumed by (or is equivalent to)
		 *         class 2
		 */
		boolean isClassSubsumedBy(OWLClass class1, OWLClass class2) {
			if (class1.equals(class2)
					|| reasoner.getEquivalentClasses(class1).contains(class2)
					|| reasoner.getSuperClasses(class2, false).containsEntity(
							class1))
				return true;

			return false;
		}

		/**
		 * Checks if object property 1 is subsumed by (or is equivalent to)
		 * object property 2
		 * 
		 * @param prop1
		 *            object property 1
		 * @param prop2
		 *            object property 2
		 * @return Returns true iff object property 1 is subsumed by (or is
		 *         equivalent to) object property 2
		 */
		boolean isPropertySubsumedBy(OWLObjectPropertyExpression prop1,
				OWLObjectPropertyExpression prop2) {
			if (prop1.equals(prop2)
					|| prop1.getEquivalentProperties(imports).contains(prop2)
					|| prop2.getSuperProperties(imports).contains(prop1))
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

//		logger.info("input expression = " + inputExpression.toString());

		// TODO is this correct? or should all attributes be collected as well?
		if (inputExpression.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
			return inputExpression;

		Set<OWLClassExpression> directSupers = new HashSet<OWLClassExpression>();
		directSupers.addAll(reasoner.getSuperClasses(inputExpression, true)
				.getFlattened());

//		logger.info("direct supers = " + directSupers.toString());

		DifferentiaSet differentia = new DifferentiaSet();

		// it is (given EL++) either a single class or an
		// intersection of class(es) and existential restrictions
		if (inputExpression.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
			for (OWLClassExpression e : ((OWLObjectIntersectionOf) inputExpression)
					.getOperands()) {
				// no need to consider other than existential restrictions
				// (differentia) as genera are already in set of direct supers
				if (e.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
//					logger.info("diff = " + e.toString());
					// add each restriction to the initial set of differentia
					differentia.add((OWLObjectSomeValuesFrom) e);
				}
			}

			try {
				// collect additional restrictions from direct supers
				// recursively and add to the set of non-redundant differentia
				differentia.addAll(collectDifferentia(directSupers));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		// add the restrictions in the differentia set to the set of direct
		// supers
		directSupers.addAll(differentia.getDifferentiaAsSet());
		// create a new OWL intersection of the direct supers and the
		// differentia restrictions
		OWLObjectIntersectionOf normalForm = manager.getOWLDataFactory()
				.getOWLObjectIntersectionOf(directSupers);

		return normalForm;
	}

	/**
	 * Collects differentia (here, existential restrictions) from a set of class
	 * expressions recursively upwards subsumption hierarchy TODO check if the
	 * search tree can be pruned
	 * 
	 * @param exprSet
	 *            The set of classes
	 * @return Returns a set of (possibly/probably redundant)
	 *         differentia/restrictions
	 */
	private Set<OWLClassExpression> collectDifferentia(
			Set<OWLClassExpression> exprSet) {
		Set<OWLClassExpression> differentia = new HashSet<OWLClassExpression>();
		// iterate through all input expressions
		for (OWLClassExpression e : exprSet) {
			// different cases for different expression types
			switch (e.getClassExpressionType()) {
			// if the expression is a class, then collect differentia of direct
			// supers (for primitives) and equivalent classes (for fully
			// defined). The OWLAPI methods OWLClass.getSuperClasses and
			// OWLClass.getEquivalentClasses return the asserted (on not
			// inferred) class expressions. The OWLReasoner versions only return
			// OWLClasses and not any other OWLClassExpressions. TODO confirm
			// that this gives the intended result, or that there is no better
			// way of achieving the intended result.
			case OWL_CLASS:
				Set<OWLClassExpression> supers = ((OWLClass) e)
						.getSuperClasses(imports);
				if (supers.size() > 0)
					differentia
							.addAll(collectDifferentia(supers));
				Set<OWLClassExpression> eqs = ((OWLClass) e)
						.getEquivalentClasses(imports);
				if (eqs.size() > 0)
					differentia.addAll(collectDifferentia(eqs));
				break;
			// if the expression is an intersection, then collect differentia
			// for all subexpressions of the intersection
			case OBJECT_INTERSECTION_OF:
				Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) e)
						.getOperands();
				differentia.addAll(collectDifferentia(operands));
				break;
			// if the expression is an existential restriction, then add the
			// restriction to the set of differentia
			case OBJECT_SOME_VALUES_FROM:
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
		throw new UnsupportedOperationException("DistributionNormalFormConverter.isInNormalForm not implemented");
	}

}
