package se.skltp.aggregatingservices.utils;

import riv.clinicalprocess.healthcond.actoutcome._4.LaboratoryOrderOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder.v4.GetLaboratoryOrderOutcomeResponseType;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusRecordType;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusType;
import se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum;

import static org.junit.jupiter.api.Assertions.*;

public class AssertUtil {


  // Utility class
  private AssertUtil() {
  }

  public static void assertExpectedResponse(ServiceResponse<GetLaboratoryOrderOutcomeResponseType> response,
      ExpectedResponse expectedResponse,
      String patientId) {

    assertEquals(expectedResponse.getResponseCode(), response.getResponseCode(), "Not expected response code");

    assertEquals(expectedResponse.numResponses(),response.getObject().getLaboratoryOrderOutcome().size(),
            "GetLaboratoryOrderOutcome does not have expected size");

    for (LaboratoryOrderOutcomeType responseElement : response.getObject().getLaboratoryOrderOutcome()) {
      String systemId = responseElement.getLaboratoryOrderOutcomeHeader().getSource().getSystemId().getRoot();
      assertTrue(expectedResponse.contains(systemId), String.format("%s wasn't expected in response", systemId));
      assertEquals(patientId,
          responseElement.getLaboratoryOrderOutcomeHeader().getAccessControlHeader().getPatient().getId().get(0).getRoot());
    }

  }


  public static void assertExpectedProcessingStatus(ProcessingStatusType processingStatusType,
      ExpectedResponse expectedResponse) {

    assertEquals(expectedResponse.numProducers(), processingStatusType.getProcessingStatusList().size(),
            "ProcessingStatus does not have expected size");

    for (ProcessingStatusRecordType processingStatus : processingStatusType.getProcessingStatusList()) {
      String logicalAddress = processingStatus.getLogicalAddress();

      assertTrue(expectedResponse.contains(logicalAddress),
              String.format("%s wasn't expected in ProcessingStatus", logicalAddress));

      assertEquals(expectedResponse.getStatusCode(logicalAddress), processingStatus.getStatusCode());
      if (processingStatus.getStatusCode() == StatusCodeEnum.NO_DATA_SYNCH_FAILED) {
        final String errTxtPart = expectedResponse.getErrTxtPart(logicalAddress);
        String errTxt = processingStatus.getLastUnsuccessfulSynchError().getText();
        String errCode = processingStatus.getLastUnsuccessfulSynchError().getCode();

        if(errTxtPart!=null && !errTxtPart.isEmpty()) {
          assertTrue(errTxt.matches(errTxtPart),
                  String.format("Error txt: %s\n Does not contain:\n  %s ", errTxt, errTxtPart));
        }

        assertNotNull(errCode, "errorCode should not be null");
      }

    }
  }

}
