package se.skltp.aggregatingservices.integrationtests;

import static org.apache.camel.test.junit5.TestSupport.assertStringContains;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_1;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_ONE_FORMAT_ERROR;
import static se.skltp.aggregatingservices.utils.AssertUtil.assertExpectedProcessingStatus;
import static se.skltp.aggregatingservices.utils.AssertUtil.assertExpectedResponse;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.cxf.binding.soap.SoapFault;
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
    "getaggregatedlaboratoryorderoutcome.v4.enableSchemaValidation=true",
    "validate.soapAction=true"
   })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class AA_ValidationErrorIT {

  @Autowired
  ConsumerService consumerService;

  @Autowired
  TestLogAppender testLogAppender;

  //
  // TC8 - One ok response, second response contains field with format error
  //      Should fail Ok when schema validation is enabled
  //
  @Test
  public void testFormatErrorNotAccepted() throws Exception {
    ExpectedResponse expectedResponse = new ExpectedResponse();
    expectedResponse.add("HSA-ID-4", 1, StatusCodeEnum.DATA_FROM_SOURCE, "");
    expectedResponse.add("HSA-ID-5", 0, StatusCodeEnum.NO_DATA_SYNCH_FAILED, "(?s).*: (Value|Värdet) '1895'.*");

    final ServiceResponse<GetLaboratoryOrderOutcomeResponseType> response = consumerService
        .callService(TEST_RR_ID_ONE_FORMAT_ERROR);

    assertExpectedResponse(response, expectedResponse, TEST_RR_ID_ONE_FORMAT_ERROR);
    assertExpectedProcessingStatus(response.getProcessingStatus(), expectedResponse);
  }

  //
  // Call service with wrong contract should give a soap fault
  //
  @Test
  public void wrongSoapActionShouldGiveError() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put("SoapAction", "Unknown-action.is.wrong");
    final ServiceResponse<GetLaboratoryOrderOutcomeResponseType> response = consumerService
        .callService(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_MANY_HITS_NO_ERRORS, headers);

    assertEquals("Not expected response code", 500, response.getResponseCode());

    final SoapFault soapFault = response.getSoapFault();
    assertNotNull("Expected a SoapFault", soapFault);
    assertStringContains(soapFault.getReason(), "The given SOAPAction Unknown-action.is.wrong does not match an operation");

  }
}
