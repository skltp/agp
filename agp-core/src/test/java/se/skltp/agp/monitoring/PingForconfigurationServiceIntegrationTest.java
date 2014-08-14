package se.skltp.agp.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.skltp.agp.test.consumer.PingForConfigurationTestConsumer;

public class PingForconfigurationServiceIntegrationTest extends AbstractTestCase {

	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("pingforconfiguration-test-config");

	private String url = null;

	public PingForconfigurationServiceIntegrationTest() {
		url = rb.getString("PINGFORCONFIGURATION_INBOUND_ENDPOINT");
	}
	
	protected String getConfigResources() {
		return 	"soitoolkit-mule-jms-connector-activemq-embedded.xml," +
				"PingForConfiguration-test-common.xml," +
				"teststub-services/PingForConfiguration-teststub-service.xml," +
				"aggregating-services-common.xml";
	}

	@Test
	public void pingForConfiguration_ok() throws Exception {

		PingForConfigurationTestConsumer consumer = new PingForConfigurationTestConsumer(url);
		PingForConfigurationResponseType response = consumer.callService("logicalAddress");

		assertNotNull(response.getPingDateTime());
		assertEquals("Applikation", response.getConfiguration().get(0).getName());
		assertEquals(rb.getString("APPLICATION_NAME"), response.getConfiguration().get(0).getValue());
		
		//Test correct http headers are passed to producer
		assertEquals(rb.getString("SKLTP_HSA_ID"), PingForConfigurationTestProducerLogger.getLastConsumer());
		assertEquals(rb.getString("VP_INSTANCE_ID"), PingForConfigurationTestProducerLogger.getLastVpInstance());
	}
	
	@Test
	public void pingForConfiguration_error_in_ei() throws Exception {

		PingForConfigurationTestConsumer consumer = new PingForConfigurationTestConsumer(url);
		try {
			consumer.callService(PingForconfigurationTestProducer.ERROR_LOGICAL_ADDRESS);
			fail("Exception excpected");
		} catch (Exception e) {
			assertNotNull(e.getMessage());
			assertTrue(e.getMessage().contains("Error occured trying to use EI database, see application logs for details"));
		}
	}
	
	@Test
	public void pingForConfiguration_timeout_in_ei() throws Exception {

		PingForConfigurationTestConsumer consumer = new PingForConfigurationTestConsumer(url);
		try {
			consumer.callService(PingForconfigurationTestProducer.TIMEOUT_LOGICAL_ADDRESS);
			fail("Exception excpected");
		} catch (Exception e) {
			assertNotNull(e.getMessage());
			assertTrue(e.getMessage().contains("Read timed out"));
		}
	}

}
