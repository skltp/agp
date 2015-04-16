package se.skltp.agp.test.producer;

import static se.skltp.agp.test.producer.TestProducerDb.*;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.riv.itintegration.engagementindex.findcontent.v1.rivtabp21.FindContentResponderInterface;
import se.skltp.agp.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.agp.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentType;
import se.skltp.agp.riv.itintegration.engagementindex.v1.EngagementType;

@WebService(serviceName     = "FindContentResponderService", 
            portName        = "FindContentResponderPort", 
            targetNamespace = "urn:riv:itintegration:engagementindex:FindContent:1:rivtabp21", 
            name            = "FindContentInteraction")
public class EngagemangsindexTestProducer implements FindContentResponderInterface {

	public static final String TEST_ID_FAULT_INVALID_ID_IN_EI = "EI:INV_ID";
	public static final String TEST_ID_FAULT_TIMEOUT_IN_EI    = "EI:TIMEOUT";

	private static final Logger log = LoggerFactory.getLogger(EngagemangsindexTestProducer.class);

	private String eiServiceDomain;
	public void setEiServiceDomain(String eiServiceDomain) {
		this.eiServiceDomain = eiServiceDomain;
	}

	private String eiCategorization;
	public void setEiCategorization(String eiCategorization) {
		this.eiCategorization = eiCategorization;
	}

	private long serviceTimeoutMs;
	public void setServiceTimeoutMs(long serviceTimeoutMs) {
		this.serviceTimeoutMs = serviceTimeoutMs;
	}

	private static final Map<String, FindContentResponseType> INDEX = new HashMap<String, FindContentResponseType>();

	public EngagemangsindexTestProducer() {
		if (INDEX.size() == 0) {
			initIndex();
		}
	}

	@Override
	public FindContentResponseType findContent(String logicalAdress, FindContentType request) {

		log.info("### Engagemengsindex.findContent() received a request for Registered Resident id: {}", request.getRegisteredResidentIdentification());

		String id = request.getRegisteredResidentIdentification();

		// Return an error-message if invalid id
		if (TEST_ID_FAULT_INVALID_ID_IN_EI.equals(id)) {
			throw new RuntimeException("Invalid Id: " + id);
		}

		// Force a timeout if zero Id
        if (TEST_ID_FAULT_TIMEOUT_IN_EI.equals(id)) {
	    	try {
				Thread.sleep(serviceTimeoutMs + 1000);
			} catch (InterruptedException e) {}
        }

        // Lookup the response
		FindContentResponseType response = INDEX.get(request.getRegisteredResidentIdentification());
        if (response == null) {
        	// Return an empty response object instead of null if nothing is found
        	response = new FindContentResponseType();
        }

		log.info("### Engagemengsindex return {} items", response.getEngagement().size());

        return response;
	}

    // Build a booking-index based subjectOfCare as key containing a number of bookings with unique 
    // booking-id's spread over one or more logical-addresses. 
	private void initIndex() {

        //
        // TC1 - Patient with three bookings spread over three logical-addresses, all with fast response times
        //
	    FindContentResponseType response = new FindContentResponseType();
        response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_4, TEST_RR_ID_MANY_HITS_NO_ERRORS, TEST_BO_ID_MANY_HITS_1, TEST_DATE_MANY_HITS_1));
        response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_5, TEST_RR_ID_MANY_HITS_NO_ERRORS, TEST_BO_ID_MANY_HITS_2, TEST_DATE_MANY_HITS_2));
        response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_6, TEST_RR_ID_MANY_HITS_NO_ERRORS, TEST_BO_ID_MANY_HITS_3, TEST_DATE_MANY_HITS_3));
        INDEX.put(TEST_RR_ID_MANY_HITS_NO_ERRORS, response);
        log.info("### Engagemengsindex add {} items to the index for resident {}", response.getEngagement().size(), TEST_RR_ID_MANY_HITS_NO_ERRORS);
        
		//
		// TC3 - Patient with two bookings in Engagement Index - second booking is missing in producer
		//
		response = new FindContentResponseType();
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_ONE_HIT, TEST_BO_ID_ONE_HIT, TEST_DATE_ONE_HIT));
        response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_2, TEST_RR_ID_ONE_HIT, TEST_BO_ID_ONE_HIT, TEST_DATE_ONE_HIT));
		INDEX.put(TEST_RR_ID_ONE_HIT, response);
		log.info("### Engagemengsindex add {} items to the index for resident {}", response.getEngagement().size(), TEST_RR_ID_ONE_HIT);

		//
		// TC4 - Patient with four bookings spread over three logical-addresses
		//
		response = new FindContentResponseType();
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_1, TEST_DATE_MANY_HITS_1));
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_2, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_2, TEST_DATE_MANY_HITS_2));
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_2, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_3, TEST_DATE_MANY_HITS_3));
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_3, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_4, TEST_DATE_MANY_HITS_4));
		INDEX.put(TEST_RR_ID_MANY_HITS, response);
		log.info("### Engagemengsindex add {} items to the index for resident {}", response.getEngagement().size(), TEST_RR_ID_MANY_HITS);
			
		//
		// TC5 - Patient that causes an exception in the source system
		//
		response = new FindContentResponseType();
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_FAULT_INVALID_ID, TEST_BO_ID_FAULT_INVALID_ID, TEST_DATE_FAULT_INVALID_ID));
		INDEX.put(TEST_RR_ID_FAULT_INVALID_ID, response);
		log.info("### Engagemengsindex add {} items to the index for resident {}", response.getEngagement().size(), TEST_RR_ID_FAULT_INVALID_ID);

        //
        // TC6 - Patient that causes an exception in the source system
        //
        response = new FindContentResponseType();
        response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_7, TEST_RR_ID_EJ_SAMVERKAN_I_TAK, TEST_BO_ID_EJ_SAMVERKAN_I_TAK, TEST_DATE_EJ_SAMVERKAN_I_TAK));
        INDEX.put(TEST_RR_ID_EJ_SAMVERKAN_I_TAK, response);
        log.info("### Engagemengsindex add {} items to the index for resident {}", response.getEngagement().size(), TEST_RR_ID_EJ_SAMVERKAN_I_TAK);
	}

	private EngagementType createResponse(String receiverLogicalAddress, String registeredResidentIdentification, String businessObjectId, String date) {
		
		EngagementType e = new EngagementType();
		e.setServiceDomain(eiServiceDomain);
		e.setCategorization(eiCategorization);
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