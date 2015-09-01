package se.skltp.agp.service.transformers;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.soitoolkit.commons.mule.util.MiscUtil;

public class CreateResponseListTransformerTest {

	@Ignore("needs fixing")
	@Test
	public void testTransformer_ok() throws Exception {
		
		// Specify input and expected result 

		String input          = MiscUtil.readFileAsString("src/test/resources/testfiles/tidbokning/response-expected-result.xml"); // No transformation is done by default so use expected also as input...

		String expectedResult = MiscUtil.readFileAsString("src/test/resources/testfiles/tidbokning/response-expected-result.xml");
		
		// Create the transformer under test and let it perform the transformation
		CreateResponseListTransformer transformer = new CreateResponseListTransformer();
		//String result = (String)transformer.pojoTransform(null, input, "UTF-8");

		// Compare the result to the expected value
		//assertEquals(expectedResult, result);
	}
}