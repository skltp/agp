package se.skltp.agp.test.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum.DATA_FROM_CACHE;
import static se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum.DATA_FROM_SOURCE;
import static se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum.NO_DATA_SYNCH_FAILED;

import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;

import org.springframework.beans.factory.BeanInitializationException;
import se.skltp.agp.riv.interoperability.headers.v1.CausingAgentEnum;
import se.skltp.agp.riv.interoperability.headers.v1.LastUnsuccessfulSynchErrorType;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusRecordType;
import se.skltp.agp.test.producer.SokVagvalsInfoMockInput;
import se.skltp.agp.test.producer.TestProducerDb;
import se.skltp.agp.test.producer.TjansteKatalogenTestProducer;
import se.skltp.agp.test.producer.VagvalMockInputRecord;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractAggregateIntegrationTest extends AbstractTestCase {

	private static final String ERROR_LOG_QUEUE = "SOITOOLKIT.LOG.ERROR";
	private AbstractJmsTestUtil jmsUtil = null;

	static SokVagvalsInfoMockInput svimi = new SokVagvalsInfoMockInput();

	private static final String[] receivers = { TestProducerDb.TEST_LOGICAL_ADDRESS_1, TestProducerDb.TEST_LOGICAL_ADDRESS_2,
			TestProducerDb.TEST_LOGICAL_ADDRESS_3, TestProducerDb.TEST_LOGICAL_ADDRESS_4, TestProducerDb.TEST_LOGICAL_ADDRESS_5,
			TestProducerDb.TEST_LOGICAL_ADDRESS_6 };


	private String targetNamespace;


	public AbstractAggregateIntegrationTest(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	    // Only start up Mule once to make the tests run faster...
	    // Set to false if tests interfere with each other when Mule is started only once.
	    setDisposeContextPerClass(true);
    }

	@Override
	protected void doSetUpBeforeMuleContextCreation() throws DatatypeConfigurationException {
		setupTjanstekatalogen();
	}

	private void setupTjanstekatalogen() {
		List<VagvalMockInputRecord> vagvalInputs = new ArrayList<>();

		for (int i = 0; i < 6; i++) {
			vagvalInputs.add(createVagvalRecord(null, receivers[i], "rivtabp21", targetNamespace, false, true));
		}

		// We should not have permissions to this one
		vagvalInputs.add(createVagvalRecord(null, "HSA-ID-77", "rivtabp21", targetNamespace, false, true));

		// Add some faulty ones
		vagvalInputs.add(createVagvalRecord(null, "HSA-ID-FEL", "rivtabp21", (UUID.randomUUID().toString()), false, true));
		vagvalInputs.add(createVagvalRecord(null, "HSA-ID-FEL", "rivtabp21", (UUID.randomUUID().toString()), false, true));



		for (int i = 0; i < 6; i++) {
			vagvalInputs.add(createVagvalRecord(AbstractTestConsumer.SAMPLE_SENDER_ID, receivers[i], "rivtabp21", targetNamespace, true, false));
		}

		//for trädklättring test
		vagvalInputs.add(createVagvalRecord(AbstractTestConsumer.SAMPLE_SENDER_ID, TestProducerDb.TEST_LOGICAL_ADDRESS_PARENT, "rivtabp21", targetNamespace, true, false));
//

		//Standard behörighet
		vagvalInputs.add(createVagvalRecord(AbstractTestConsumer.SENDER_MED_STANDARD_BEHORIGHET_ID, "*", "rivtabp21", targetNamespace, true, false));


		// Permissions for AbstractTestConsumer.SAMPLE_ORIGINAL_CONSUMER_HSAID
		vagvalInputs.add(createVagvalRecord(AbstractTestConsumer.SAMPLE_ORIGINAL_CONSUMER_HSAID, "HSA-ID-1", "rivtabp21", targetNamespace, true, false));
		vagvalInputs.add(createVagvalRecord(AbstractTestConsumer.SAMPLE_ORIGINAL_CONSUMER_HSAID, "HSA-ID-77", "rivtabp21", targetNamespace, true, false));


		// Some faulty random permissions
		vagvalInputs.add(createVagvalRecord("TK_" + "HSA-ID-FEL", "HSA-ID-FEL", "rivtabp21", UUID.randomUUID().toString(), true, false));
		vagvalInputs.add(createVagvalRecord("TK_" + "HSA-ID-FEL", "HSA-ID-FEL", "rivtabp21", UUID.randomUUID().toString(), true, false));
		svimi.setVagvalInputs(vagvalInputs);
	}

	private static VagvalMockInputRecord createVagvalRecord(String senderId, String receiverId, String rivVersion, String serviceNameSpace, boolean addBehorighet, boolean addVagval) {
		VagvalMockInputRecord vagvalInput = new VagvalMockInputRecord();
		vagvalInput.receiverId = receiverId;
		vagvalInput.rivVersion = rivVersion;
		vagvalInput.senderId = senderId;
		vagvalInput.serviceContractNamespace = serviceNameSpace;
		vagvalInput.addBehorighet=addBehorighet;
		vagvalInput.addVagval=addVagval;

		return vagvalInput;
	}


    @Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

//		TODO: Mule EE dependency
//		CacheMemoryStoreImpl<MuleEvent> cache = getCache(muleContext);
//		cache.reset();

		// Setup jms
		
		// TODO: Fix lazy init of JMS connection et al so that we can create jmsutil in the declaration
		// (The embedded ActiveMQ queue manager is not yet started by Mule when jmsutil is declared...)
		if (jmsUtil == null) jmsUtil = new ActiveMqJmsTestUtil();
		
 		// Clear queues used for error handling
		jmsUtil.clearQueues(ERROR_LOG_QUEUE);
    }

    protected TestProducerDb getTestDb() {
    	return (TestProducerDb)muleContext.getRegistry().lookupObject("service-producer-testdb-bean");
    }
    
    protected void assertProcessingStatusDataFromSource(ProcessingStatusRecordType status, String logicalAddress) {
		assertEquals(logicalAddress, status.getLogicalAddress());
		assertEquals(DATA_FROM_SOURCE, status.getStatusCode());
		assertFalse(status.isIsResponseFromCache());
		assertTrue(status.isIsResponseInSynch());
		assertNotNull(status.getLastSuccessfulSynch());
		assertNull(status.getLastUnsuccessfulSynch());
		assertNull(status.getLastUnsuccessfulSynchError());
	}

    protected void assertProcessingStatusDataFromCache(ProcessingStatusRecordType status, String logicalAddress) {
		assertEquals(logicalAddress, status.getLogicalAddress());
		assertEquals(DATA_FROM_CACHE, status.getStatusCode());
		assertTrue(status.isIsResponseFromCache());
		assertTrue(status.isIsResponseInSynch());
		assertNotNull(status.getLastSuccessfulSynch());
		assertNull(status.getLastUnsuccessfulSynch());
		assertNull(status.getLastUnsuccessfulSynchError());
	}

    protected void assertProcessingStatusNoDataSynchFailed(ProcessingStatusRecordType status, String logicalAddress, CausingAgentEnum agent, String expectedErrStartingWith) {
		assertEquals(logicalAddress, status.getLogicalAddress());
		assertEquals(NO_DATA_SYNCH_FAILED, status.getStatusCode());
		assertFalse(status.isIsResponseFromCache());
		assertFalse(status.isIsResponseInSynch());
		assertNull(status.getLastSuccessfulSynch());
		assertNotNull(status.getLastUnsuccessfulSynch());

		LastUnsuccessfulSynchErrorType error = status.getLastUnsuccessfulSynchError();
		assertNotNull(error);
		assertEquals(agent, error.getCausingAgent());
		assertNotNull(error.getCode());
		assertTrue("Missing expected [" + expectedErrStartingWith + "] in the beginning if the error message [" + error.getText() + "]", error.getText().startsWith(expectedErrStartingWith));
	}
}
