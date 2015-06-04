/**
 * 
 */
package test;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTQueryExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTQueryExpressionParser;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;

/**
 * @author daniel
 *
 */
public class TestSNOMEDCTQueryExpressionParser {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {

		String[] testCases = new String[] { 
				"<12345|hej| OR <<12345|tjo|" };

		for (String testCase : testCases) {

			// parse string and throw ExpressionSyntaxError if unparsable
			ANTLRInputStream input = new ANTLRInputStream(testCase);
			SNOMEDCTQueryExpressionLexer lexer = new SNOMEDCTQueryExpressionLexer(input);
			
			Token token;
		    while (true) {
		        token = lexer.nextToken();
		        if (token.getType() == Token.EOF) {
		            break;
		        }

		        System.out.println("Token: ‘" + token.getText() + "’ " + SNOMEDCTQueryExpressionLexer.ruleNames[token.getType()]);
		    }
		    
		    lexer.reset();
		    
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			SNOMEDCTQueryExpressionParser parser = new SNOMEDCTQueryExpressionParser(
					tokens);
			parser.setErrorHandler(new BailErrorStrategy());
			
			ParseTree tree = parser.query();
			
			assertNotNull(tree);
		}
	}

}
