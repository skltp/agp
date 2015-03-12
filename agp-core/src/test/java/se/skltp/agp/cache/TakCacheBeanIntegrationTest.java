package se.skltp.agp.cache;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;


public class TakCacheBeanIntegrationTest extends AbstractTestCase {
	
	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("takcache-test-config");
	
	private TakCacheBean testObject;
	private Path testLocalCacheFile;
	
	@Before
	public void init() throws Exception {
		testObject = new TakCacheBean();
		testObject.setTakEndpoint(rb.getString("TAK_TESTSTUB_INBOUND_URL"));
		testObject.setTakLocalCacheFile(rb.getString("TAK_CACHE_FILE_NAME"));
		testObject.setTargetNamespace(rb.getString("SERVICE_INBOUND_NAMESPACE"));
		testObject.setServiceTimeout(Long.valueOf(rb.getString("SERVICE_TIMEOUT_MS")));
		
		testLocalCacheFile = FileSystems.getDefault().getPath(rb.getString("TAK_CACHE_FILE_NAME"));
		
		super.doSetUp();
	}

	@Override
	protected String getConfigResources() {
		return 	"soitoolkit-mule-jms-connector-activemq-embedded.xml," +
				"PingForConfiguration-test-common.xml," +
				"teststub-services/tak-teststub-service.xml," +
				"PingForConfiguration-rivtabp21-service.xml, " +
				"aggregating-services-common.xml";
	}
	
	@Test
	public void testUpdateCacheSuccess() {
		testObject.updateCache();
	}
	
	@After
	public void cleanup() throws Exception {
		Files.deleteIfExists(testLocalCacheFile);
	}

}
