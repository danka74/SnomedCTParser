/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyFormat;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyStorer;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTParserFactory;

/**
 * @author Daniel Karlsson, Linköping Univsrsity, daniel.karlsson@liu.se
 * @author Kent Spackman, IHTSDO, ksp@ihtsdo.org
 * 
 * 
 */
public class SNOMEDCTTranslator {

	static Logger logger = Logger.getLogger(SNOMEDCTTranslator.class);

	/**
	 * @param args
	 * @throws OWLException
	 */
	public static void main(String[] args) throws OWLException {
		// create Options object
		Options options = new Options();

		// add OWL output format option
		options.addOption("f", "owl-format", true,
				"OWL output format [turtle|owlf|sct]");
		// add normal form/stated option
		options.addOption("n", "normal-form", true,
				"output normal form [stated|distribution|flat]");
		// add SNOMED CT ontology file option
		options.addOption("s", "snomed-file", true, "SNOMED CT ontology file");
		// add labels option
		options.addOption("l", "labels", false, "show labels in output");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}

		String format = cmd.getOptionValue("owl-format", "sct");
		String normalForm = cmd.getOptionValue("normal-form", "stated");
		String snomedCTFile = cmd.getOptionValue("snomed-file");
		boolean labels = cmd.hasOption("labels");

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
				outputFileName = tokens[0] + "_" + format + "_" + normalForm
						+ "." + tokens[1];
			else
				outputFileName = inputFileName + "_" + format + "_"
						+ normalForm;
		} else
			outputFileName = (String) argList.get(1);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		// add SNOMED CT parser and storer to manager
		OWLParserFactoryRegistry.getInstance().registerParserFactory(
				new SNOMEDCTParserFactory());
		manager.addOntologyStorer(new SNOMEDCTOntologyStorer());

		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(new File(inputFileName));

		OWLOntologyFormat ontologyFormat = null;
		switch (format) {
		case "turtle":
			ontologyFormat = new TurtleOntologyFormat();
			break;
		case "owlf":
			ontologyFormat = new OWLFunctionalSyntaxOntologyFormat();
			break;
		case "sct": // SNOMED CT Compositional Grammar
			ontologyFormat = new SNOMEDCTOntologyFormat();
			ontologyFormat.setParameter("labels", labels);
			break;
		default:
			System.exit(1);
		}

		if (normalForm.equals("stated")) {
			// if stated form, just output the ontology in the selected format
			manager.saveOntology(ontology, ontologyFormat,
					IRI.create(new File(outputFileName)));
		} else if (normalForm.equals("distribution")
				|| normalForm.equals("flat")) {
			// if not stated form, classify the ontology, possibly after first
			// importing (a module from) SNOMED CT

			if (snomedCTFile != null) {
				// Import SNOMED CT ontology
				IRI snomedCTIRI = IRI.create(new File(snomedCTFile));
				OWLImportsDeclaration importDeclaration = ontology
						.getOWLOntologyManager().getOWLDataFactory()
						.getOWLImportsDeclaration(snomedCTIRI);
				ontology.getOWLOntologyManager().loadOntology(snomedCTIRI);
				ontology.getOWLOntologyManager().applyChange(
						new AddImport(ontology, importDeclaration));
			}

			NormalFormRewriter normalFormConverter = null;
			// create a fresh empty ontology for output of inferred expression
			OWLOntologyManager outputManager = OWLManager
					.createOWLOntologyManager();
			// add SNOMED CT storer to ontology manager
			outputManager.addOntologyStorer(new SNOMEDCTOntologyStorer());
			OWLOntology inferredOntology = outputManager.createOntology();

			List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

			if (normalForm.equals("distribution")) {
				// Create reasoner and classify the ontology including SNOMED CT
				OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
				OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
				Logger.getLogger("org.semanticweb.elk").setLevel(Level.ERROR);
				reasoner.flush();
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

				// create the normal form converter
				normalFormConverter = new DistributionNormalFormConverter(
						ontology, reasoner);

//				// add annotations from original ontology
//				for (OWLOntology o : reasoner.getRootOntology()
//						.getImportsClosure()) {
//					for (OWLAnnotation annot : o.getAnnotations()) {
//						changes.add(new AddOntologyAnnotation(inferredOntology,
//								annot));
//					}
//					for (OWLAnnotationAssertionAxiom axiom : o
//							.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
//						changes.add(new AddAxiom(inferredOntology, axiom));
//					}
//				}
			} else
				normalFormConverter = new Flattener(manager);

			// iterate over equivalent classes axioms in the source ontology
			for (OWLEquivalentClassesAxiom eqAxiom : ontology
					.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
				// logger.info("axiom = " + eqAxiom.toString());
				// the equivalent classes axiom is assumed to have only two
				// class expressions, the second (right hand side) being the
				// class definition. As the source ontology is resulting from a
				// set of Compositional Grammar expressions, e.g. concept
				// inclusion is not possible.
				Iterator<OWLClassExpression> expressionSet = eqAxiom
						.getClassExpressions().iterator();
				OWLClassExpression lhs = expressionSet.next(); // left hand side
				OWLClassExpression rhs = expressionSet.next(); // right hand
																// side
				// convert the class definition to normal form
				OWLClassExpression normalFormExpression = normalFormConverter
						.convertToNormalForm(rhs);
				// logger.info("expression = " + rhs.toString());
				// logger.info("normal form = " + normalFormExpression);
				changes.add(new AddAxiom(inferredOntology,
						inferredOntology
								.getOWLOntologyManager()
								.getOWLDataFactory()
								.getOWLEquivalentClassesAxiom(lhs,
										normalFormExpression)));
			}

			// iterate over subclass axioms in the source ontology
			for (OWLSubClassOfAxiom subClassAxiom : ontology
					.getAxioms(AxiomType.SUBCLASS_OF)) {
				// logger.info("axiom = " + subClassAxiom.toString());

				OWLClassExpression lhs = subClassAxiom.getSubClass(); // left
																		// hand
																		// side
				OWLClassExpression rhs = subClassAxiom.getSuperClass(); // right
																		// hand
				// side

				// convert the class definition to normal form
				OWLClassExpression normalFormExpression = normalFormConverter
						.convertToNormalForm(rhs);
				// logger.info("expression = " + rhs.toString());
				// logger.info("normal form = " + normalFormExpression);
				changes.add(new AddAxiom(inferredOntology, inferredOntology
						.getOWLOntologyManager().getOWLDataFactory()
						.getOWLSubClassOfAxiom(lhs, normalFormExpression)));
			}

			outputManager.applyChanges(changes);

			// save the ontology in the selected format
			outputManager.saveOntology(inferredOntology, ontologyFormat,
					IRI.create(new File(outputFileName)));

		}

	}
}
