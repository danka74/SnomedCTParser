/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormatImpl;
import org.semanticweb.owlapi.util.OWLDocumentFormatFactoryImpl;

/**
 * @author Daniel Karlsson, Linköping university, daniel.karlsson@liu.se 
 */

public class SNOMEDCTDocumentFormat extends OWLDocumentFormatImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2159844877279654256L;

	@Override
	public String getKey() {
        return "SNOMED CT Compositional Grammar Format";
	}

	@Override
	public PrefixDocumentFormat asPrefixOWLOntologyFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPrefixOWLOntologyFormat() {
		// TODO Auto-generated method stub
		return false;
	}

}