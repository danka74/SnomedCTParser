/**
 * 
 */
package test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

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
			throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
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

}
