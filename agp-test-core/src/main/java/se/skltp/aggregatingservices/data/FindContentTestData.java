package se.skltp.aggregatingservices.data;

import static se.skltp.aggregatingservices.data.TestDataDefines.CATEGORY_DEFAULT;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_BO_ID_FAULT_INVALID_ID;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.v1.EngagementType;

@Log4j2
@Service
public class FindContentTestData {

  public static final String LOG_MSG_ADDED_ITEM = "### Engagemengsindex add {} items to the index for resident {}";
  private static final Map<String, FindContentResponseType> FINDCONTENT_RESPONSE_MAP = new HashMap<>();

  public FindContentTestData() {
    generateResponseMap();
  }

  public FindContentResponseType getResponseForPatient(String patientId) {
    final FindContentResponseType response = FINDCONTENT_RESPONSE_MAP.get(patientId);
    return response == null ? new FindContentResponseType() :response;
  }

  public void generateResponseMap() {

    //
    // TC1 - Patient with three bookings spread over three logical-addresses, all with fast response times
    //
    FindContentResponseType response = new FindContentResponseType();
    response.getEngagement().add(
        createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_4, TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_1,
            TestDataDefines.TEST_DATE_MANY_HITS_1));
    response.getEngagement().add(
        createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_5, TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_2,
            TestDataDefines.TEST_DATE_MANY_HITS_2));
    response.getEngagement().add(
        createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_6, TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_3,
            TestDataDefines.TEST_DATE_MANY_HITS_3));
    FINDCONTENT_RESPONSE_MAP.put(TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS, response);
    log.info(LOG_MSG_ADDED_ITEM, response.getEngagement().size(),
        TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS);

    //
    // TC3 - Patient with two bookings in Engagement Index - second booking is missing in producer
    //
    response = new FindContentResponseType();
    response.getEngagement()
        .add(createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_1, TestDataDefines.TEST_RR_ID_ONE_HIT,
            TestDataDefines.TEST_BO_ID_ONE_HIT, TestDataDefines.TEST_DATE_ONE_HIT));
    response.getEngagement()
        .add(createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_2, TestDataDefines.TEST_RR_ID_ONE_HIT,
            TestDataDefines.TEST_BO_ID_ONE_HIT, TestDataDefines.TEST_DATE_ONE_HIT));
    FINDCONTENT_RESPONSE_MAP.put(TestDataDefines.TEST_RR_ID_ONE_HIT, response);
    log.info(LOG_MSG_ADDED_ITEM, response.getEngagement().size(),
        TestDataDefines.TEST_RR_ID_ONE_HIT);

    //
    // TC4 - Patient with four bookings spread over three logical-addresses
    //
    response = new FindContentResponseType();
    response.getEngagement()
        .add(createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_1, TestDataDefines.TEST_RR_ID_MANY_HITS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_1,
            TestDataDefines.TEST_DATE_MANY_HITS_1));
    response.getEngagement()
        .add(createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_2, TestDataDefines.TEST_RR_ID_MANY_HITS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_2,
            TestDataDefines.TEST_DATE_MANY_HITS_2));
    response.getEngagement()
        .add(createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_2, TestDataDefines.TEST_RR_ID_MANY_HITS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_3,
            TestDataDefines.TEST_DATE_MANY_HITS_3));
    response.getEngagement()
        .add(createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_3, TestDataDefines.TEST_RR_ID_MANY_HITS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_4,
            TestDataDefines.TEST_DATE_MANY_HITS_4));
    FINDCONTENT_RESPONSE_MAP.put(TestDataDefines.TEST_RR_ID_MANY_HITS, response);
    log.info(LOG_MSG_ADDED_ITEM, response.getEngagement().size(),
        TestDataDefines.TEST_RR_ID_MANY_HITS);

    //
    // TC5 - Patient that causes an exception in the source system
    //
    response = new FindContentResponseType();
    response.getEngagement().add(createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_1,
        TestDataDefines.TEST_RR_ID_FAULT_INVALID_ID, TEST_BO_ID_FAULT_INVALID_ID,
        TestDataDefines.TEST_DATE_FAULT_INVALID_ID));
    FINDCONTENT_RESPONSE_MAP.put(TestDataDefines.TEST_RR_ID_FAULT_INVALID_ID, response);
    log.info(LOG_MSG_ADDED_ITEM, response.getEngagement().size(),
        TestDataDefines.TEST_RR_ID_FAULT_INVALID_ID);

    //
    // TC6 - Patient that causes an exception in the source system
    //
    response = new FindContentResponseType();
    response.getEngagement().add(
        createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_7, TestDataDefines.TEST_RR_ID_EJ_SAMVERKAN_I_TAK,
            TestDataDefines.TEST_BO_ID_EJ_SAMVERKAN_I_TAK,
            TestDataDefines.TEST_DATE_EJ_SAMVERKAN_I_TAK));
    FINDCONTENT_RESPONSE_MAP.put(TestDataDefines.TEST_RR_ID_EJ_SAMVERKAN_I_TAK, response);
    log.info(LOG_MSG_ADDED_ITEM, response.getEngagement().size(),
        TestDataDefines.TEST_RR_ID_EJ_SAMVERKAN_I_TAK);
    //
    // TC7 - Patient with one booking
    //
    response = new FindContentResponseType();
    response.getEngagement().add(
        createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_CHILD, TestDataDefines.TEST_RR_ID_TRADKLATTRING,
            TestDataDefines.TEST_BO_ID_TRADKLATTRING,
            TestDataDefines.TEST_DATE_TRADKLATTRING));
    FINDCONTENT_RESPONSE_MAP.put(TestDataDefines.TEST_RR_ID_TRADKLATTRING, response);
    log.info(LOG_MSG_ADDED_ITEM, response.getEngagement().size(),
        TestDataDefines.TEST_RR_ID_TRADKLATTRING);  //

    // TC8 - Patient with one booking
    //
    response = new FindContentResponseType();
    response.getEngagement().add(
        createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_4, TestDataDefines.TEST_RR_ID_ONE_FORMAT_ERROR,
            TestDataDefines.TEST_BO_ID_MANY_HITS_1,
            TestDataDefines.TEST_DATE_MANY_HITS_1));
    response.getEngagement().add(
        createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_5, TestDataDefines.TEST_RR_ID_ONE_FORMAT_ERROR,
            TestDataDefines.TEST_BO_ID_MANY_HITS_2,
            TestDataDefines.TEST_DATE_MANY_HITS_2));
    FINDCONTENT_RESPONSE_MAP.put(TestDataDefines.TEST_RR_ID_ONE_FORMAT_ERROR, response);
    log.info(LOG_MSG_ADDED_ITEM, response.getEngagement().size(),
        TestDataDefines.TEST_RR_ID_ONE_FORMAT_ERROR);

    // TC9 - Patient three engagement, three different categories
    //
    response = new FindContentResponseType();
    response.getEngagement().add(
        createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_4, TestDataDefines.TEST_RR_ID_THREE_CATEGORIES,
            TestDataDefines.TEST_BO_ID_MANY_HITS_1,
            TestDataDefines.TEST_DATE_MANY_HITS_1,
            TestDataDefines.CATEGORY_1));
    response.getEngagement().add(
        createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_5, TestDataDefines.TEST_RR_ID_THREE_CATEGORIES,
            TestDataDefines.TEST_BO_ID_MANY_HITS_2,
            TestDataDefines.TEST_DATE_MANY_HITS_2,
            TestDataDefines.CATEGORY_2));
    response.getEngagement().add(
        createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_6, TestDataDefines.TEST_RR_ID_THREE_CATEGORIES,
            TestDataDefines.TEST_BO_ID_MANY_HITS_3,
            TestDataDefines.TEST_DATE_MANY_HITS_3,
            TestDataDefines.CATEGORY_3));
    FINDCONTENT_RESPONSE_MAP.put(TestDataDefines.TEST_RR_ID_THREE_CATEGORIES, response);

    //
    // TC10 - Patient with two engagements, one has AgP's own logical address
    //
    response = new FindContentResponseType();
    response.getEngagement().add(
            createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_RECURSIVE, TestDataDefines.TEST_RR_ID_RECURSIVE,
                    TestDataDefines.TEST_BO_ID_MANY_HITS_1,
                    TestDataDefines.TEST_DATE_MANY_HITS_1));
    response.getEngagement().add(
            createEngagement(TestDataDefines.TEST_LOGICAL_ADDRESS_5, TestDataDefines.TEST_RR_ID_RECURSIVE,
                    TestDataDefines.TEST_BO_ID_MANY_HITS_2,
                    TestDataDefines.TEST_DATE_MANY_HITS_2));
    FINDCONTENT_RESPONSE_MAP.put(TestDataDefines.TEST_RR_ID_RECURSIVE, response);
    log.info(LOG_MSG_ADDED_ITEM, response.getEngagement().size(),
            TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS);

    log.info(LOG_MSG_ADDED_ITEM, response.getEngagement().size(), TestDataDefines.TEST_RR_ID_ONE_FORMAT_ERROR);

  }

  private EngagementType createEngagement(String receiverLogicalAddress, String registeredResidentIdentification,
      String businessObjectId, String date) {
    return createEngagement(receiverLogicalAddress, registeredResidentIdentification, businessObjectId, date, CATEGORY_DEFAULT);
  }

  private EngagementType createEngagement(String receiverLogicalAddress, String registeredResidentIdentification,
      String businessObjectId, String date, String category) {

    EngagementType e = new EngagementType();
    e.setServiceDomain("test_domain");
    e.setCategorization(category);
    e.setLogicalAddress(receiverLogicalAddress);
    e.setRegisteredResidentIdentification(registeredResidentIdentification);
    e.setBusinessObjectInstanceIdentifier(businessObjectId);
    e.setCreationTime(date);
    e.setUpdateTime(date);
    e.setMostRecentContent(date);
    e.setSourceSystem(receiverLogicalAddress);
    return e;
  }
}