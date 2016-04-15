/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import org.semanticweb.owlapi.model.OWLDocumentFormatImpl;
import org.semanticweb.owlapi.util.OWLDocumentFormatFactoryImpl;

/**
 * @author Daniel Karlsson, Linköping university, daniel.karlsson@liu.se 
 */

public class SNOMEDCTDocumentFormat extends OWLDocumentFormatImpl {

	//private static final long serialVersionUID = 30406L;

	@Override
	public String getKey() {
		return "SNOMED CT Compositional Grammar Format";
	}

}