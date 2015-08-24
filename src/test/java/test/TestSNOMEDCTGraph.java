package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTGraph;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTTranslator;

public class TestSNOMEDCTGraph {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		SNOMEDCTGraph.main(new String[] {"src/test/resources/example_expression.sct", "-f", "-s", "/home/daniel/Documents/SCT/SnomedCT_RF2Release_INT_20150131/Snapshot/Terminology/sct2_Concept_Snapshot_INT_20150131.txt", "-o", "test.svg"});
	}

}
