package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTSignatureGenerator;

public class TestSNOMEDCTSignatureGenerator {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMain() {
		SNOMEDCTSignatureGenerator.main(new String[] {"-f", "|", "src/test/resources/example_expression.sct"});
	}

}
