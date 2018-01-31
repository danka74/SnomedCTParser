/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import se.liu.imt.mi.snomedct.parser.SNOMEDCTExpressionRefsetParserFactory;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTParserFactory;

/**
 * @author dlkn02
 *
 */
public class TestExpressionRefsetParser {

	private OWLOntologyManager manager;
	private OWLOntology ontology;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		manager.getOntologyParsers().add(new SNOMEDCTExpressionRefsetParserFactory());
	}

	@Test
	public void test() throws OWLOntologyCreationException, OWLOntologyStorageException {
		URL expressionsURL = getClass()
				.getResource("/der2_sscccRefset_fake_LOINCExpressionAssociationSnapshot_INT_20170731.txt");
		ontology = manager.loadOntologyFromOntologyDocument(new File(expressionsURL.getFile()));

		// create a file for the new format
		File output = new File("loinc_as_turtle.owl");
		// save the ontology in Turtle format
		OWLDocumentFormat format = manager.getOntologyFormat(ontology);
		TurtleDocumentFormat turtleFormat = new TurtleDocumentFormat();
		if (format.isPrefixOWLOntologyFormat()) {
			turtleFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
		}
		manager.saveOntology(ontology, turtleFormat, IRI.create(output.toURI()));
	}

}
