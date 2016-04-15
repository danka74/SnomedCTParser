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
import org.semanticweb.owlapi.io.DocumentSources;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDocumentFormatFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import se.liu.imt.mi.snomedct.parser.SNOMEDCTDocumentFormat;
import se.liu.imt.mi.snomedct.expression.tools.ExpressionSyntaxError;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;

/**
 * @author Daniel Karlsson, Linköping University, daniel.karlsson@liu.se
 * 
 */
public class SNOMEDCTOWLParser extends AbstractOWLParser {

	@Override
    public OWLDocumentFormatFactory getSupportedFormat() {
        return new SNOMEDCTDocumentFormatFactory();
    }
	
	static final String PC_IRI = "http://snomed.info/expid/";
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owlapi.io.OWLParser#parse(org.semanticweb.owlapi.io.
	 * OWLOntologyDocumentSource, org.semanticweb.owlapi.model.OWLOntology)
	 */
	@Override
	public OWLDocumentFormat parse(OWLOntologyDocumentSource documentSource,
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
	public OWLDocumentFormat parse(OWLOntologyDocumentSource documentSource,
			OWLOntology ontology, OWLOntologyLoaderConfiguration configuration)
//			throws OWLParserException, IOException, OWLOntologyChangeException,
//			UnloadableImportException {
			{

		SNOMEDCTDocumentFormat format = new SNOMEDCTDocumentFormat();

		InputStream is = null;
		if (documentSource.getInputStream().isPresent()) {
			is = documentSource.getInputStream().get();
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
		} catch (IOException e) {
			e.printStackTrace();
			throw new OWLParserException(e);
		}

		return format;
	}
}
