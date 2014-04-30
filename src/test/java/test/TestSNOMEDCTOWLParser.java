/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.log4j.Logger;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import se.liu.imt.mi.snomedct.parser.SNOMEDCTParserFactory;

/**
 * @author daniel
 *
 */
public class TestSNOMEDCTOWLParser {
	
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory dataFactory;
	private OWLReasoner reasoner;
	
	static Logger logger = Logger.getLogger(TestSNOMEDCTOWLParser.class);


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
//		logger.info("Loading SNOMED CT ontology...");
//		ontology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/snomed.owl"));
//		logger.info("Loaded SNOMED CT ontology");

		
		dataFactory = manager.getOWLDataFactory();
		
		OWLParserFactoryRegistry.getInstance().registerParserFactory(new SNOMEDCTParserFactory());

		
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link se.liu.imt.mi.snomedct.parser.SNOMEDCTOWLParser#parse(org.semanticweb.owlapi.io.OWLOntologyDocumentSource, org.semanticweb.owlapi.model.OWLOntology)}.
	 * @throws OWLOntologyCreationException 
	 * @throws OWLOntologyStorageException 
	 */
	@Test
	public void testParseOWLOntologyDocumentSourceOWLOntology() throws OWLOntologyCreationException, OWLOntologyStorageException {
//		logger.info("Loading expressions...");
//		IRI importIRI = IRI.create(new File("src/test/resources/expressions.scg"));
//		OWLImportsDeclaration importDeclaraton =
//				   dataFactory.getOWLImportsDeclaration(importIRI); 
//		manager.applyChange(new AddImport(ontology, importDeclaraton));
//		manager.loadOntology(importIRI);
		ontology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/expressions.scg"));
		
		//Create a file for the new format
		File output = new File("output.owl");
		//Save the ontology in a different format
		OWLOntologyFormat format = manager.getOntologyFormat(ontology);
		TurtleOntologyFormat turtleFormat = new TurtleOntologyFormat();
		if (format.isPrefixOWLOntologyFormat()) { 
		  turtleFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat()); 
		}
		manager.saveOntology(ontology, turtleFormat, IRI.create(output.toURI()));
	}

}
