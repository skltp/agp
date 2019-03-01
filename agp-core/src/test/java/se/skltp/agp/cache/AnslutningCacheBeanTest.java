package se.skltp.agp.cache;

import org.junit.After;
import org.junit.Test;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import se.skltp.agp.test.producer.SokVagvalsInfoMockInput;
import se.skltp.agp.test.producer.VagvalMockInputRecord;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AnslutningCacheBeanTest extends AbstractTestCase {
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("takcache-test-config");
    private AnslutningCacheBean anslutningCacheBean;
    static SokVagvalsInfoMockInput svimi = new SokVagvalsInfoMockInput();

    private static final String receiverChild = "SE0000000005-1234";
    private static final String receiverParent = "SE0000000003-1234";
    private static final String receiverParentParent = "SE0000000004-1234";

    private static final String senderA = "senderA";
    private static final String senderB = "senderB";
    private static final String senderC = "senderC";
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
    }

    @Test
    public void testTradklattring(){
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderA, "", receiverChild));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderC, "", receiverChild));
        assertFalse(anslutningCacheBean.isAuthorizedConsumer(senderA, "", receiverParent));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderC, "", receiverParent));
    }

    @Test
    public void testIsAuthorizedConsumerWithFiltreringFelTjanstekontrakt(){
        assertFalse(anslutningCacheBean.isAuthorizedConsumer(senderB, "", receiverChild));
    }

    @Test
    public void testStandardBehorighet(){
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderStandard, "", receiverParentParent));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderStandard, "", receiverChild));
        assertTrue(anslutningCacheBean.isAuthorizedConsumer(senderStandard, "", receiverParent));
    }

    @Override
    protected void doSetUpBeforeMuleContextCreation() throws Exception {
        setupTjanstekatalogen();
    }

    private void setupTjanstekatalogen() {
        List<VagvalMockInputRecord> vagvalInputs = new ArrayList<>();
        vagvalInputs.add(createVagvalRecord(senderA, receiverChild, "rivtabp20", rb.getString("TAK_TJANSTEKONTRAKT")));
        vagvalInputs.add(createVagvalRecord(senderC, receiverParent, "rivtabp21", rb.getString("TAK_TJANSTEKONTRAKT")));

        vagvalInputs.add(createVagvalRecord(senderStandard, "*", "rivtabp21", rb.getString("TAK_TJANSTEKONTRAKT")));

        vagvalInputs.add(createVagvalRecord(senderB, receiverChild, "rivtabp20", "urn:riv:ehr:accesscontrol:AssertCareEngagementResponder:1"));

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
