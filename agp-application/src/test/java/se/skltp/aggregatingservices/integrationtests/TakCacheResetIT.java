package se.skltp.aggregatingservices.integrationtests;

import static org.apache.camel.test.junit5.TestSupport.assertStringContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.skltp.aggregatingservices.data.TestDataDefines.SAMPLE_SENDER_ID;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_1;

import java.util.Arrays;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.skltp.aggregatingservices.AgpApplication;
import se.skltp.aggregatingservices.data.VagvalsInfoTestData;
import se.skltp.aggregatingservices.service.TakCacheService;
import se.skltp.takcache.TakCacheLog;

@CamelSpringBootTest
@SpringBootTest(classes = {AgpApplication.class})
public class TakCacheResetIT {

  @Produce
  protected ProducerTemplate template;

  @Autowired
  VagvalsInfoTestData vagvalsInfoTestData;

  @Autowired
  TakCacheService takCacheService;

  @BeforeEach
  public void setUp(){
    // See agp-teststub/readme.md for information about the TAK data generated.
    vagvalsInfoTestData.resetTestData();
    vagvalsInfoTestData.generateDefaultTestData("test.namespace.1");
    vagvalsInfoTestData.generateDefaultTestData("test.namespace.2");
    takCacheService.setTakContracts(Arrays.asList("test.namespace.1","test.namespace.2"));

  }

  @AfterEach
  public void tearDown(){
    takCacheService.resetTakContracts();
    takCacheService.refresh();
  }

  @Test
  public void resetCacheShouldWork() throws Exception {

    // Call reset cache route
    String result =   template.requestBody("{{reset.cache.url}}", "body", String.class);
    assertStringContains(result, "Init done, was successful: true");

    assertEquals( true, takCacheService.isInitalized() );

    TakCacheLog takCacheLog = takCacheService.getLastRefreshLog();
    assertEquals( 22, takCacheLog.getNumberBehorigheter());
  }

  @Test
  public void testAuthorization() throws Exception {

    // Call reset cache route
    String result =   template.requestBody("{{reset.cache.url}}", "body", String.class);

    // See agp-teststub/readme.md for information about the TAK data generated.
    assertTrue(takCacheService.isAuthorized(SAMPLE_SENDER_ID, "test.namespace.1", TEST_LOGICAL_ADDRESS_1));
    assertTrue(takCacheService.isAuthorized(SAMPLE_SENDER_ID, "test.namespace.2", TEST_LOGICAL_ADDRESS_1));
    assertFalse(takCacheService.isAuthorized(SAMPLE_SENDER_ID, "test.namespace.3", TEST_LOGICAL_ADDRESS_1));
 }

}
