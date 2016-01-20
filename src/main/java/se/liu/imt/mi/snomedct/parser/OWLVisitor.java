/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.semanticweb.elk.util.collections.ArraySet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeGroupContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeSetContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ConceptReferenceContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ExpressionContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.FocusConceptContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.NonGroupedAttributeSetContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.RefinementContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.StatementContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.StatementsContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.SubExpressionContext;

/**
 * @author Daniel Karlsson, Linköping University, daniel.karlsson@liu.se
 *
 */
public class OWLVisitor extends SNOMEDCTExpressionBaseVisitor<OWLObject> {

	private static final String PC_IRI = "http://snomed.org/postcoord/";
	private static final String SCTID_IRI = "http://snomed.info/id/";
	private static final String ROLEGROUP_IRI = "http://snomed.info/id/609096000";

	private static final String[] neverGrouped = { SCTID_IRI + "123005000",
			SCTID_IRI + "272741003", SCTID_IRI + "127489000",
			SCTID_IRI + "704347000", SCTID_IRI + "704318007",
			SCTID_IRI + "704319004", SCTID_IRI + "123454321",
			SCTID_IRI + "704327008", SCTID_IRI + "370132008",
			SCTID_IRI + "246501002", SCTID_IRI + "411116001",
			SCTID_IRI + "704346009" };

	static Logger logger = Logger.getLogger(OWLVisitor.class);

	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLDataFactory dataFactory;
	private OWLClass definiendum;
	private Map<IRI, OWLAnnotation> labels;
	private boolean defaultToPrimitive;

	public OWLVisitor(OWLOntology ontology) {
		this(ontology, null);
	}

	public OWLVisitor(OWLOntology ontology, OWLClass c) {
		this(ontology, c, false);
	}

	public OWLVisitor(OWLOntology ontology, OWLClass c,
			boolean defaultToPrimitive) {
		super();
		this.ontology = ontology;
		this.manager = ontology.getOWLOntologyManager();
		this.dataFactory = manager.getOWLDataFactory();
		this.definiendum = c;
		this.labels = new HashMap<IRI, OWLAnnotation>();
		this.defaultToPrimitive = defaultToPrimitive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitStatement
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.
	 * StatementContext)
	 */
	@Override
	public OWLObject visitStatement(StatementContext ctx) {
		// ctx.subExpression().size() == 2
		// ctx.definitionStatus() != null

		logger.info("visitStatement: " + ctx.getText());

		OWLObject subExpression1 = visit(ctx.subExpression(0));
		OWLObject subExpression2 = visit(ctx.subExpression(1));
		OWLAxiom axiom = null;

		if ((ctx.definitionStatus() == null && defaultToPrimitive == false)
				|| ctx.definitionStatus().start.getType() == SNOMEDCTExpressionLexer.EQ_TO) {
			logger.info("equivalentTo");
			axiom = dataFactory.getOWLEquivalentClassesAxiom(
					(OWLClassExpression) subExpression1,
					(OWLClassExpression) subExpression2);
		} else if ((ctx.definitionStatus() == null && defaultToPrimitive == true)
				|| ctx.definitionStatus().start.getType() == SNOMEDCTExpressionLexer.SC_OF) {
			logger.info("subClassOf");
			axiom = dataFactory.getOWLSubClassOfAxiom(
					(OWLClassExpression) subExpression1,
					(OWLClassExpression) subExpression2);
		}

		return axiom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitExpression
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .ExpressionContext)
	 */
	@Override
	public OWLObject visitExpression(ExpressionContext ctx) {
		logger.info("visitExpression: " + ctx.getText());
		OWLObject subExpression = visit(ctx.subExpression());
		OWLAxiom axiom = null;

		if (definiendum == null)
			definiendum = dataFactory.getOWLClass(IRI.create(PC_IRI
					+ UUID.randomUUID().toString()));

		if ((ctx.definitionStatus() == null && defaultToPrimitive == false)
				|| ctx.definitionStatus().start.getType() == SNOMEDCTExpressionLexer.EQ_TO) {
			logger.info("equivalentTo");
			axiom = dataFactory.getOWLEquivalentClassesAxiom(definiendum,
					(OWLClassExpression) subExpression);
		} else if ((ctx.definitionStatus() == null && defaultToPrimitive == true)
				|| ctx.definitionStatus().start.getType() == SNOMEDCTExpressionLexer.SC_OF) {
			logger.info("subClassOf");
			axiom = dataFactory.getOWLSubClassOfAxiom(definiendum,
					(OWLClassExpression) subExpression);
		}
		return axiom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitRefinement
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .RefinementContext)
	 */
	@Override
	public OWLObject visitRefinement(RefinementContext ctx) {
		logger.info("visitRefinement: " + ctx.getText());

		OWLClassExpression expression = null;

		Set<OWLClassExpression> expressionSet = new HashSet<OWLClassExpression>();

		if (ctx.nonGroupedAttributeSet() != null) {
			expressionSet
					.add((OWLClassExpression) visitNonGroupedAttributeSet(ctx
							.nonGroupedAttributeSet()));
		}

		if (ctx.attributeGroup() != null) {
			for (SNOMEDCTExpressionParser.AttributeGroupContext attrGroupCtx : ctx
					.attributeGroup()) {
				expressionSet
						.add((OWLClassExpression) visitAttributeGroup(attrGroupCtx));
			}
		}

		if (expressionSet.size() > 1)
			expression = dataFactory.getOWLObjectIntersectionOf(expressionSet);
		else
			expression = expressionSet.iterator().next();

		return expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitSubExpression
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .SubExpressionContext)
	 */
	@Override
	public OWLObject visitSubExpression(SubExpressionContext ctx) {
		logger.info("visitSubExpression: " + ctx.getText());
		OWLClassExpression expression = null;

		if (ctx.getChildCount() > 1)
			expression = dataFactory.getOWLObjectIntersectionOf(
					(OWLClassExpression) visitFocusConcept(ctx.focusConcept()),
					(OWLClassExpression) visitRefinement(ctx.refinement()));
		else
			expression = (OWLClassExpression) visitFocusConcept(ctx
					.focusConcept());

		return expression;
	}

