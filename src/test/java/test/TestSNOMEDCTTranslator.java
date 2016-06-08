/**
 *
 */
package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
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
	 * @throws IOException
	 */
	@Test
	public void testMain() throws OWLException, IOException {
//		SNOMEDCTTranslator.main(new String[] {"-l", "-f", "owlf", "-n", "stated", "/home/danka74/Documents/Projekt/Snomed/ordorsak/ordorsak.sct"});
		
		SNOMEDCTTranslator.main(new String[] {"-l", "-f", "sct", "-n", "stated", "src/test/resources/hernia_repair_module.owl"});
		SNOMEDCTTranslator.main(new String[] {"-f", "owlf", "src/test/resources/hernia_repair_module_sct_stated.owl"});
		File file1 = new File("src/test/resources/hernia_repair_module.owl");
		File file2 = new File("src/test/resources/hernia_repair_module_sct_stated_owlf_stated.owl");
		assertTrue("Files differ!", FileUtils.contentEquals(file1, file2));
	}

//	@Test
//	public void testMain2() throws OWLException, IOException {
//		SNOMEDCTTranslator.main(new String[] {"-l", "-f", "owlf", "-n", "flat", "src/test/resources/obsTechPreviewExtract.owl"});
//	}
}
