/**
 * 
 */
package test;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTTranslator;

/**
 * @author daniel
 *
 */
public class TestSNOMEDCTTranslator {

	/**
	 * Test method for {@link se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTTranslator#main(java.lang.String[])}.
	 * @throws OWLException 
	 */
	@Test
	public void testMain() throws OWLException {
		SNOMEDCTTranslator.main(new String[] {"-l", "-f", "sct", "-n", "distribution", /*"-s", "src/test/resources/hernia_repair_module.owl",*/ "src/test/resources/hernia_repair_module.owl"});
	}

}
