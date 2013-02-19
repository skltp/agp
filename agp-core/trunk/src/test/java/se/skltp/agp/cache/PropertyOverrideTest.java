package se.skltp.agp.cache;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

public class PropertyOverrideTest {

	
	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("test-config", "test-config-override");

	public static final String PROPERTY_NO_OVERRIDE = rb.getString("PROPERTY_NO_OVERRIDE");
	public static final String PROPERTY_OVERRIDE = rb.getString("PROPERTY_OVERRIDE");

	@Test
	public void testPropertyOverride() throws Exception {
		assertTrue(PROPERTY_NO_OVERRIDE.equals("ORIGINAL PROPERTY 1"));
		assertTrue(PROPERTY_OVERRIDE.equals("OVERRIDE PROPERTY 2"));
		assertFalse(PROPERTY_OVERRIDE.equals("ORIGINAL PROPERTY 2"));
	}
}
