/**
 *
 */
package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.liu.imt.mi.snomedct.expression.tools.ExpressionSyntaxError;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;
import se.liu.imt.mi.snomedct.parser.SVGPart;
import se.liu.imt.mi.snomedct.parser.SVGVisitor;

/**
 * @author daniel
 *
 */
public class TestSVGVisitor {

	static Logger logger = Logger.getLogger(TestSVGVisitor.class);

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
	public void test() throws IOException, ExpressionSyntaxError {
		URL testCaseURL = getClass().getResource(
				"/sct_test_cases_statement.txt");
		BufferedReader testCaseReader = new BufferedReader(new FileReader(
				testCaseURL.getFile()));

		String strLine;

		Integer i = 0;

		while ((strLine = testCaseReader.readLine()) != null) {

			if (strLine.startsWith("#"))
				continue;

			i++;

			String[] strTokens = strLine.split("\t");

			logger.info(strTokens[0]);

			ParseTree tree = null;
			if (strTokens[0].startsWith("("))
				tree = SNOMEDCTParserUtil.parseStatement(strTokens[0]);
			else
				tree = SNOMEDCTParserUtil.parseExpression(strTokens[0]);

			SVGVisitor visitor = new SVGVisitor();
			SVGPart result = visitor.visit(tree);

			FileWriter writer = new FileWriter(System.getProperty("user.dir")+"/svg_diagram_" + i + ".svg");
			writer.write(result.getSVG());
			writer.close();

		}

		testCaseReader.close();
	}
}
