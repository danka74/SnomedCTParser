package test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import se.liu.imt.mi.snomedct.expression.tools.Flattener;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTDocumentFormat;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyStorer;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTParserFactory;

public class TestFlattener {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@Test
	public void test() throws OWLOntologyCreationException, OWLOntologyStorageException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		URL expressionsURL = getClass().getResource("/obsTechPreviewExtract.owl");
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(new File(expressionsURL
						.getFile()));

		// create a fresh empty ontology for output of inferred expression
		OWLOntologyManager outputManager = OWLManager
				.createOWLOntologyManager();
		// add SNOMED CT storer to ontology manager
		outputManager.addOntologyStorer(new SNOMEDCTOntologyStorer());
		OWLOntology inferredOntology = outputManager.createOntology();

		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

		Flattener flattener = new Flattener(manager);

		for (OWLEquivalentClassesAxiom eqAxiom : ontology
				.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
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
			OWLClassExpression flatExpression = flattener
					.convertToNormalForm(rhs);
			// logger.info("expression = " + rhs.toString());
			// logger.info("normal form = " + normalFormExpression);
			changes.add(new AddAxiom(inferredOntology, outputManager
					.getOWLDataFactory().getOWLEquivalentClassesAxiom(lhs,
							flatExpression)));
		}

		for (OWLSubClassOfAxiom subClassAxiom : ontology
				.getAxioms(AxiomType.SUBCLASS_OF)) {
			OWLClassExpression lhs = subClassAxiom.getSubClass(); // left
																	// hand
																	// side
			OWLClassExpression rhs = subClassAxiom.getSuperClass(); // right
																	// hand
			// side
			// convert the class definition to normal form
			OWLClassExpression flatExpression = flattener
					.convertToNormalForm(rhs);
			// logger.info("expression = " + rhs.toString());
			// logger.info("normal form = " + normalFormExpression);
			changes.add(new AddAxiom(inferredOntology, inferredOntology
					.getOWLOntologyManager().getOWLDataFactory()
					.getOWLSubClassOfAxiom(lhs, flatExpression)));
		}

		outputManager.applyChanges(changes);

		outputManager.saveOntology(inferredOntology, new SNOMEDCTDocumentFormat(),
				IRI.create(new File("flatTest.owl")));

	}

}
