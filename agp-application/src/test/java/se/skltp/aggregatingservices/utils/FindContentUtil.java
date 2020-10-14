package se.skltp.aggregatingservices.utils;

import org.apache.cxf.message.MessageContentsList;
import se.skltp.aggregatingservices.data.FindContentTestData;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentType;

public class FindContentUtil {

  static FindContentTestData findContentTestData = new FindContentTestData();

  public static FindContentResponseType createFindContentResponse(String patient) {
    findContentTestData.generateResponseMap();
    return findContentTestData.getResponseForPatient(patient);
  }

  public static FindContentType createFindContentRequest(String patient) {
    FindContentType fc = new FindContentType();
    fc.setRegisteredResidentIdentification(patient);
    fc.setServiceDomain("serviceDomain");
    fc.setCategorization("categorization");
    return fc;
  }

  public static MessageContentsList createRequestMessageContentsList(String logicalAddress, String patient) {
    MessageContentsList messageContentsList = new MessageContentsList();
    messageContentsList.add(logicalAddress);
    messageContentsList.add(createFindContentRequest(patient));
    return messageContentsList;
  }

  public static MessageContentsList createMessageContentsList(String patient) {
    MessageContentsList messageContentsList = new MessageContentsList();
    messageContentsList.set(0,  createFindContentResponse(patient));
    return messageContentsList;
  }

}
