/**
 * 
 */
package test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;

import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyFormat;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyStorer;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTParserFactory;

/**
 * @author Daniel Karlsson, Linköping University, daniel.karlsson@liu.se
 * 
 */
public class TestSNOMEDCTOWLParser {

	private OWLOntologyManager manager;
	private OWLOntology ontology;

	static Logger logger = Logger.getLogger(TestSNOMEDCTOWLParser.class);

	public static final String snomedOWLFileName = "/res_StatedOWLF_Core_INT_20140131.owl";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		// logger.info("Loading SNOMED CT ontology...");
		// ontology = manager.loadOntologyFromOntologyDocument(new
		// File("src/test/resources/snomed.owl"));
		// logger.info("Loaded SNOMED CT ontology");

		OWLParserFactoryRegistry.getInstance().registerParserFactory(
				new SNOMEDCTParserFactory());
		manager.addOntologyStorer(new SNOMEDCTOntologyStorer());

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link se.liu.imt.mi.snomedct.parser.SNOMEDCTOWLParser#parse(org.semanticweb.owlapi.io.OWLOntologyDocumentSource, org.semanticweb.owlapi.model.OWLOntology)}
	 * . Actually, this is not as much a test method as a method for generating
	 * output
	 * 
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException
	 */
	@Test
	public void testParseAndSaveOWLOntology()
			throws OWLOntologyCreationException, OWLOntologyStorageException {
		URL expressionsURL = getClass().getResource("/expressions.owl");
		ontology = manager.loadOntologyFromOntologyDocument(new File(
				expressionsURL.getFile()));

		// create a file for the new format
		File output = new File("output_as_Turtle.owl");
		// save the ontology in Turtle format
		OWLOntologyFormat format = manager.getOntologyFormat(ontology);
		TurtleOntologyFormat turtleFormat = new TurtleOntologyFormat();
		if (format.isPrefixOWLOntologyFormat()) {
			turtleFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
		}
		manager.saveOntology(ontology, turtleFormat, IRI.create(output.toURI()));

		// create another file for SNOMED CT Compositional Grammar format
		File output2 = new File("output_as_SNOMED_CT_CG.owl");
		// save the ontology in SNOMED CT Compositional Grammar format
		SNOMEDCTOntologyFormat snomedCTFormat = new SNOMEDCTOntologyFormat();
		manager.saveOntology(ontology, snomedCTFormat,
				IRI.create(output2.toURI()));

	}

	/**
	 * Test method for
	 * {@link se.liu.imt.mi.snomedct.parser.SNOMEDCTOWLParser#parse(org.semanticweb.owlapi.io.OWLOntologyDocumentSource, org.semanticweb.owlapi.model.OWLOntology)}
	 * .
	 * 
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException
	 * @throws IOException
	 */
	@Test
	public void testLoadNonSNOMEDCTConformantOntology()
			throws OWLOntologyCreationException, OWLOntologyStorageException,
			IOException {
		// load ontology with constructs not supported by SNOMED CT
		// Compositional Grammar
		URL failFileURL = getClass().getResource("/fail_test_ontology.owl");
		ontology = manager.loadOntologyFromOntologyDocument(new File(
				failFileURL.getFile()));

		// save the ontology in SNOMED CT Compositional Grammar format
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		SNOMEDCTOntologyFormat snomedCTFormat = new SNOMEDCTOntologyFormat();
		manager.saveOntology(ontology, snomedCTFormat, os);

		// no output should be generated as no classes in the ontology in the
		// file "fail_test_ontology.owl" are supported by SNOMED CT
		// Compositional Grammar
		assertTrue(os.size() == 0);

	}

