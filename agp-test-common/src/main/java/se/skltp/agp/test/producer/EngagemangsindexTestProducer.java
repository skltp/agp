package se.skltp.agp.test.producer;

import static se.skltp.agp.test.producer.TestProducerDb.TEST_BO_ID_FAULT_INVALID_ID;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_BO_ID_MANY_HITS_1;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_BO_ID_MANY_HITS_2;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_BO_ID_MANY_HITS_3;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_BO_ID_MANY_HITS_4;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_BO_ID_ONE_HIT;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_LOGICAL_ADDRESS_1;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_LOGICAL_ADDRESS_2;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_LOGICAL_ADDRESS_3;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_RR_ID_FAULT_INVALID_ID;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_RR_ID_MANY_HITS;
import static se.skltp.agp.test.producer.TestProducerDb.TEST_RR_ID_ONE_HIT;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.riv.itintegration.engagementindex.findcontent.v1.rivtabp21.FindContentResponderInterface;
import se.skltp.agp.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.agp.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentType;
import se.skltp.agp.riv.itintegration.engagementindex.v1.EngagementType;

@WebService(serviceName = "FindContentResponderService", portName = "FindContentResponderPort", targetNamespace = "urn:riv:itintegration:engagementindex:FindContent:1:rivtabp21", name = "FindContentInteraction")
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

	private void initIndex() {
		// Build a booking-index based subjectOfCare as key containing a number of bookings with unique booking-id's spread over one or more logical-addresses. 

		// Patient with one booking
		FindContentResponseType response = new FindContentResponseType();
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_ONE_HIT, TEST_BO_ID_ONE_HIT));
		INDEX.put(TEST_RR_ID_ONE_HIT, response);

		// Patient with three bookings spread over two logical-addresses
		response = new FindContentResponseType();
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_1));
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_2, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_2));
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_2, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_3));
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_3, TEST_RR_ID_MANY_HITS, TEST_BO_ID_MANY_HITS_4));
		INDEX.put(TEST_RR_ID_MANY_HITS, response);
				
		// Patient that cause an exception in the source system
		response = new FindContentResponseType();
		response.getEngagement().add(createResponse(TEST_LOGICAL_ADDRESS_1, TEST_RR_ID_FAULT_INVALID_ID, TEST_BO_ID_FAULT_INVALID_ID));
		INDEX.put(TEST_RR_ID_FAULT_INVALID_ID, response);
	}

	private EngagementType createResponse(String receiverLogicalAddress, String registeredResidentIdentification, String businessObjectId) {
		
		EngagementType e = new EngagementType();
		e.setServiceDomain(eiServiceDomain);
		e.setCategorization(eiCategorization);
		e.setLogicalAddress(receiverLogicalAddress);
		e.setRegisteredResidentIdentification(registeredResidentIdentification);
		e.setBusinessObjectInstanceIdentifier(businessObjectId);
		e.setCreationTime("2011101000000");
		e.setUpdateTime("2011101000000");
		e.setMostRecentContent("2011101000000");
		e.setSourceSystem(receiverLogicalAddress);
		return e;
	}
}