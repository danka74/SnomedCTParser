/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import org.semanticweb.owlapi.io.OWLParser;
import org.semanticweb.owlapi.io.OWLParserFactoryImpl;
import org.semanticweb.owlapi.model.OWLDocumentFormatFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Daniel Karlsson, Linköping University, daniel.karlsson@liu.se
 *
 */
public class SNOMEDCTParserFactory extends OWLParserFactoryImpl {

	protected SNOMEDCTParserFactory() {
		super(new SNOMEDCTDocumentFormatFactory());
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3285098326875692657L;
	/* BASE IRI - can be overridden */
	public String subject = null;
	/* isPrimitive - defaults to false to preserve existing behavior */
	public boolean isPrimitive = false;

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLParserFactory#createParser(org.semanticweb.owlapi.model.OWLOntologyManager)
	 */
	@Override
	public OWLParser createParser() {
		return new SNOMEDCTOWLParser(); //subject, isPrimitive);
	}

}
