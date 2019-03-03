package se.skltp.agp.cache;

import org.junit.After;
import org.junit.Test;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import se.skltp.agp.test.producer.SokVagvalsInfoMockInput;
import se.skltp.agp.test.producer.TjansteKatalogenTestProducer;
import se.skltp.agp.test.producer.VagvalMockInputRecord;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class AnslutningCacheBeanTest extends AbstractTestCase {
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("takcache-test-config");
    private AnslutningCacheBean anslutningCacheBean;
    static SokVagvalsInfoMockInput svimi = new SokVagvalsInfoMockInput();

    private static final String receiverChild = "SE0000000005-1234";
    private static final String receiverParent = "SE0000000003-1234";
    private static final String receiverParentParent = "SE0000000004-1234";

    private static final String senderA = "senderA";
    private static final String senderB = "senderB";
    private static final String senderMedFelKontrakt = "senderMedFelKontrakt";
    private static final String senderC = "senderC";
    private static final String senderMedExpiredAnslutning = "senderMedExpiredAnslutning";


    private static final String senderStandard = "senderSt";

    @Override
    public void doSetUp()  {
        anslutningCacheBean = muleContext.getRegistry().lookupObject("anslutningCache");
    }


    @Test
    public void testIsAuthorizedConsumer(){
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderA, "", receiverChild));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderC, "", receiverParent));
        assertFalse(anslutningCacheBean.isAuthorizedConsumer(senderA, "", receiverParent));

        assertTrue(anslutningCacheBean.isAuthorizedConsumer("", senderA, receiverChild));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer("", senderC, receiverParent));
        assertFalse(anslutningCacheBean.isAuthorizedConsumer("", senderA, receiverParent));

    }

    @Test
    public void testTradklattring(){
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderA, "", receiverChild));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderC, "", receiverChild));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderB, "", receiverChild));

        assertFalse(anslutningCacheBean.isAuthorizedConsumer(senderA, "", receiverParent));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderB, "", receiverParent));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderC, "", receiverParent));

        assertFalse(anslutningCacheBean.isAuthorizedConsumer(senderA, "", receiverParentParent));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderB, "", receiverParentParent));
        assertFalse(anslutningCacheBean.isAuthorizedConsumer(senderC, "", receiverParentParent));


        assertTrue(anslutningCacheBean.isAuthorizedConsumer("", senderA, receiverChild));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer("",senderC, receiverChild));
        assertFalse(anslutningCacheBean.isAuthorizedConsumer("", senderA, receiverParent));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer("", senderC, receiverParent));

    }

    @Test
    public void testIsAuthorizedConsumerWithFiltreringFelTjanstekontrakt(){
        assertFalse(anslutningCacheBean.isAuthorizedConsumer(senderMedFelKontrakt, "", receiverChild));
        assertFalse(anslutningCacheBean.isAuthorizedConsumer("", senderMedFelKontrakt, receiverChild));
    }

    @Test
    public void testStandardBehorighet(){
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderStandard, "", receiverParentParent));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderStandard, "", receiverChild));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderStandard, "", receiverParent));

        assertTrue(anslutningCacheBean.isAuthorizedConsumer("", senderStandard, receiverParentParent));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer("", senderStandard, receiverChild));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer("", senderStandard, receiverParent));
    }

    @Test
    public void testIsAuthorizedDateFiltrering(){
        assertFalse(anslutningCacheBean.isAuthorizedConsumer(senderMedExpiredAnslutning, "", receiverParent));

        assertFalse(anslutningCacheBean.isAuthorizedConsumer("", senderMedExpiredAnslutning, receiverParent));
    }

    @Test
    public void testgetReceivers() {
        List<String> receivers = anslutningCacheBean.getReceivers(senderA, "");
        assertEquals(1, receivers.size());
        assertEquals(receiverChild, receivers.get(0));

        receivers = anslutningCacheBean.getReceivers("", senderC);
        assertEquals(1, receivers.size());
        assertEquals(receiverParent, receivers.get(0));

        receivers = anslutningCacheBean.getReceivers(senderStandard, "");
        assertEquals(0, receivers.size());

        receivers = anslutningCacheBean.getReceivers(senderMedFelKontrakt, "");
        assertEquals(0, receivers.size());

        receivers = anslutningCacheBean.getReceivers(senderMedExpiredAnslutning, "");
        assertEquals(0, receivers.size());
    }



    @Override
    protected void doSetUpBeforeMuleContextCreation() throws DatatypeConfigurationException {
        setupTjanstekatalogen();
    }

    private void setupTjanstekatalogen() throws DatatypeConfigurationException {
        List<VagvalMockInputRecord> vagvalInputs = new ArrayList<>();
        vagvalInputs.add(createVagvalRecord(senderA, receiverChild, "rivtabp20", rb.getString("TAK_TJANSTEKONTRAKT")));
        vagvalInputs.add(createVagvalRecord(senderC, receiverParent, "rivtabp21", rb.getString("TAK_TJANSTEKONTRAKT")));
        vagvalInputs.add(createVagvalRecord(senderB, receiverParentParent, "rivtabp21", rb.getString("TAK_TJANSTEKONTRAKT")));

        vagvalInputs.add(createVagvalRecord(senderStandard, "*", "rivtabp21", rb.getString("TAK_TJANSTEKONTRAKT")));

        vagvalInputs.add(createVagvalRecord(senderMedFelKontrakt, receiverChild, "rivtabp20", "urn:riv:ehr:accesscontrol:AssertCareEngagementResponder:1"));

        vagvalInputs.add(createVagvalRecord(senderMedExpiredAnslutning, receiverParent, "rivtabp21", rb.getString("TAK_TJANSTEKONTRAKT"),
                TjansteKatalogenTestProducer.getRelativeDate(DatatypeFactory.newInstance().newDuration(false, 0, 0, 1, 1, 0, 0)),
                TjansteKatalogenTestProducer.getRelativeDate(DatatypeFactory.newInstance().newDuration(false, 0, 0, 0, 1, 0, 0))));


        svimi.setVagvalInputs(vagvalInputs);
    }

    private static VagvalMockInputRecord createVagvalRecord(String senderId, String receiverId, String rivVersion, String serviceNameSpace) {
        VagvalMockInputRecord vagvalInput = new VagvalMockInputRecord();
        vagvalInput.receiverId = receiverId;
        vagvalInput.rivVersion = rivVersion;
        vagvalInput.senderId = senderId;
        vagvalInput.serviceContractNamespace = serviceNameSpace;
        return vagvalInput;
    }

    private static VagvalMockInputRecord createVagvalRecord(String senderId, String receiverId, String rivVersion, String serviceNameSpace, XMLGregorianCalendar fromDate, XMLGregorianCalendar toDate) {
        VagvalMockInputRecord vagvalInput = createVagvalRecord( senderId, receiverId, rivVersion, serviceNameSpace);
        vagvalInput.setFromDate(fromDate);
        vagvalInput.setToDate(toDate);
        return vagvalInput;
    }

    @Override
    protected String getConfigResources() {
        return "soitoolkit-mule-jms-connector-activemq-embedded.xml," +
                "PingForConfiguration-test-common.xml," +
                "teststub-non-default-services/tak-teststub-service.xml," +
                "PingForConfiguration-rivtabp21-service.xml, " +
                "aggregating-services-common.xml," +
                "TakCache-service.xml";
    }

    @After
    public void cleanUpAfterTest() throws IOException {
        Files.deleteIfExists(FileSystems.getDefault().getPath(rb.getString("takcache.persistent.file.name")));
    }
}
