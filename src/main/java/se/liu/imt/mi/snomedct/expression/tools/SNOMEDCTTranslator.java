/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;

import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyFormat;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyStorer;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTParserFactory;
import test.TestSNOMEDCTOWLParser;

/**
 * @author daniel
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
		options.addOption("f", "owl-format", true, "OWL output format");
		// add normal form/stated option
		options.addOption("n", "normal-form", true, "output normal form");
		// add SNOMED CT ontology file option
		options.addOption("s", "snomed-file", true, "SNOMED CT ontology file");

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
		List<String> argList = cmd.getArgList();
		if (argList.size() < 1) {
			System.exit(2);
		}
		String inputFileName = argList.get(0);
		String outputFileName;
		if (argList.size() < 2) {
			String[] tokens = inputFileName.split("\\.(?=[^\\.]+$)");
			if (tokens.length >= 2)
				outputFileName = tokens[0] + "_" + format + "_" + normalForm
						+ "." + tokens[1];
			else
				outputFileName = inputFileName + "_" + format + "_"
						+ normalForm;
		} else
			outputFileName = argList.get(1);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

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
		default: // SNOMED CT Compositional Grammar
			ontologyFormat = new SNOMEDCTOntologyFormat();
			break;
		}

		if (normalForm.equals("stated")) {

			manager.saveOntology(ontology, ontologyFormat,
					IRI.create(new File(outputFileName)));

		} else if (normalForm.equals("distribution")) {
			// Import SNOMED CT ontology
			IRI snomedCTIRI = IRI.create(new File(snomedCTFile));
			OWLImportsDeclaration importDeclaration = ontology
					.getOWLOntologyManager().getOWLDataFactory()
					.getOWLImportsDeclaration(snomedCTIRI);
			ontology.getOWLOntologyManager().loadOntology(snomedCTIRI);
			ontology.getOWLOntologyManager().applyChange(
					new AddImport(ontology, importDeclaration));

			// Create reasoner and classify the ontology including SNOMED CT
			OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
			OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
			reasoner.flush();
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

			// Get inferred axioms
			List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
			gens.add(new InferredSubClassAxiomGenerator());
			gens.add(new InferredEquivalentClassAxiomGenerator());

			// Put the inferred axioms into a fresh empty ontology.
			OWLOntologyManager outputManager = OWLManager
					.createOWLOntologyManager();
			outputManager.addOntologyStorer(new SNOMEDCTOntologyStorer());
			OWLOntology inferredOntology = outputManager.createOntology();
			InferredOntologyGenerator iog = new InferredOntologyGenerator(
					reasoner, gens);
			iog.fillOntology(outputManager, inferredOntology);

			logger.info("subclass-of axioms = "
					+ inferredOntology.getAxiomCount(AxiomType.SUBCLASS_OF));
			logger.info("equivalent-to axioms = "
					+ inferredOntology
							.getAxiomCount(AxiomType.EQUIVALENT_CLASSES));

			List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

			DistributionNormalFormConverter distFormConv = new DistributionNormalFormConverter(
					ontology, reasoner);
			for (OWLLogicalAxiom ax : ontology.getLogicalAxioms()) {
				logger.info("axiom = " + ax.toString());
				if (ax.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
					Iterator<OWLClassExpression> expressionSet = ((OWLEquivalentClassesAxiom) ax)
							.getClassExpressions().iterator();
					OWLClassExpression lhs = expressionSet.next();
					OWLClassExpression rhs = expressionSet.next();
					OWLClassExpression normalFormExpression = distFormConv.convertToNormalForm(rhs);
					logger.info("expression = " + rhs.toString());
					logger.info("normal form = "
							+ normalFormExpression);
					changes.add(new AddAxiom(inferredOntology, inferredOntology
							.getOWLOntologyManager().getOWLDataFactory()
							.getOWLEquivalentClassesAxiom(lhs, normalFormExpression)));
				}

			}
			
			// add annotations from original ontology
	    	for (OWLOntology o : reasoner.getRootOntology().getImportsClosure()) {
	    		for (OWLAnnotation annot : o.getAnnotations()) {
	    			changes.add(new AddOntologyAnnotation(inferredOntology, annot));
	    		}
	    		for (OWLAnnotationAssertionAxiom axiom : o.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
	    			changes.add(new AddAxiom(inferredOntology, axiom));
	    		}
	    	}


			outputManager.applyChanges(changes);

			// save the ontology in SNOMED CT Compositional Grammar format
			outputManager.saveOntology(inferredOntology, ontologyFormat,
					IRI.create(new File(outputFileName)));

			reasoner.dispose();

		}

	}
}