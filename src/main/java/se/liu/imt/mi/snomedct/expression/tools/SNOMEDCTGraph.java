/**
 *
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import se.liu.imt.mi.snomedct.parser.SVGPart;
import se.liu.imt.mi.snomedct.parser.SVGVisitor;

/**
 * @author daniel
 *
 */
public class SNOMEDCTGraph {

	static Logger logger = Logger.getLogger(SNOMEDCTGraph.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// create Options object
		Options options = new Options();

		// add SNOMED CT concepts file option
		options.addOption("s", "snomed-file", true, "SNOMED CT concepts file");

		// add output file option
		options.addOption("o", "output-file", true, "output file");

		// add option for setting fully defined as default, otherwise primitive
		// will be default
		options.addOption("f", "fully-defined-default", false,
				"will generate fully defined concepts references as default");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}

		String snomedCTFileName = cmd.getOptionValue("snomed-file");
		String outputFileName = cmd.getOptionValue("output-file");
		boolean fullyDefinedDefault = cmd.hasOption("fully-defined-default");

		List<?> argList = (List<?>) cmd.getArgList();
		if (argList.size() < 1) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp("SNOMEDCTTranslator", options);
			System.exit(2);
		}

		// get input file name from argument list
		String inputFileName = (String) argList.get(0);
		// get output file name from argument list or create a new output file
		// name from the input file name
		if (outputFileName == null) {
			// separate out the extension (after last '.')
			String[] tokens = inputFileName.split("\\.(?=[^\\.]+$)");
			outputFileName = tokens[0] + ".svg";
		}

		// read SNOMED CT concept file if present
		HashMap<Long, Boolean> concepts = loadSnomedCTConcepts(snomedCTFileName);		

		try {
			String inputExpression = new String(Files.readAllBytes(Paths
					.get(inputFileName)));
			ParseTree tree = null;

			logger.info("Parsing input file: " + inputFileName);

			if (inputExpression.startsWith("("))
				tree = SNOMEDCTParserUtil.parseStatement(inputExpression);
			else
				tree = SNOMEDCTParserUtil.parseExpression(inputExpression);

			SVGVisitor visitor = new SVGVisitor(concepts, fullyDefinedDefault);

			SVGPart result = visitor.visit(tree);

			logger.info("Writing resluting graph to: " + outputFileName);

			FileWriter writer = new FileWriter(outputFileName);
			writer.write(result.getSVG());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseCancellationException e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		} catch (ExpressionSyntaxError e) {
			ParseCancellationException pce = (ParseCancellationException) e
					.getCause();
			logger.info(pce.getMessage());
			System.err.println(pce.getMessage());
		}

	}
	
	public static HashMap<Long, Boolean> loadSnomedCTConcepts(String snomedCTFileName) {
		HashMap<Long, Boolean> concepts = new HashMap<Long, Boolean>();
		if (snomedCTFileName != null) {
			logger.info("Reading SNOMED CT concept file...");
			BufferedReader snomedCTFile;
			try {
				snomedCTFile = new BufferedReader(new FileReader(
						snomedCTFileName));
				// skip first line
				snomedCTFile.readLine();
				String strLine;
				while ((strLine = snomedCTFile.readLine()) != null) {
					String tokens[] = strLine.split("\t");
					concepts.put(new Long(tokens[0]),
							tokens[4].equals("900000000000073002"));
				}
			} catch (Exception e) {
				concepts = null;
				// e.printStackTrace();
			}
			logger.info("Finished reading SNOMED CT concept file.");
		}
		
		return concepts;
		
	}

}
