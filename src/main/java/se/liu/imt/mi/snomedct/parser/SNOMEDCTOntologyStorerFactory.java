/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import org.semanticweb.owlapi.model.OWLStorer;
import org.semanticweb.owlapi.util.OWLStorerFactoryImpl;

/**
 * @author danka74
 *
 */
public class SNOMEDCTOntologyStorerFactory extends OWLStorerFactoryImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7039843405327813026L;

	public SNOMEDCTOntologyStorerFactory() {
		super(new SNOMEDCTDocumentFormatFactory());
	}
	
	@Override
	public OWLStorer createStorer() {
		// TODO Auto-generated method stub
		return new SNOMEDCTOntologyStorer();
	}

}
