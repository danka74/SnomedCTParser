/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.util.OWLDocumentFormatFactoryImpl;

/**
 * @author Daniel Karlsson, Linköping university, daniel.karlsson@liu.se
 * @
 *
 */
public class SNOMEDCTDocumentFormatFactory extends OWLDocumentFormatFactoryImpl {

    
    @Override
    public String getKey() {
        return "SNOMED CT Compositional Grammar Format";
    }
    
	@Override
	public OWLDocumentFormat createFormat() {
		return new SNOMEDCTDocumentFormat();
	}
}