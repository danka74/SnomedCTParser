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
public class SNOMEDCTExpressionRefsetDocumentFormatFactory extends OWLDocumentFormatFactoryImpl {

    
    /**
	 * 
	 */
	private static final long serialVersionUID = 23781371287387L;

	@Override
    public String getKey() {
        return "SNOMED CT Expression Association Refset Format";
    }
    
	@Override
	public OWLDocumentFormat createFormat() {
		return new SNOMEDCTExpressionRefsetDocumentFormat();
	}
}