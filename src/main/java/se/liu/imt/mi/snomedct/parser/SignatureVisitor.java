/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.util.HashSet;
import java.util.Set;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ConceptReferenceContext;

/**
 * @author danka74
 *
 */
public class SignatureVisitor extends SNOMEDCTExpressionBaseVisitor<Boolean> {
	
	private Set<String> sctidSet = new HashSet<String>();

	public Set<String> getSctidSet() {
		return sctidSet;
	}

	@Override
	public Boolean visitConceptReference(ConceptReferenceContext ctx) {
		
		String sctid = ctx.getChild(0).getText();
		sctidSet.add(sctid);
		
		return true;
	}

}
