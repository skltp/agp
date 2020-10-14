package se.skltp.aggregatingservices.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_2;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_MANY_HITS;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS;

import java.util.List;
import org.apache.cxf.message.MessageContentsList;
import org.junit.jupiter.api.Test;
import se.skltp.aggregatingservices.api.AgpServiceFactory;
import se.skltp.aggregatingservices.configuration.AgpServiceConfiguration;
import se.skltp.aggregatingservices.data.FindContentTestData;
import se.skltp.aggregatingservices.data.TestDataGenerator;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;

public abstract class CreateRequestListTest {

  protected FindContentTestData eiResponseDataHelper  = new FindContentTestData();
  protected TestDataGenerator testDataGenerator;
  protected AgpServiceFactory agpServiceFactory;

  public static final String LOGISK_ADRESS = "logiskAdress";

  public CreateRequestListTest(TestDataGenerator testDataGenerator, AgpServiceFactory agpServiceFactory, AgpServiceConfiguration configuration){
    this.testDataGenerator = testDataGenerator;
    this.agpServiceFactory = agpServiceFactory;
    this.agpServiceFactory.setAgpServiceConfiguration(configuration);
  }

  @Test
  public void testCreateRequestListAllProducers(){
    MessageContentsList messageContentsList = TestDataUtil
        .createRequest(LOGISK_ADRESS, testDataGenerator.createRequest(TEST_RR_ID_MANY_HITS_NO_ERRORS, null));
    FindContentResponseType eiResponse = eiResponseDataHelper.getResponseForPatient(TEST_RR_ID_MANY_HITS_NO_ERRORS);


    List<MessageContentsList> requestList = agpServiceFactory.createRequestList(messageContentsList, eiResponse);
    assertEquals(3, requestList.size());
  }

  @Test
  public void testCreateRequestListSomeFilteredBySourceSystem(){
    MessageContentsList messageContentsList = TestDataUtil.createRequest(LOGISK_ADRESS, testDataGenerator.createRequest(
        TEST_RR_ID_MANY_HITS, TEST_LOGICAL_ADDRESS_2));
    FindContentResponseType eiResponse = eiResponseDataHelper.getResponseForPatient(TEST_RR_ID_MANY_HITS);


    List<MessageContentsList> requestList = agpServiceFactory.createRequestList(messageContentsList, eiResponse);

    assertEquals(1, requestList.size());
  }

  @Test
  public void testCreateRequestListAllFilteredBySourceSystem(){
    MessageContentsList messageContentsList = TestDataUtil.createRequest(LOGISK_ADRESS, testDataGenerator.createRequest(
        TEST_RR_ID_MANY_HITS_NO_ERRORS, TEST_LOGICAL_ADDRESS_2));

    FindContentResponseType eiResponse = eiResponseDataHelper.getResponseForPatient(TEST_RR_ID_MANY_HITS_NO_ERRORS);

    List<MessageContentsList> requestList = agpServiceFactory.createRequestList(messageContentsList, eiResponse);

    assertEquals(0, requestList.size());
  }

}
