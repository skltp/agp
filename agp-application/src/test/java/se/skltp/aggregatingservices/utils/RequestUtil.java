package se.skltp.aggregatingservices.utils;

import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_1;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS;

import org.apache.cxf.message.MessageContentsList;
import riv.clinicalprocess.healthcond.actoutcome._4.PersonIdType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder.v4.GetLaboratoryOrderOutcomeType;

public class RequestUtil {

  public static MessageContentsList createTestMessageContentsList() {
    return createTestMessageContentsList(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_MANY_HITS_NO_ERRORS);
  }

  public static MessageContentsList createTestMessageContentsList(String logicalAddress, String patientId) {

    GetLaboratoryOrderOutcomeType labOrderOutcome = createLabOrderOutComeRequest(patientId);

    MessageContentsList messageContentsList = new MessageContentsList();
    messageContentsList.set(0, logicalAddress);
    messageContentsList.set(1, labOrderOutcome);
    return messageContentsList;
  }


  private static GetLaboratoryOrderOutcomeType createLabOrderOutComeRequest(String patientId) {
    GetLaboratoryOrderOutcomeType labOrderOutcome = new GetLaboratoryOrderOutcomeType();

    PersonIdType personIdType = new PersonIdType();
    personIdType.setId(patientId);
    personIdType.setType("1.2.752.129.2.1.3.1");

    labOrderOutcome.setPatientId(personIdType);
    labOrderOutcome.setSourceSystemHSAId(null);

    return labOrderOutcome;
  }
}
