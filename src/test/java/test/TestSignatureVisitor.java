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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTQueryExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTQueryExpressionParser;
import se.liu.imt.mi.snomedct.parser.SignatureVisitor;

/**
 * @author danka74
 *
 */
public class TestSignatureVisitor {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

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
		String[] testCases = new String[] { "12345|hej|:12345|tjo|=3456|hopp|", "12345|hej|:12345|tjo|=3456|hopp|"  };

		for (String testCase : testCases) {

			// parse string and throw ExpressionSyntaxError if unparsable
			ANTLRInputStream input = new ANTLRInputStream(testCase);
			SNOMEDCTExpressionLexer lexer = new SNOMEDCTExpressionLexer(
					input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			SNOMEDCTExpressionParser parser = new SNOMEDCTExpressionParser(
					tokens);
			parser.setErrorHandler(new BailErrorStrategy());

			ParseTree tree = parser.expression();
			
			SignatureVisitor visitor = new SignatureVisitor();
			visitor.visit(tree);
			
			

			assert(visitor.getSctidSet().size() == 2);
		}
		
		
	}


}