	@Test
	public void testParseAndSaveOWLOntologyWholeEnchildad()
			throws OWLOntologyCreationException, OWLOntologyStorageException,
			FileNotFoundException {
		logger.info("Loading SNOMED CT ontology...");
		URL snomedFileURL = getClass().getResource(snomedOWLFileName);
		if (snomedFileURL == null)
			throw new FileNotFoundException("SNOMED CT OWL file '"
					+ snomedOWLFileName + "' not found");
		ontology = manager.loadOntologyFromOntologyDocument(new File(
				snomedFileURL.getFile()));

		// create another file for SNOMED CT Compositional Grammar format
		File output = new File("output_whole_SNOMED_CT_as_CG_pre_classification.owl");
		// save the ontology in SNOMED CT Compositional Grammar format
		SNOMEDCTOntologyFormat snomedCTFormat = new SNOMEDCTOntologyFormat();
		manager.saveOntology(ontology, snomedCTFormat,
				IRI.create(output.toURI()));

		// Create an ELK reasoner.
		OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

		// Classify the ontology.
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		//reasoner.flush();
		
		 // To generate an inferred ontology we use implementations of
        // inferred axiom generators
        List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        gens.add(new InferredSubClassAxiomGenerator());
        gens.add(new InferredEquivalentClassAxiomGenerator());

        // Put the inferred axioms into a fresh empty ontology.
        OWLOntologyManager outputManager = OWLManager.createOWLOntologyManager();
        OWLParserFactoryRegistry.getInstance().registerParserFactory(
				new SNOMEDCTParserFactory());
        outputManager.addOntologyStorer(new SNOMEDCTOntologyStorer());
        OWLOntology infOnt = outputManager.createOntology();
        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner,
                        gens);
        iog.fillOntology(outputManager, infOnt);

    	logger.info("subclass-of axioms = " + infOnt.getAxiomCount(AxiomType.SUBCLASS_OF));
    	logger.info("equivalent-to axioms = " + infOnt.getAxiomCount(AxiomType.EQUIVALENT_CLASSES));
    	
    	List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
    	
    	// add annotations from original ontology
    	for (OWLOntology o : reasoner.getRootOntology().getImportsClosure()) {
    		for (OWLAnnotation annot : o.getAnnotations()) {
    			changes.add(new AddOntologyAnnotation(infOnt, annot));
    		}
    		for (OWLAnnotationAssertionAxiom axiom : o.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
    			changes.add(new AddAxiom(infOnt, axiom));
    		}
    	}
    	
    	// add asserted from original ontology
    	for (OWLOntology o : reasoner.getRootOntology().getImportsClosure()) {
    		for (OWLLogicalAxiom ax : o.getLogicalAxioms()) {
    			if (ax.isAnnotated() && infOnt.containsAxiom(ax.getAxiomWithoutAnnotations())) {
    				changes.add(new RemoveAxiom(infOnt, ax.getAxiomWithoutAnnotations()));
    			}
    			changes.add(new AddAxiom(infOnt, ax));
    		}
    	}
    	
    	outputManager.applyChanges(changes);

		// create another file for SNOMED CT Compositional Grammar format
		File output2 = new File("output_whole_SNOMED_CT_as_CG_post_classification.owl");
		// save the ontology in SNOMED CT Compositional Grammar format
		outputManager.saveOntology(infOnt, snomedCTFormat,
				IRI.create(output2.toURI()));
		
		reasoner.dispose();

	}
	
	
	@Test
	public void testParseAndSaveObsAlpha()
			throws OWLOntologyCreationException, OWLOntologyStorageException {
		URL expressionsURL = getClass().getResource("/obsAlpha.owl");
		ontology = manager.loadOntologyFromOntologyDocument(new File(
				expressionsURL.getFile()));

		// create another file for SNOMED CT Compositional Grammar format
		File output = new File("obsAlpha_as_SNOMED_CT_CG.owl");
		// save the ontology in SNOMED CT Compositional Grammar format
		SNOMEDCTOntologyFormat snomedCTFormat = new SNOMEDCTOntologyFormat();
		manager.saveOntology(ontology, snomedCTFormat,
				IRI.create(output.toURI()));

	}

}
