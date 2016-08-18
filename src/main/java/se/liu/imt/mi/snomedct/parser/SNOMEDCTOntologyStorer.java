/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.io.PrintWriter;
import java.io.Writer;

import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;


/**
 * @author daniel
 *
 */
public class SNOMEDCTOntologyStorer extends AbstractOWLStorer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 30406L;

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyStorer#canStoreOntology(org.semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	public boolean canStoreOntology(OWLDocumentFormat ontologyFormat) {
		return ontologyFormat.equals(new SNOMEDCTDocumentFormat());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.AbstractOWLOntologyStorer#storeOntology(org.semanticweb.owlapi.model.OWLOntology, java.io.Writer, org.semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	protected void storeOntology(OWLOntology ontology, PrintWriter writer,
			OWLDocumentFormat format) throws OWLOntologyStorageException {
		try {
            SNOMEDCTRenderer ren = new SNOMEDCTRenderer((boolean) format.getParameter("labels", true));
            ren.render(ontology, writer);
        } catch (Exception e) {
            throw new OWLOntologyStorageException(e);
        }

	}

}