	/**
	 * @return the labels
	 */
	public Map<IRI, OWLAnnotation> getLabels() {
		return labels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitFocusConcept
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .FocusConceptContext)
	 */
	@Override
	public OWLObject visitFocusConcept(FocusConceptContext ctx) {
		logger.info("visitFocusConcept: " + ctx.getText());
		OWLClassExpression expression = null;

		if (ctx.getChildCount() > 1) {
			Set<OWLClassExpression> expressionSet = new HashSet<OWLClassExpression>();
			for (SNOMEDCTExpressionParser.ConceptReferenceContext focusConceptCtx : ctx
					.getRuleContexts(SNOMEDCTExpressionParser.ConceptReferenceContext.class))
				expressionSet
						.add((OWLClassExpression) visitConceptReference(focusConceptCtx));
			expression = dataFactory.getOWLObjectIntersectionOf(expressionSet);
		} else
			expression = (OWLClassExpression) visitConceptReference(ctx
					.conceptReference(0));
		return expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitConceptReference
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .ConceptReferenceContext)
	 */
	@Override
	public OWLObject visitConceptReference(ConceptReferenceContext ctx) {
		logger.info("visitConceptReference: " + ctx.getText());

		OWLObject entity = null;

		// concept reference may be to a object or data property or a class
		// depending on
		// the parent parse rule context
		IRI iri = IRI.create(SCTID_IRI + ctx.SCTID().getText());
		if (ctx.getParent().getClass() == SNOMEDCTExpressionParser.AttributeContext.class)
			// return the IRI as we do not know here if it is an object or data
			// property
			entity = iri;
		else if (ctx.getParent().getClass() == SNOMEDCTExpressionParser.FocusConceptContext.class
				|| ctx.getParent().getClass() == SNOMEDCTExpressionParser.AttributeValueContext.class)
			entity = dataFactory.getOWLClass(iri);

		if (ctx.TERM() != null) {
			labels.put(iri, dataFactory.getOWLAnnotation(dataFactory
					.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL
							.getIRI()), dataFactory
					.getOWLLiteral(removeCharacter(ctx.TERM().getText(), "|"))));
		}

