package se.skltp.aggregatingservices.integrationtests;

import static se.skltp.aggregatingservices.data.TestDataDefines.SAMPLE_SENDER_ID;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_1;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import se.skltp.aggregatingservices.AgpApplication;
import se.skltp.aggregatingservices.config.EiConfig;
import se.skltp.aggregatingservices.config.VpConfig;
import se.skltp.aggregatingservices.constants.AgpHeaders;
import se.skltp.aggregatingservices.consumer.ConsumerService;
import se.skltp.aggregatingservices.route.FindContentStubRoute;
import se.skltp.aggregatingservices.route.ProducerBaseRoute;

@CamelSpringBootTest
@SpringBootTest(classes = {AgpApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FullServiceHeadersIT {

  @Autowired
  ProducerBaseRoute producerBaseRoute;

  @Autowired
  FindContentStubRoute findContentStubRoute;

  @Autowired
  ConsumerService consumerService;

  @Autowired
  VpConfig vpConfig;

  @Autowired
  EiConfig eiConfig;

  @BeforeEach
  public void beforeTest() {
    producerBaseRoute.getMock().reset();
    findContentStubRoute.getMock().reset();
  }

  @Test
  public void senderIdForwardedToProducer() throws Exception {

    final MockEndpoint mock = producerBaseRoute.getMock();
    mock.expectedHeaderValuesReceivedInAnyOrder(AgpHeaders.X_VP_SENDER_ID, SAMPLE_SENDER_ID, SAMPLE_SENDER_ID, SAMPLE_SENDER_ID);
    mock.expectedMessageCount(3);

    consumerService.callService(TEST_RR_ID_MANY_HITS_NO_ERRORS);

    mock.assertIsSatisfied();
  }

  @Test
  public void originalConsumerIdForwardedToProducer() throws Exception {

    final MockEndpoint mock = producerBaseRoute.getMock();
    mock.expectedHeaderValuesReceivedInAnyOrder(AgpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, SAMPLE_SENDER_ID, SAMPLE_SENDER_ID,
        SAMPLE_SENDER_ID);
    mock.expectedMessageCount(3);

    consumerService.callService(TEST_RR_ID_MANY_HITS_NO_ERRORS);

    mock.assertIsSatisfied();
  }

  @Test
  public void intanceIdSentToProducer() throws Exception {

    final MockEndpoint mock = producerBaseRoute.getMock();
    mock.expectedHeaderReceived(AgpHeaders.X_VP_INSTANCE_ID, vpConfig.getInstanceId());
    mock.expectedMessageCount(3);

    consumerService.callService(TEST_RR_ID_MANY_HITS_NO_ERRORS);

    mock.assertIsSatisfied();
  }


  @Test
  public void correlationIdForwardedToProducer() throws Exception {

    final MockEndpoint mock = producerBaseRoute.getMock();
    mock.expectedHeaderReceived(AgpHeaders.X_SKLTP_CORRELATION_ID, "test-corr-id");
    mock.expectedMessageCount(3);

    consumerService.callService(TEST_RR_ID_MANY_HITS_NO_ERRORS);

    mock.assertIsSatisfied();
  }

  @Test
  public void correlationIdForwardedToEI() throws Exception {

    final MockEndpoint mock = findContentStubRoute.getMock();
    mock.expectedHeaderReceived(AgpHeaders.X_SKLTP_CORRELATION_ID, "test-corr-id");
    mock.expectedMessageCount(1);

    consumerService.callService(TEST_RR_ID_MANY_HITS_NO_ERRORS);

    mock.assertIsSatisfied();
  }

  @Test
  public void instanceIdSentToEI() throws Exception {

    final MockEndpoint mock = findContentStubRoute.getMock();
    mock.expectedHeaderReceived(AgpHeaders.X_VP_INSTANCE_ID, vpConfig.getInstanceId());
    mock.expectedMessageCount(1);

    consumerService.callService(TEST_RR_ID_MANY_HITS_NO_ERRORS);

    mock.assertIsSatisfied();
  }

  @Test
  public void platformIdUsedAsSenderIdToEI() throws Exception {

    final MockEndpoint mock = findContentStubRoute.getMock();
    mock.expectedHeaderReceived(AgpHeaders.X_VP_SENDER_ID, eiConfig.getSenderId());
    mock.expectedMessageCount(1);

    consumerService.callService(TEST_RR_ID_MANY_HITS_NO_ERRORS);

    mock.assertIsSatisfied();
  }


  @Test
  public void createHttpHeadersWithXRivta() throws InterruptedException {
    final MockEndpoint mock = producerBaseRoute.getMock();
    mock.expectedHeaderReceived("x-rivta-test1", "test1");
    mock.expectedHeaderReceived("x-rivta-TEST2", "TEST2");
    mock.expectedHeaderReceived("x-rivta-123", "123");

    Map<String, Object> headers = new HashMap<>();
    headers.put("x-rivta-test1", "test1");
    headers.put("x-rivta-TEST2", "TEST2");
    headers.put("x-rivta-123", "123");

    consumerService
        .callService(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_MANY_HITS_NO_ERRORS, headers);
    mock.assertIsSatisfied();
  }
}
