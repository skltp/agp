package se.skltp.agp.test.producer;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestProducerDb {

	private static final Logger log = LoggerFactory.getLogger(TestProducerDb.class);

	public static final String TEST_LOGICAL_ADDRESS_1 = "HSA-ID-1";
	public static final String TEST_LOGICAL_ADDRESS_2 = "HSA-ID-2";
	public static final String TEST_LOGICAL_ADDRESS_3 = "HSA-ID-3";

	public static final String TEST_RR_ID_ZERO_HITS = "000000000000";
	public static final String TEST_RR_ID_ONE_HIT   = "111111111111";
	public static final String TEST_RR_ID_MANY_HITS = "222222222222";
	public static final String TEST_RR_ID_FAULT_INVALID_ID = "-1";
	public static final String TEST_RR_ID_FAULT_TIMEOUT    = "0";

	public static final String TEST_BO_ID_ONE_HIT      = "1001";
	public static final String TEST_BO_ID_MANY_HITS_1  = "1002";
	public static final String TEST_BO_ID_MANY_HITS_2  = "1003";
	public static final String TEST_BO_ID_MANY_HITS_3  = "1004";
	public static final String TEST_BO_ID_MANY_HITS_4  = "1004";
	public static final String TEST_BO_ID_MANY_HITS_NEW_1  = "2001";
	public static final String TEST_BO_ID_FAULT_INVALID_ID = "1005";

	private long serviceTimeoutMs;
	public void setServiceTimeoutMs(long serviceTimeoutMs) {
		this.serviceTimeoutMs = serviceTimeoutMs;
	}

	public Object processRequest(String logicalAddress, String registeredResidentId) {

		// Return an error-message if invalid id
		if (TEST_RR_ID_FAULT_INVALID_ID.equals(registeredResidentId)) {
			throw new RuntimeException("Invalid Id: " + registeredResidentId);
		}

		// Force a timeout if zero Id
		// TODO: Do we need this one, we already have a timeout case below???
        if (TEST_RR_ID_FAULT_TIMEOUT.equals(registeredResidentId)) {
	    	try {
				Thread.sleep(serviceTimeoutMs + 1000);
			} catch (InterruptedException e) {}
        }

        // Simulate some processing
		doSomeProcessingForSomeTime(logicalAddress);
        
        // Lookup the response
        Object object = retreiveFromDb(logicalAddress, registeredResidentId);
		return object;
	}

	private void doSomeProcessingForSomeTime(String logicalAddress) {
		long processingTime = getProcessingTime(logicalAddress);
    	try {
    		log.debug("## SLEEP FOR " + processingTime + " ms.");
    		Thread.sleep(processingTime );
    		log.debug("## SLEEP DONE.");
		} catch (InterruptedException e) {}
	}

	public long getProcessingTime(String logicalAddress) {
		long processingTime = 0;
		if      (TEST_LOGICAL_ADDRESS_1.equals(logicalAddress)) processingTime = 1000;                    // Normal 1 sec response time on system #1
		else if (TEST_LOGICAL_ADDRESS_2.equals(logicalAddress)) processingTime = serviceTimeoutMs - 1000; // Slow but below the timeout on system #2
		else if (TEST_LOGICAL_ADDRESS_3.equals(logicalAddress)) processingTime = serviceTimeoutMs + 1000; // Too slow on system #3, the timeout will kick in
		return processingTime;
	}
	
	public abstract Object createResponse(Object... responseItems);

	public abstract Object createResponseItem(String logicalAddress, String registeredResidentId, String businessObjectId);

	//
	// Simplest possible memory db for business object instances from test-stubs for a number of source systems
	//
	private static Map<String, Object> DB = null;
	
	void initDb() {
		log.debug("### INIT-DB CALLED, DB == NULL? " + (DB == null));

		// Start with resetting the db from old values.
		resetDb();

		// Patient with one booking, id = TEST_RR_ID_ONE_HIT
		Object response = createResponse(createResponseItem(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_ONE_HIT, TEST_BO_ID_ONE_HIT));
		storeInDb(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_ONE_HIT, response);

		// Patient with four bookings spread over three logical-addresses, where one is on a slow system, i.e. that cause timeouts
		response = createResponse(createResponseItem(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_1));
		storeInDb(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_MANY_HITS, response);

		response = createResponse(
			createResponseItem(TEST_LOGICAL_ADDRESS_2, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_2),
			createResponseItem(TEST_LOGICAL_ADDRESS_2, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_3));
		storeInDb(TEST_LOGICAL_ADDRESS_2, TEST_RR_ID_MANY_HITS, response);

		response = createResponse(createResponseItem(TEST_LOGICAL_ADDRESS_3, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_4));
		storeInDb(TEST_LOGICAL_ADDRESS_3, TEST_RR_ID_MANY_HITS, response);
	}

	public void resetDb() {
		log.debug("### RESET-DB CALLED, DB == NULL? " + (DB == null));
		DB = new HashMap<String, Object>();
	}

	public void storeInDb(String logicalAddress, String registeredResidentId, Object value) {
		log.debug("### STORE-DB CALLED, DB == NULL? " + (DB == null));
		if (DB == null) {
			initDb();
		}
		DB.put(logicalAddress + "|" + registeredResidentId, value);
	}
	
	public Object retreiveFromDb(String logicalAddress, String registeredResidentId) {
		log.debug("### RETREIVE-DB CALLED, DB == NULL? " + (DB == null));
		if (DB == null) {
			initDb();
		}
        return DB.get(logicalAddress + "|" + registeredResidentId);
	}
}