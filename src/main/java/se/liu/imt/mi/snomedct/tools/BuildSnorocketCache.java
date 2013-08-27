package se.liu.imt.mi.snomedct.tools;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

//import au.csiro.snorocket.core.Snorocket;
//import au.csiro.snorocket.owlapi3.SnorocketReasoner;



public class BuildSnorocketCache {

	/**
	 * @param args
	 * @throws OWLOntologyCreationException
	 */
	public static void main(String[] args) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(IRI
						.create("file:///home/danka/SnomedCT_INT_20110731/OtherResources/StatedRelationships/res_StatedOWL_Core_INT_20110731.owl"));
//		Snorocket.DEBUGGING = true;
//		SnorocketReasoner reasoner = new SnorocketReasoner(manager, true);
	}

}
