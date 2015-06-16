package se.liu.imt.mi.snomedct.parser;

import java.util.Iterator;
import java.util.TreeSet;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeGroupContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeSetContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeValueContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ConceptReferenceContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ExpressionContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.FocusConceptContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.NestedExpressionContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.NonGroupedAttributeSetContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.RefinementContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.StatementContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.SubExpressionContext;

public class SortedExpressionVisitor extends
		SNOMEDCTExpressionBaseVisitor<String> {

	@Override
	public String visitAttributeSet(AttributeSetContext ctx) {
		if(ctx.getChildCount() > 1) {
			TreeSet<String> attributeSet = new TreeSet<String>();
			for(AttributeContext attr : ctx.attribute()) {
				attributeSet.add(visitAttribute(attr));
			}
			StringBuilder result = new StringBuilder();
			for (Iterator<String> i = attributeSet.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append(',');
			}
			return result.toString();
		}
		else
			return visitAttribute(ctx.attribute(0));
	}
	
	@Override
	public String visitNonGroupedAttributeSet(NonGroupedAttributeSetContext ctx) {
		if(ctx.getChildCount() > 1) {
			TreeSet<String> attributeSet = new TreeSet<String>();
			for(AttributeContext attr : ctx.attribute()) {
				attributeSet.add(visitAttribute(attr));
			}
			StringBuilder result = new StringBuilder();
			for (Iterator<String> i = attributeSet.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append(',');
			}
			return result.toString();
		}
		else
			return visitAttribute(ctx.attribute(0));
	}

	@Override
	protected String defaultResult() {
		return "";
	}

	@Override
	protected String aggregateResult(String aggregate, String nextResult) {
		return aggregate + nextResult;
	}

	@Override
	public String visitExpression(ExpressionContext ctx) {
		StringBuilder result = new StringBuilder();
		if(ctx.definitionStatus() == null || ctx.definitionStatus().start.getType() == SNOMEDCTExpressionLexer.EQ_TO)
			result.append("===");
		else
			result.append("<<<");
		
		result.append(visitSubExpression(ctx.subExpression()));
		
		return result.toString();
	}

	@Override
	public String visitStatement(StatementContext ctx) {
		// TODO Auto-generated method stub
		return super.visitStatement(ctx);
	}

	@Override
	public String visitRefinement(RefinementContext ctx) {
		StringBuilder result = new StringBuilder();
		if(ctx.nonGroupedAttributeSet() != null) {
			result.append(visitNonGroupedAttributeSet(ctx.nonGroupedAttributeSet()));
			if(!ctx.attributeGroup().isEmpty())
				result.append(',');
		}
		
		if(!ctx.attributeGroup().isEmpty()) {
			TreeSet<String> attributeGroupSet = new TreeSet<String>();
			for(AttributeGroupContext attrGroup : ctx.attributeGroup()) {
				attributeGroupSet.add(visitAttributeGroup(attrGroup));
			}
			for (Iterator<String> i = attributeGroupSet.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append(',');
			}
		}

		return result.toString();
	}

	@Override
	public String visitNestedExpression(NestedExpressionContext ctx) {
		return "(" + visitSubExpression(ctx.subExpression()) + ")";
	}

	@Override
	public String visitAttributeValue(AttributeValueContext ctx) {
		// TODO Auto-generated method stub
		return super.visitAttributeValue(ctx);
	}

	@Override
	public String visitSubExpression(SubExpressionContext ctx) {
		if(ctx.refinement() != null)
			return visitFocusConcept(ctx.focusConcept()) + ":" + visitRefinement(ctx.refinement());
		else
			return visitFocusConcept(ctx.focusConcept());
	}

	@Override
	public String visitFocusConcept(FocusConceptContext ctx) {
		if(ctx.getChildCount() > 1) {
			TreeSet<Long> conceptIdSet = new TreeSet<Long>();
			for(ConceptReferenceContext crCtx : ctx.conceptReference()) { 
				conceptIdSet.add(Long.valueOf(crCtx.SCTID().getText()));
			}
			StringBuilder result = new StringBuilder();
			for (Iterator<Long> i = conceptIdSet.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append('+');
			}
			return result.toString();
		}
		else
			return ctx.conceptReference(0).SCTID().getText();
	}

	@Override
	public String visitAttribute(AttributeContext ctx) {
		return ctx.conceptReference().SCTID().getText() + "=" + visitAttributeValue(ctx.attributeValue());
	}

	@Override
	public String visitAttributeGroup(AttributeGroupContext ctx) {
		return "{" + visitAttributeSet(ctx.attributeSet()) + "}";
	}

	@Override
	public String visitConceptReference(ConceptReferenceContext ctx) {
		return ctx.SCTID().getText();
	}

}
