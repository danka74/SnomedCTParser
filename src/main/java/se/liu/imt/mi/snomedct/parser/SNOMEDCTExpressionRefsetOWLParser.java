/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.semanticweb.owlapi.io.AbstractOWLParser;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
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
public class SNOMEDCTExpressionRefsetOWLParser extends AbstractOWLParser {

	@Override
	public OWLDocumentFormatFactory getSupportedFormat() {
		return new SNOMEDCTExpressionRefsetDocumentFormatFactory();
	}

	static final String PC_IRI = "http://snomed.info/expid/";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owlapi.io.OWLParser#parse(org.semanticweb.owlapi.io.
	 * OWLOntologyDocumentSource, org.semanticweb.owlapi.model.OWLOntology,
	 * org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration)
	 */
	@Override
	public OWLDocumentFormat parse(OWLOntologyDocumentSource documentSource, OWLOntology ontology,
			OWLOntologyLoaderConfiguration configuration)
			throws OWLParserException, IOException, OWLOntologyChangeException, UnloadableImportException {

		SNOMEDCTExpressionRefsetDocumentFormat format = new SNOMEDCTExpressionRefsetDocumentFormat();

		InputStream is = documentSource.getInputStream();

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = reader.readLine(); // "id	effectiveTime	active	moduleId	refsetId	referencedComponentId	mapTarget	expression	definitionStatusId	correlationId	contentOriginId"

		if(!"id	effectiveTime	active	moduleId	refsetId	referencedComponentId	mapTarget	expression	definitionStatusId	correlationId	contentOriginId".equals(line))
			throw new OWLParserException("Not a SNOMED CT Expression Association Refset file");

		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();

		line = reader.readLine();
		while (line != null && line.length() > 0) {

			String[] fields = line.split("\t");
			
			if(fields.length > 7 && fields[2].equals("1")) { // active
				String code = fields[6];
				String expression = fields[7];
				
				OWLClass cls = dataFactory.getOWLClass(IRI.create("http://loinc.org/" + code));
				

				try {
					OWLAxiom axiom = SNOMEDCTParserUtil.parseExpressionToOWLAxiom(expression, ontology, cls);
					manager.addAxiom(ontology, axiom);
					
				} catch (ParseCancellationException e) {
					e.printStackTrace();
					throw new OWLParserException(e);
				} catch (ExpressionSyntaxError e) {
					e.printStackTrace();
					throw new OWLParserException(e);
				}
				
				
			} else
				throw new OWLParserException("Not a SNOMED CT Expression Association Refset file");
			
			line = reader.readLine();
		}


		return format;
	}
}
