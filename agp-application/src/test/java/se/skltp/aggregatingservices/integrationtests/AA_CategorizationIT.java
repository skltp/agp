package se.skltp.aggregatingservices.integrationtests;

import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_THREE_CATEGORIES;
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
import se.skltp.aggregatingservices.utils.ExpectedResponse;
import se.skltp.aggregatingservices.utils.ServiceResponse;
import se.skltp.aggregatingservices.utils.TestLogAppender;
import se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum;

@CamelSpringBootTest
@SpringBootTest(classes = AgpApplication.class, properties = {
     "getaggregatedlaboratoryorderoutcome.v4.eiCategorization=cat2,cat3"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class AA_CategorizationIT {

  @Autowired
  ConsumerService consumerService;

  @Autowired
  TestLogAppender testLogAppender;

  //
  // TC9 - Three engagements with different categorizations,
  //    If list of categorizations configured not matching categorizations should be fitltered
  //
  @Test
  public void testThreeDifferentEiCategoriziesButOnlyTwoAllowed() throws Exception {
    ExpectedResponse expectedResponse = new ExpectedResponse();
    expectedResponse.add("HSA-ID-5", 1, StatusCodeEnum.DATA_FROM_SOURCE, "");
    expectedResponse.add("HSA-ID-6", 1, StatusCodeEnum.DATA_FROM_SOURCE, "");

    final ServiceResponse<GetLaboratoryOrderOutcomeResponseType> response = consumerService
        .callService(TEST_RR_ID_THREE_CATEGORIES);

    assertExpectedResponse(response, expectedResponse, TEST_RR_ID_THREE_CATEGORIES);
    assertExpectedProcessingStatus(response.getProcessingStatus(), expectedResponse);
    assertLogging(testLogAppender, expectedResponse);
  }

}
