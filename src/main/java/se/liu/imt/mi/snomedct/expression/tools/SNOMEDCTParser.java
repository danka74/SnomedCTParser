/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTree;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser;

/**
 * Wrapper class for the SNOMED CT ANTLR parser
 * 
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 *
 */
public class SNOMEDCTParser {

	/**
	 * Wrapper method for parsing an SNOMED CT expression from a string
	 *  
	 * @param expression	string representation of a SNOMED CT expression
	 * @return				parse tree resulting from parsing
	 * @throws ExpressionSyntaxError
	 * 						thrown when syntax error in expression string	
	 */
	public static ParseTree parseExpression(String expression) throws ExpressionSyntaxError {
		
		ParseTree tree = null;
		
		// parse string and throw ExpressionSyntaxError if unparsable
		ANTLRInputStream input = new ANTLRInputStream(expression);
		SNOMEDCTExpressionLexer lexer = new SNOMEDCTExpressionLexer(input);
	    CommonTokenStream tokens = new CommonTokenStream(lexer);
		SNOMEDCTExpressionParser parser = new SNOMEDCTExpressionParser(tokens);
		try {
			tree = parser.expression();
		} catch (Exception e) {
			throw new ExpressionSyntaxError(e);
		}
		if (tree == null)
			throw new ExpressionSyntaxError(
					"Parse result is null. Should not happen ever!");
		
		return tree;
	}

	

}
