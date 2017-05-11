/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.util.HashSet;
import java.util.Set;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ConceptReferenceContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.StatementContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.SubExpressionContext;

/**
 * @author danka74
 *
 */
public class SignatureVisitor extends SNOMEDCTExpressionBaseVisitor<Boolean> {
	
	private Set<String> sctidSet = new HashSet<String>();

	public Set<String> getSctidSet() {
		return sctidSet;
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#visitStatement(se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.StatementContext)
	 */
	@Override
	public Boolean visitStatement(StatementContext ctx) {
		SubExpressionContext sec = ctx.getChild(SubExpressionContext.class, 1);
		if(sec != null)
			return visitSubExpression(sec);
		return true;
	}

	@Override
	public Boolean visitConceptReference(ConceptReferenceContext ctx) {
		
		String sctid = ctx.getChild(0).getText();
		sctidSet.add(sctid);
		
		return true;
	}

}
