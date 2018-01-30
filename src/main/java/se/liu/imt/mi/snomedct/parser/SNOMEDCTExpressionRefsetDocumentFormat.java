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

public class SNOMEDCTExpressionRefsetDocumentFormat extends OWLDocumentFormatImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8947589357234895723L;

	@Override
	public String getKey() {
        return "SNOMED CT Expression Association Refset Format";
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