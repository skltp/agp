package se.skltp.aggregatingservices.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.apache.cxf.message.MessageContentsList;


@Log4j2
public abstract class TestDataGenerator {
  private long serviceTimeoutMs;

  public void setServiceTimeoutMs(long serviceTimeoutMs) {
    this.serviceTimeoutMs = serviceTimeoutMs;
  }


  public TestDataGenerator() {
    initDb();
  }

  public Object processRequest(MessageContentsList messageContentsList) {
  	String patientId = getPatientId(messageContentsList);
  	return processRequest((String)messageContentsList.get(0), patientId);
  }


  private Object processRequest(String logicalAddress, String registeredResidentId) {

    // TC5 - Invalid id - return error-message
    if (TestDataDefines.TEST_RR_ID_FAULT_INVALID_ID.equals(registeredResidentId)) {
      throw new TestProducerException("Invalid Id: " + registeredResidentId);
    }

    // TC6 - in EI, but not in TAK
    if (TestDataDefines.TEST_RR_ID_EJ_SAMVERKAN_I_TAK.equals(registeredResidentId)) {
      throw new TestProducerException("VP007 Authorization missing");
    }

    // Simulate some processing
    doSomeProcessingForSomeTime(logicalAddress);

    // Lookup the response
    return retrieveFromDb(logicalAddress, registeredResidentId);
  }

