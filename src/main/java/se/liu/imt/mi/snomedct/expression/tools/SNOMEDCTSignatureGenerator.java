/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLException;

import se.liu.imt.mi.snomedct.parser.SignatureVisitor;

/**
 * @author Daniel Karlsson, Linköping Univsrsity, daniel.karlsson@liu.se
 * @author Kent Spackman, IHTSDO, ksp@ihtsdo.org
 * 
 * 
 */
public class SNOMEDCTSignatureGenerator {

	static Logger logger = Logger.getLogger(SNOMEDCTSignatureGenerator.class);

	/**
	 * @param args
	 * @throws OWLException
	 */
	public static void main(String[] args)  {
		// create Options object
		Options options = new Options();

		options.addOption("f", "format", true, "format of signature file, '|' or 'url'");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		String format = cmd.getOptionValue("format", "url");

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
		String outputFileName;
		if (argList.size() < 2) {
			// separate out the extension (after last '.')
			String[] tokens = inputFileName.split("\\.(?=[^\\.]+$)");
			if (tokens.length >= 2)
				outputFileName = tokens[0] + "_signature.txt";
			else
				outputFileName = inputFileName + "_signature.txt";
		} else
			outputFileName = (String) argList.get(1);

		try {
			InputStream is = new FileInputStream(inputFileName);
			ParseTree tree = SNOMEDCTParserUtil.parseFile(is);
			SignatureVisitor visitor = new SignatureVisitor();
			visitor.visit(tree);
			
			FileWriter fw = new FileWriter(outputFileName);
			for(String sctid : visitor.getSctidSet()) {
				if(format.equalsIgnoreCase("url"))
					fw.write("http://snomed.org/id/" + sctid + '\n');
				else if(format.equals("|"))
					fw.write(sctid + "|\n");
			}
			fw.flush();
			fw.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseCancellationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExpressionSyntaxError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
