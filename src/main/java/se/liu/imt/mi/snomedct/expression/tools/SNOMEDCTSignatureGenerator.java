/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.normalform.NormalFormRewriter;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import se.liu.imt.mi.snomedct.parser.OWLVisitor;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyFormat;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyStorer;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTParserFactory;
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
		
		CommandLineParser parser = new PosixParser();
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
