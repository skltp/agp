package se.skltp.aggregatingservices.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_MANY_HITS;
import static se.skltp.aggregatingservices.utils.AssertLoggingUtil.LOGGER_NAME_RESP_OUT;
import static se.skltp.aggregatingservices.utils.AssertLoggingUtil.assertRespOut;
import static se.skltp.aggregatingservices.utils.AssertUtil.assertExpectedProcessingStatus;
import static se.skltp.aggregatingservices.utils.AssertUtil.assertExpectedResponse;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder.v4.GetLaboratoryOrderOutcomeResponseType;
import se.skltp.aggregatingservices.AgpApplication;
import se.skltp.aggregatingservices.consumer.ConsumerService;
import se.skltp.aggregatingservices.utils.ExpectedResponse;
import se.skltp.aggregatingservices.utils.ServiceResponse;
import se.skltp.aggregatingservices.utils.TestLogAppender;
import se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum;

@CamelSpringBootTest
@SpringBootTest(classes = AgpApplication.class, properties = {
    "gloo.teststub.serviceTimeout=2000"
  , "getaggregatedlaboratoryorderoutcome.v4.receiveTimeout=3000"
  , "aggregate.timeout=2000"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class AA_AggregateTimeoutIT {

  @Autowired
  ConsumerService consumerService;

  @Autowired
  TestLogAppender testLogAppender;

  @Test
  public void testNoProducersAnswersBeforeTimeout() throws Exception {
    ExpectedResponse expectedResponse = new ExpectedResponse();
    expectedResponse.add("HSA-ID-1", 1, StatusCodeEnum.DATA_FROM_SOURCE, "");
    expectedResponse.add("HSA-ID-2", 2, StatusCodeEnum.DATA_FROM_SOURCE, "");
    expectedResponse.add("HSA-ID-3", 0, StatusCodeEnum.NO_DATA_SYNCH_FAILED, "(?s).*Unknown error.*");

    final ServiceResponse<GetLaboratoryOrderOutcomeResponseType> response = consumerService.callService(TEST_RR_ID_MANY_HITS);

    assertExpectedResponse(response, expectedResponse, TEST_RR_ID_MANY_HITS);
    assertExpectedProcessingStatus(response.getProcessingStatus(), expectedResponse);
    assertEquals(1, testLogAppender.getNumEvents(LOGGER_NAME_RESP_OUT));
    assertRespOut(testLogAppender, expectedResponse);
  }
}