		return entity;
	}

	// TODO: let the grammar handle vertical bars instead! Only semi-possible?
	private String removeCharacter(String input, String c) {
		if (input.startsWith(c))
			input = input.substring(1);
		if (input.endsWith(c))
			input = input.substring(0, input.length() - 1);
		return input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitAttribute
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.
	 * AttributeContext)
	 */
	@Override
	public OWLObject visitAttribute(
			se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeContext ctx) {
		logger.info("visitAttribute: " + ctx.getText());
		OWLClassExpression expression = null;

		// if property filler is a concept reference
		if (ctx.attributeValue().getChild(0).getClass() == SNOMEDCTExpressionParser.ConceptReferenceContext.class) {
			OWLObjectProperty property = dataFactory
					.getOWLObjectProperty((IRI) visitConceptReference(ctx
							.conceptReference()));
			OWLClassExpression filler = (OWLClassExpression) visitAttributeValue(ctx
					.attributeValue());
			expression = dataFactory.getOWLObjectSomeValuesFrom(property,
					filler);
		} else // if the property filler is a number
		if (ctx.attributeValue().start.getType() == SNOMEDCTExpressionLexer.NUMBER) {
			String stringValue = ctx.attributeValue().getText();
			OWLLiteral literal;
			if (stringValue.indexOf('.') > 0) {
				double value = Double.parseDouble(stringValue.substring(1));

				literal = dataFactory.getOWLLiteral(value);
			} else {
				int value = Integer.parseInt(stringValue.substring(1));
				literal = dataFactory.getOWLLiteral(value);
			}
			OWLDataProperty property = dataFactory
					.getOWLDataProperty((IRI) visitConceptReference(ctx
							.conceptReference()));
			expression = dataFactory.getOWLDataHasValue(property, literal);
		} else // if the property filler is a string
		if (ctx.attributeValue().start.getType() == SNOMEDCTExpressionLexer.STRING) {
			String stringValue = StringEscapeUtils
					.unescapeJava(removeCharacter(ctx.attributeValue()
							.getText(), "\""));
			OWLDataProperty property = dataFactory
					.getOWLDataProperty((IRI) visitConceptReference(ctx
							.conceptReference()));
			OWLLiteral literal = dataFactory.getOWLLiteral(stringValue);
			expression = dataFactory.getOWLDataHasValue(property, literal);
		} else // a subexpression
		if (ctx.attributeValue().getChild(0).getClass() == SNOMEDCTExpressionParser.NestedExpressionContext.class) {
			OWLObjectProperty property = dataFactory
					.getOWLObjectProperty((IRI) visitConceptReference(ctx
							.conceptReference()));
			OWLClassExpression filler = (OWLClassExpression) visitSubExpression(ctx
					.attributeValue().nestedExpression().subExpression());
			expression = dataFactory.getOWLObjectSomeValuesFrom(property,
					filler);
		} else
			logger.info("Shouldn't ever get here");

		return expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitAttributeGroup
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .AttributeGroupContext)
	 */
	@Override
	public OWLObject visitAttributeGroup(AttributeGroupContext ctx) {
		logger.info("visitAttributeGroup: " + ctx.getText());
		OWLClassExpression expression = null;

		OWLClassExpression attrSet = (OWLClassExpression) visitAttributeSet(ctx
				.attributeSet());
		OWLObjectProperty attrGroup = dataFactory.getOWLObjectProperty(IRI
				.create(ROLEGROUP_IRI));

		expression = dataFactory.getOWLObjectSomeValuesFrom(attrGroup, attrSet);

		return expression;
	}

	/*
	 * This method does not insert attribute groups for non-grouped attributes!
	 * TODO: remove magic transforms from Compositional Grammar specification
	 * 
	 * (non-Javadoc)
	 * 
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitAttributeSet
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .AttributeSetContext)
	 */
	@Override
	public OWLObject visitAttributeSet(AttributeSetContext ctx) {
		OWLClassExpression expression = null;

		if (ctx.getChildCount() > 1) {
			Set<OWLClassExpression> expressionSet = new HashSet<OWLClassExpression>();
			for (SNOMEDCTExpressionParser.AttributeContext attrCtx : ctx
					.getRuleContexts(SNOMEDCTExpressionParser.AttributeContext.class))
				expressionSet.add((OWLClassExpression) visitAttribute(attrCtx));
			expression = dataFactory.getOWLObjectIntersectionOf(expressionSet);
		} else
			expression = (OWLClassExpression) visitAttribute(ctx.attribute(0));
		return expression;
	}

	@Override
	public OWLObject visitNonGroupedAttributeSet(
			NonGroupedAttributeSetContext ctx) {
		OWLClassExpression expression = null;

		if (ctx.getChildCount() > 1) {
			Set<OWLClassExpression> groupedExpressionSet = new HashSet<OWLClassExpression>();
			Set<OWLClassExpression> shouldNotBeGroupedExpressionSet = new HashSet<OWLClassExpression>();

			for (SNOMEDCTExpressionParser.AttributeContext attrCtx : ctx
					.getRuleContexts(SNOMEDCTExpressionParser.AttributeContext.class)) {
				OWLClassExpression attr = (OWLClassExpression) visitAttribute(attrCtx);
				if (attr.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
					if (isNeverGrouped(((OWLObjectSomeValuesFrom) attr)
							.getProperty()))
						shouldNotBeGroupedExpressionSet.add(attr);
					else
						groupedExpressionSet.add(attr);

				} else
					groupedExpressionSet.add(attr);
			}
			if (!shouldNotBeGroupedExpressionSet.isEmpty())
				expression = dataFactory
						.getOWLObjectIntersectionOf(shouldNotBeGroupedExpressionSet);
			if (!groupedExpressionSet.isEmpty()) {
				OWLObjectProperty attrGroup = dataFactory
						.getOWLObjectProperty(IRI.create(ROLEGROUP_IRI));
				OWLClassExpression intersect = dataFactory
						.getOWLObjectIntersectionOf(groupedExpressionSet);
				OWLClassExpression groupExpression = dataFactory
						.getOWLObjectSomeValuesFrom(attrGroup, intersect);
				if (!shouldNotBeGroupedExpressionSet.isEmpty()) {
					shouldNotBeGroupedExpressionSet.add(groupExpression);
					expression = dataFactory
							.getOWLObjectIntersectionOf(shouldNotBeGroupedExpressionSet);
				} else
					expression = groupExpression;
			}
		} else {
			OWLClassExpression attr = (OWLClassExpression) visitAttribute((AttributeContext) ctx
					.getChild(0));
			if (attr.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM
					&& isNeverGrouped(((OWLObjectSomeValuesFrom) attr)
							.getProperty()))
				expression = attr;
			else {
				OWLObjectProperty attrGroup = dataFactory
						.getOWLObjectProperty(IRI.create(ROLEGROUP_IRI));
				expression = dataFactory.getOWLObjectSomeValuesFrom(attrGroup,
						attr);
			}
		}
		return expression;
	}

	private boolean isNeverGrouped(OWLObjectPropertyExpression property) {
		IRI iri = property.getNamedProperty().getIRI();
		String iriString = iri.toString();
		for (String nonGroupedIri : neverGrouped) {
			if (iriString.equals(nonGroupedIri))
				return true;
		}
		return false;
	}

	@Override
	public OWLObject visitStatements(StatementsContext ctx) {

		ArraySet<OWLAxiom> axioms = new ArraySet<OWLAxiom>();
		OWLAxiom owlAxiom = null;
		for (StatementContext statement : ctx
				.getRuleContexts(SNOMEDCTExpressionParser.StatementContext.class)) {
			owlAxiom = (OWLClassAxiom) visitStatement(statement);
			axioms.add(owlAxiom);

		}
		manager.addAxioms(ontology, axioms);

		// labels for expression parts are kept in a map
		Map<IRI, OWLAnnotation> annotations = this.getLabels();
		// add labels, if any
		for (Entry<IRI, OWLAnnotation> label : annotations.entrySet()) {
			if (dataFactory.getOWLClass(label.getKey())
					.getAnnotations(ontology).isEmpty())
				manager.addAxiom(
						ontology,
						dataFactory.getOWLAnnotationAssertionAxiom(
								label.getKey(), label.getValue()));
		}

		return owlAxiom;
	}

}
