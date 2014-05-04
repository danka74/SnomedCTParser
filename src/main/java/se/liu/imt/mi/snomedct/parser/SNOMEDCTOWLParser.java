/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.runtime.tree.Tree;
import org.semanticweb.owlapi.io.AbstractOWLParser;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import se.liu.imt.mi.snomedct.expression.tools.SCTOWLExpressionBuilder;
import se.liu.imt.mi.snomedct.expression.tools.SnomedCTParser;

/**
 * @author Daniel Karlsson, Linköping University, daniel.karlsson@liu.se
 * 
 */
public class SNOMEDCTOWLParser extends AbstractOWLParser {

	static final String PC_IRI = "http://snomed.info/expid/";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owlapi.io.OWLParser#parse(org.semanticweb.owlapi.io.
	 * OWLOntologyDocumentSource, org.semanticweb.owlapi.model.OWLOntology)
	 */
	@Override
	public OWLOntologyFormat parse(OWLOntologyDocumentSource documentSource,
			OWLOntology ontology) throws OWLParserException, IOException,
			OWLOntologyChangeException, UnloadableImportException {
		return parse(documentSource, ontology,
				new OWLOntologyLoaderConfiguration());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owlapi.io.OWLParser#parse(org.semanticweb.owlapi.io.
	 * OWLOntologyDocumentSource, org.semanticweb.owlapi.model.OWLOntology,
	 * org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration)
	 */
	@Override
	public OWLOntologyFormat parse(OWLOntologyDocumentSource documentSource,
			OWLOntology ontology, OWLOntologyLoaderConfiguration configuration)
			throws OWLParserException, IOException, OWLOntologyChangeException,
			UnloadableImportException {

		SNOMEDCTOntologyFormat format = new SNOMEDCTOntologyFormat();

		// get BufferedReader from document source
		BufferedReader reader = null;
		if (documentSource.isInputStreamAvailable()) {
			reader = new BufferedReader(new InputStreamReader(
					documentSource.getInputStream()));
		} else if (documentSource.isReaderAvailable()) {
			reader = new BufferedReader(documentSource.getReader());
		} else {
			reader = new BufferedReader(new InputStreamReader(getInputStream(
					documentSource.getDocumentIRI(), configuration)));
		}

		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();

		// create OWL expression builder
		SCTOWLExpressionBuilder expressionBuilder = new SCTOWLExpressionBuilder(
				ontology, dataFactory);

		try {
			// give each expression a number starting with 0
			int expressionNumber = 0;
			// read first line from source file
			String line = reader.readLine();
			while (line != null) {
				// tab separated lines
				// tokens[0] Compositional Grammar expression
				// tokens[1] (optional) RDFS label annotation value
				String[] tokens = line.split("\t");
				// translate Compositional Grammar expression to OWl
				Tree t = SnomedCTParser.parseExpression(tokens[0]);
				Map<IRI, OWLAnnotation> annotations = new HashMap<IRI, OWLAnnotation>();
				OWLClassExpression owlExpression = expressionBuilder
						.translateToOWL(t, annotations);

				// create new class for the expression, generate new IRI
				OWLClass newExpressionClass = dataFactory.getOWLClass(IRI
						.create(PC_IRI + expressionNumber++));

				// add equivalence axiom to ontology
				manager.addAxiom(ontology, dataFactory
						.getOWLEquivalentClassesAxiom(newExpressionClass,
								owlExpression));
				// if there is a label, add that too
				if (tokens.length > 1) {
					// create label
					OWLAnnotation label = dataFactory
							.getOWLAnnotation(
									dataFactory
											.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL
													.getIRI()), dataFactory
											.getOWLLiteral(tokens[1]));
					// add annotation axiom to ontology
					manager.addAxiom(ontology, dataFactory
							.getOWLAnnotationAssertionAxiom(
									newExpressionClass.getIRI(), label));
				}

				if (!annotations.isEmpty()) {
					for (Entry<IRI, OWLAnnotation> label : annotations
							.entrySet()) {
						if (dataFactory.getOWLClass(label.getKey())
								.getAnnotations(ontology).isEmpty())
							manager.addAxiom(ontology, dataFactory
									.getOWLAnnotationAssertionAxiom(
											label.getKey(), label.getValue()));
					}
				}

				// read next line, readLine() will return null of end of file
				line = reader.readLine();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return format;
	}
}
