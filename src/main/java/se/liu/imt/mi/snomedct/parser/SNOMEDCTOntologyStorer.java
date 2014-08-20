/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import java.io.Writer;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.AbstractOWLOntologyStorer;

/**
 * @author daniel
 *
 */
public class SNOMEDCTOntologyStorer extends AbstractOWLOntologyStorer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 30406L;

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyStorer#canStoreOntology(org.semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	public boolean canStoreOntology(OWLOntologyFormat ontologyFormat) {
		return ontologyFormat.equals(new SNOMEDCTOntologyFormat());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.AbstractOWLOntologyStorer#storeOntology(org.semanticweb.owlapi.model.OWLOntologyManager, org.semanticweb.owlapi.model.OWLOntology, java.io.Writer, org.semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	@Deprecated
	protected void storeOntology(OWLOntologyManager manager,
			OWLOntology ontology, Writer writer, OWLOntologyFormat format)
			throws OWLOntologyStorageException {
		storeOntology(ontology, writer, format);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.AbstractOWLOntologyStorer#storeOntology(org.semanticweb.owlapi.model.OWLOntology, java.io.Writer, org.semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	protected void storeOntology(OWLOntology ontology, Writer writer,
			OWLOntologyFormat format) throws OWLOntologyStorageException {
		try {
            SNOMEDCTRenderer ren = new SNOMEDCTRenderer((boolean) format.getParameter("labels", true));
            ren.render(ontology, writer);
        } catch (Exception e) {
            throw new OWLOntologyStorageException(e);
        }

	}

}
