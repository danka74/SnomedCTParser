/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.semanticweb.owlapi.io.AbstractOWLParser;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser;
import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyFormat;

/**
 * @author Daniel Karlsson, Linköping University, daniel.karlsson@liu.se
 * 
 */
public class SNOMEDCTOWLParser extends AbstractOWLParser {

<<<<<<< HEAD
	static final String PC_IRI = "http://snomed.info/expid/";
	private String subject = null;
	private boolean isPrimitive = true;

	public SNOMEDCTOWLParser(String subject, boolean isPrimitive) {
		this.subject = subject;
		this.isPrimitive = isPrimitive;
	}
=======
	static final String PC_IRI = "http://snomed.info/postcoord/";
>>>>>>> branch 'CG2014' of https://github.com/danka74/SnomedCTParser.git

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

		// give each expression a number starting with 0
		int expressionNumber = 0;
		
		// read first line from source file
		String line = reader.readLine();
		while (line != null) {
			// # is used for commenting out expression
			if (line.startsWith("#")) {
				line = reader.readLine();
				continue;
			}

			// tab separated lines
			// tokens[0] Compositional Grammar expression
			// tokens[1] (optional) RDFS label annotation value
			String[] tokens = line.split("\t");
<<<<<<< HEAD
			// translate Compositional Grammar expression to OWl
			Tree t;
			Map<IRI, OWLAnnotation> annotations = new HashMap<IRI, OWLAnnotation>();
			OWLClassExpression owlExpression = null;
			try {
				t = SnomedCTParser.parseExpression(tokens[0]);
				
				owlExpression = expressionBuilder
						.translateToOWL(t, annotations);
			} catch (Exception e) {
				throw new OWLParserException(e);
			}

			// create new class for the expression, generate new IRI or use the existing one if it is there
			String subj;
			if(this.subject != null) {
				subj = this.subject;
				this.subject = null;
			} else {
				subj = PC_IRI + expressionNumber++;
			}
			OWLClass newExpressionClass = dataFactory.getOWLClass(IRI.create(subj));
=======
			
			// create new class for the expression, generate new IRI
			OWLClass newExpressionClass = dataFactory.getOWLClass(IRI
					.create(PC_IRI + expressionNumber++));
>>>>>>> branch 'CG2014' of https://github.com/danka74/SnomedCTParser.git

<<<<<<< HEAD
			// add equivalence axiom to ontology
			if(this.isPrimitive) {
				manager.addAxiom(ontology, dataFactory
						.getOWLSubClassOfAxiom(newExpressionClass,
								owlExpression));
			} else {
				manager.addAxiom(ontology, dataFactory
						.getOWLEquivalentClassesAxiom(newExpressionClass,
								owlExpression));
			}
=======
			OWLVisitor visitor = new OWLVisitor(manager, newExpressionClass);
			OWLClassAxiom owlAxiom = null;
			try {
				ANTLRInputStream input = new ANTLRInputStream(tokens[0]);
				SNOMEDCTExpressionLexer lexer = new SNOMEDCTExpressionLexer(input);
			    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
				SNOMEDCTExpressionParser parser = new SNOMEDCTExpressionParser(tokenStream);
				ParseTree tree = parser.expression();
				owlAxiom = (OWLClassAxiom) visitor.visit(tree);
			} catch (Exception e) {
				throw new OWLParserException(e);
			}

			// labels for expression parts are kept in a map 
			Map<IRI, OWLAnnotation> annotations = visitor.getLabels();
			
			// add axiom to ontology
			manager.addAxiom(ontology, owlAxiom);

>>>>>>> branch 'CG2014' of https://github.com/danka74/SnomedCTParser.git
			// if there is a label, add that too
			if (tokens.length > 1) {
				// create label
				OWLAnnotation label = dataFactory.getOWLAnnotation(dataFactory
						.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL
								.getIRI()), dataFactory
						.getOWLLiteral(tokens[1]));
				// add annotation axiom to ontology
				manager.addAxiom(ontology, dataFactory
						.getOWLAnnotationAssertionAxiom(
								newExpressionClass.getIRI(), label));
			}

			if (!annotations.isEmpty()) {
				for (Entry<IRI, OWLAnnotation> label : annotations.entrySet()) {
					if (dataFactory.getOWLClass(label.getKey())
							.getAnnotations(ontology).isEmpty())
						manager.addAxiom(ontology, dataFactory
								.getOWLAnnotationAssertionAxiom(label.getKey(),
										label.getValue()));
				}
			}

			// read next line, readLine() will return null of end of file
			line = reader.readLine();
		}

		return format;
	}
}
