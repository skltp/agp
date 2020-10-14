package se.skltp.aggregatingservices.tests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNull;

import org.apache.cxf.message.MessageContentsList;
import org.junit.jupiter.api.Test;
import se.skltp.aggregatingservices.api.AgpServiceFactory;
import se.skltp.aggregatingservices.configuration.AgpServiceConfiguration;
import se.skltp.aggregatingservices.data.TestDataGenerator;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentType;

public abstract class CreateFindContentTest {

  private static final String PATIENT_ID = "121212121212";

  protected AgpServiceFactory agpServiceFactory;
  protected AgpServiceConfiguration configuration;

  protected TestDataGenerator testDataGenerator;

  public CreateFindContentTest(TestDataGenerator testDataGenerator, AgpServiceFactory agpServiceFactory, AgpServiceConfiguration configuration){
    this.testDataGenerator = testDataGenerator;
    this.agpServiceFactory = agpServiceFactory;
    this.agpServiceFactory.setAgpServiceConfiguration(configuration);
    this.configuration = configuration;
  }


  @Test
  public void testCreateFindContent(){
    MessageContentsList messageContentsList = TestDataUtil.createRequest("logiskAdress", testDataGenerator
        .createRequest(PATIENT_ID, null));

    FindContentType findContentRequest = agpServiceFactory.createFindContent(messageContentsList);

    // If configuration is a list of categories the request.getCategorization should be 'null'
    // to get all categories from EI
    final String eiCategorization = configuration.getEiCategorization();
    if(eiCategorization != null && eiCategorization.contains(",")){
      assertNull("Expected category==null since it's a list of categories congfigured", findContentRequest.getCategorization());
    } else {
      assertEquals(eiCategorization, findContentRequest.getCategorization());
    }

    assertEquals(configuration.getEiServiceDomain(), findContentRequest.getServiceDomain());
    assertEquals(PATIENT_ID, findContentRequest.getRegisteredResidentIdentification());
  }

}
