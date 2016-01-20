/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.semanticweb.owlapi.io.AbstractOWLParser;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import se.liu.imt.mi.snomedct.parser.SNOMEDCTOntologyFormat;
import se.liu.imt.mi.snomedct.expression.tools.ExpressionSyntaxError;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;

/**
 * @author Daniel Karlsson, Linköping University, daniel.karlsson@liu.se
 * 
 */
public class SNOMEDCTOWLParser extends AbstractOWLParser {

	static final String PC_IRI = "http://snomed.info/expid/";

	// private String subject = null;
	// private boolean isPrimitive = true;
	//
	// public SNOMEDCTOWLParser(String subject, boolean isPrimitive) {
	// this.subject = subject;
	// this.isPrimitive = isPrimitive;
	// }

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
		InputStream is = null;
		if (documentSource.isInputStreamAvailable()) {
			is = documentSource.getInputStream();
		} else {
			is = getInputStream(documentSource.getDocumentIRI(), configuration);
		}

		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();

		try {
			ParseTree tree = SNOMEDCTParserUtil.parseFile(is);
			OWLVisitor visitor = new OWLVisitor(ontology);
			visitor.visit(tree);
		} catch (ParseCancellationException e) {
			e.printStackTrace();
			throw new OWLParserException(e);
		} catch (ExpressionSyntaxError e) {
			e.printStackTrace();
			throw new OWLParserException(e);
		}

//		// give each expression a number starting with 0
//		int expressionNumber = 0;
//
//		// read first line from source file
//		String line = reader.readLine();
//		while (line != null) {
//			// # is used for commenting out expression
//			if (line.startsWith("#") || line.isEmpty()) {
//				line = reader.readLine();
//				continue;
//			}
//
//			// tab separated lines
//			// tokens[0] Compositional Grammar expression
//			// tokens[1] (optional) RDFS label annotation value
//			String[] tokens = line.split("\t");
//
//			// create new class for the expression, generate new IRI
//			OWLClass newExpressionClass = dataFactory.getOWLClass(IRI
//					.create(PC_IRI + expressionNumber++));
//
//			try {
//				SNOMEDCTParserUtil.parseExpressionToOWLAxiom(tokens[0],
//						ontology, newExpressionClass);
//			} catch (ExpressionSyntaxError e) {
//				throw new OWLParserException(e);
//			}
//
//			// OWLVisitor visitor = new OWLVisitor(manager, newExpressionClass);
//			// OWLClassAxiom owlAxiom = null;
//			// try {
//			// ANTLRInputStream input = new ANTLRInputStream(tokens[0]);
//			// SNOMEDCTExpressionLexer lexer = new
//			// SNOMEDCTExpressionLexer(input);
//			// CommonTokenStream tokenStream = new CommonTokenStream(lexer);
//			// SNOMEDCTExpressionParser parser = new
//			// SNOMEDCTExpressionParser(tokenStream);
//			// ParseTree tree = parser.expression();
//			// owlAxiom = (OWLClassAxiom) visitor.visit(tree);
//			// } catch (Exception e) {
//			// throw new OWLParserException(e);
//			// }
//			//
//			// // labels for expression parts are kept in a map
//			// Map<IRI, OWLAnnotation> annotations = visitor.getLabels();
//			//
//			// // add axiom to ontology
//			// manager.addAxiom(ontology, owlAxiom);
//
//			// if there is a label, add that too
//			if (tokens.length > 1) {
//				// create label
//				OWLAnnotation label = dataFactory.getOWLAnnotation(dataFactory
//						.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL
//								.getIRI()), dataFactory
//						.getOWLLiteral(tokens[1]));
//				// add annotation axiom to ontology
//				manager.addAxiom(ontology, dataFactory
//						.getOWLAnnotationAssertionAxiom(
//								newExpressionClass.getIRI(), label));
//			}
//
//			// if (!annotations.isEmpty()) {
//			// for (Entry<IRI, OWLAnnotation> label : annotations.entrySet()) {
//			// if (dataFactory.getOWLClass(label.getKey())
//			// .getAnnotations(ontology).isEmpty())
//			// manager.addAxiom(ontology, dataFactory
//			// .getOWLAnnotationAssertionAxiom(label.getKey(),
//			// label.getValue()));
//			// }
//			// }
//
//			// read next line, readLine() will return null of end of file
//			line = reader.readLine();
//		}

		return format;
	}
}
