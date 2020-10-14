package se.skltp.aggregatingservices.integrationtests;

import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS;
import static se.skltp.aggregatingservices.utils.AssertLoggingUtil.assertLogging;
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
import se.skltp.aggregatingservices.route.ProducerBaseRoute;
import se.skltp.aggregatingservices.utils.ExpectedResponse;
import se.skltp.aggregatingservices.utils.ServiceResponse;
import se.skltp.aggregatingservices.utils.TestLogAppender;
import se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum;


@CamelSpringBootTest
@SpringBootTest(classes = AgpApplication.class, properties = {
    "getaggregatedlaboratoryorderoutcome.v4.outboundServiceURL=http://localhost:8087/faulty"} )
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class AA_FullServiceFaultyProducerIT {
  @Autowired
  ProducerBaseRoute producerBaseRoute;

  @Autowired
  ConsumerService consumerService;

  @Autowired
  TestLogAppender testLogAppender;

  //
  // Call service that respond with unknown answer
  //
  @Test
  public void testProducerRespondWithFaultyAnswer() throws Exception {
    // A OK response with no engagements expected
    ExpectedResponse expectedResponse = new ExpectedResponse();
    expectedResponse.add("HSA-ID-4", 0, StatusCodeEnum.NO_DATA_SYNCH_FAILED, "");
    expectedResponse.add("HSA-ID-5", 0, StatusCodeEnum.NO_DATA_SYNCH_FAILED, "");
    expectedResponse.add("HSA-ID-6", 0, StatusCodeEnum.NO_DATA_SYNCH_FAILED, "");

    final ServiceResponse<GetLaboratoryOrderOutcomeResponseType> response = consumerService
        .callService(TEST_RR_ID_MANY_HITS_NO_ERRORS);

    assertExpectedResponse(response, expectedResponse, TEST_RR_ID_MANY_HITS_NO_ERRORS);
    assertExpectedProcessingStatus(response.getProcessingStatus(), expectedResponse);
    assertLogging(testLogAppender, expectedResponse, false);  }

}
