/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

import se.liu.imt.mi.snomedct.expression.SCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SCTExpressionParser;

/**
 * Wrapper class for the SNOMED CT ANTLR parser
 * 
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 *
 */
public class SnomedCTParser {

	/**
	 * Wrapper method for parsing an SNOMED CT expression from a string
	 *  
	 * @param expression	string representation of a SNOMED CT expression
	 * @return				parse tree resulting from parsing
	 * @throws ExpressionSyntaxError
	 * 						thrown when syntax error in expression string	
	 */
	public static Tree parseExpression(String expression) throws ExpressionSyntaxError {
		SCTExpressionParser.expression_return parseResult = null;

		// parse string and throw ExpressionSyntaxError if unparsable
		CharStream input = new ANTLRStringStream(expression);
		SCTExpressionLexer lexer = new SCTExpressionLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SCTExpressionParser parser = new SCTExpressionParser(tokens);
		try {
			parseResult = parser.expression();
		} catch (Exception e) {
			throw new ExpressionSyntaxError(e);
		}
		if (parseResult == null)
			throw new ExpressionSyntaxError(
					"Parse result is null. Should not happen ever!");
		
		return (Tree)parseResult.getTree();
	}
	
	/**
	 * Wrapper method for parsing an SNOMED CT expression from a string
	 *  
	 * @param query	string representation of a SNOMED CT query
	 * @return				parse tree resulting from parsing
	 * @throws ExpressionSyntaxError
	 * 						thrown when syntax error in query string	
	 */
	public static Tree parseQuery(String query) throws ExpressionSyntaxError {
		SCTExpressionParser.expressionOrQuery_return parseResult = null;

		// parse string and throw ExpressionSyntaxError if unparsable
		CharStream input = new ANTLRStringStream(query);
		SCTExpressionLexer lexer = new SCTExpressionLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SCTExpressionParser parser = new SCTExpressionParser(tokens);
		try {
			parseResult = parser.expressionOrQuery();
		} catch (Exception e) {
			throw new ExpressionSyntaxError(e);
		}
		if (parseResult == null)
			throw new ExpressionSyntaxError(
					"Parse result is null. Should not happen ever!");
		
		return (Tree)parseResult.getTree();
	}
	
	

}