  private void doSomeProcessingForSomeTime(String logicalAddress) {
    long processingTime = getProcessingTime(logicalAddress);
    try {
      log.debug("## SLEEP FOR " + processingTime + " ms.");
      TimeUnit.MILLISECONDS.sleep(processingTime);
      log.debug("## SLEEP DONE.");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public long getProcessingTime(String logicalAddress) {
    long processingTime;
		if (TestDataDefines.TEST_LOGICAL_ADDRESS_1.equals(logicalAddress)) {
			processingTime = 1000;                    // Normal 1 sec response time on system #1
		} else if (TestDataDefines.TEST_LOGICAL_ADDRESS_2.equals(logicalAddress)) {
			processingTime = serviceTimeoutMs - 1000; // Slow but below the timeout on system #2
		} else if (TestDataDefines.TEST_LOGICAL_ADDRESS_3.equals(logicalAddress)) {
			processingTime = serviceTimeoutMs + 1000; // Too slow on system #3, the timeout will kick in
		} else {
			processingTime = 500;                                                                     // 0.5 sec response time for the rest of the systems
		}
    return processingTime;
  }

  public abstract Object createResponse(Object... responseItems);

  public abstract String getPatientId(MessageContentsList messageContentsList);

  public abstract Object createResponseItem(String logicalAddress, String registeredResidentId, String businessObjectId,
      String time);

  public Object createFormatError(Object responseItem){
    return responseItem;
  }

  public abstract Object createRequest(String patientId, String sourceSystemHSAId);
  //
  // Simplest possible memory db for business object instances from test-stubs for a number of source systems
  //
  private static Map<String, Object> db = null;

  void initDb() {
    log.debug("### INIT-DB CALLED, DB == NULL? " + (db == null));

    // Start with resetting the db from old values.
    resetDb();

    //
    // TC1 - Patient with three bookings spread over three logical-addresses, all with fast response times
    //
    Object response = createResponse(
        createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_4, TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_1,
            TestDataDefines.TEST_DATE_MANY_HITS_1));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_4, TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS, response);

    response = createResponse(createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_5,
        TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS,
        TestDataDefines.TEST_BO_ID_MANY_HITS_2,
        TestDataDefines.TEST_DATE_MANY_HITS_2));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_5, TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS, response);

    response = createResponse(createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_6,
        TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS,
        TestDataDefines.TEST_BO_ID_MANY_HITS_3,
        TestDataDefines.TEST_DATE_MANY_HITS_3));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_6, TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS, response);

    //
    // TC3 - Patient with one booking, id = TEST_RR_ID_ONE_HIT
    //       Second booking in engagement index does not exist in the producer
    //
    response = createResponse(
        createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_1, TestDataDefines.TEST_RR_ID_ONE_HIT,
            TestDataDefines.TEST_BO_ID_ONE_HIT, TestDataDefines.TEST_DATE_ONE_HIT));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_1, TestDataDefines.TEST_RR_ID_ONE_HIT, response);

    //
    // TC4 - Patient with four bookings spread over three logical-addresses, where one is on a slow system, i.e. that cause timeouts
    //
    response = createResponse(
        createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_1, TestDataDefines.TEST_RR_ID_MANY_HITS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_1,
            TestDataDefines.TEST_DATE_MANY_HITS_1));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_1, TestDataDefines.TEST_RR_ID_MANY_HITS, response);

    response = createResponse(
        createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_2, TestDataDefines.TEST_RR_ID_MANY_HITS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_2,
            TestDataDefines.TEST_DATE_MANY_HITS_2),
        createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_2, TestDataDefines.TEST_RR_ID_MANY_HITS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_3,
            TestDataDefines.TEST_DATE_MANY_HITS_3));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_2, TestDataDefines.TEST_RR_ID_MANY_HITS, response);

    response = createResponse(
        createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_3, TestDataDefines.TEST_RR_ID_MANY_HITS,
            TestDataDefines.TEST_BO_ID_MANY_HITS_4,
            TestDataDefines.TEST_DATE_MANY_HITS_4));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_3, TestDataDefines.TEST_RR_ID_MANY_HITS, response);

    //
    // TC7 - Patient with one booking, id = TEST_RR_ID_TRADKLATTRING for test trädklättring
    //
    response = createResponse(createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_CHILD,
        TestDataDefines.TEST_RR_ID_TRADKLATTRING,
        TestDataDefines.TEST_BO_ID_TRADKLATTRING,
        TestDataDefines.TEST_DATE_TRADKLATTRING));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_CHILD, TestDataDefines.TEST_RR_ID_TRADKLATTRING, response);

    //
    // TC8 - One ok response, one response contains malformed field , id = TEST_RR_ID_TRADKLATTRING
    //
    response = createResponse(
        createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_4, TestDataDefines.TEST_RR_ID_ONE_FORMAT_ERROR,
            TestDataDefines.TEST_BO_ID_MANY_HITS_1,
            TestDataDefines.TEST_DATE_MANY_HITS_1));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_4, TestDataDefines.TEST_RR_ID_ONE_FORMAT_ERROR, response);

    response = createResponse(
        createFormatError(
            createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_5,
                TestDataDefines.TEST_RR_ID_ONE_FORMAT_ERROR,
                TestDataDefines.TEST_BO_ID_MANY_HITS_2,
                TestDataDefines.TEST_DATE_MANY_HITS_2)
        )
    );
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_5, TestDataDefines.TEST_RR_ID_ONE_FORMAT_ERROR, response);

    //
    // TC9 - Patient with three bookings spread over three logical-addresses and three different categories
    //
    response = createResponse(
        createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_4, TestDataDefines.TEST_RR_ID_THREE_CATEGORIES,
            TestDataDefines.TEST_BO_ID_MANY_HITS_1,
            TestDataDefines.TEST_DATE_MANY_HITS_1));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_4, TestDataDefines.TEST_RR_ID_THREE_CATEGORIES, response);

    response = createResponse(createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_5,
        TestDataDefines.TEST_RR_ID_THREE_CATEGORIES,
        TestDataDefines.TEST_BO_ID_MANY_HITS_2,
        TestDataDefines.TEST_DATE_MANY_HITS_2));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_5, TestDataDefines.TEST_RR_ID_THREE_CATEGORIES, response);

    response = createResponse(createResponseItem(TestDataDefines.TEST_LOGICAL_ADDRESS_6,
        TestDataDefines.TEST_RR_ID_THREE_CATEGORIES,
        TestDataDefines.TEST_BO_ID_MANY_HITS_3,
        TestDataDefines.TEST_DATE_MANY_HITS_3));
    storeInDb(TestDataDefines.TEST_LOGICAL_ADDRESS_6, TestDataDefines.TEST_RR_ID_THREE_CATEGORIES, response);

  }

  public static void resetDb() {
    db = new HashMap<>();
  }

  public void refreshDb() {
    initDb();
  }

  public void storeInDb(String logicalAddress, String registeredResidentId, Object value) {
    db.put(logicalAddress + "|" + registeredResidentId, value);
  }

  public Object retrieveFromDb(String logicalAddress, String registeredResidentId) {
    return db.get(logicalAddress + "|" + registeredResidentId);
  }

  public static class TestProducerException extends RuntimeException {
    public TestProducerException(String msg) {
      super(msg);
    }
  }
}