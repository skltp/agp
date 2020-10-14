package se.skltp.aggregatingservices.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import riv.clinicalprocess.healthcond.actoutcome._4.LaboratoryOrderOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder.v4.GetLaboratoryOrderOutcomeResponseType;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusRecordType;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusType;
import se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum;

public class AssertUtil {


  // Utility class
  private AssertUtil() {
  }

  public static void assertExpectedResponse(ServiceResponse<GetLaboratoryOrderOutcomeResponseType> response,
      ExpectedResponse expectedResponse,
      String patientId) {

    assertEquals("Not expected response code", expectedResponse.getResponseCode(), response.getResponseCode());

    assertEquals("GetLaboratoryOrderOutcome does not have expected size", expectedResponse.numResponses(),
        response.getObject().getLaboratoryOrderOutcome().size());

    for (LaboratoryOrderOutcomeType responseElement : response.getObject().getLaboratoryOrderOutcome()) {
      String systemId = responseElement.getLaboratoryOrderOutcomeHeader().getSource().getSystemId().getRoot();
      assertTrue(String.format("%s wasn't expected in response", systemId), expectedResponse.contains(systemId));
      assertEquals(patientId,
          responseElement.getLaboratoryOrderOutcomeHeader().getAccessControlHeader().getPatient().getId().get(0).getRoot());
    }

  }


  public static void assertExpectedProcessingStatus(ProcessingStatusType processingStatusType,
      ExpectedResponse expectedResponse) {

    assertEquals("ProcessingStatus does not have expected size", expectedResponse.numProducers(),
        processingStatusType.getProcessingStatusList().size());

    for (ProcessingStatusRecordType processingStatus : processingStatusType.getProcessingStatusList()) {
      String logicalAddress = processingStatus.getLogicalAddress();

      assertTrue(String.format("%s wasn't expected in ProcessingStatus", logicalAddress),
          expectedResponse.contains(logicalAddress));

      assertEquals(expectedResponse.getStatusCode(logicalAddress), processingStatus.getStatusCode());
      if (processingStatus.getStatusCode() == StatusCodeEnum.NO_DATA_SYNCH_FAILED) {
        final String errTxtPart = expectedResponse.getErrTxtPart(logicalAddress);
        String errTxt = processingStatus.getLastUnsuccessfulSynchError().getText();
        String errCode = processingStatus.getLastUnsuccessfulSynchError().getCode();

        if(errTxtPart!=null && !errTxtPart.isEmpty()) {
          assertTrue(String.format("Error txt: %s\n Does not contain:\n  %s ", errTxt, errTxtPart),
              errTxt.matches(errTxtPart));
        }

        assertNotNull("errorCode should not be null", errCode);
      }

    }
  }

}
