package se.skltp.agp.cache;

import org.junit.After;
import org.junit.Test;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import se.skl.tp.hsa.cache.HsaCacheImpl;
import se.skltp.agp.test.producer.SokVagvalsInfoMockInput;
import se.skltp.agp.test.producer.VagvalMockInputRecord;
import se.skltp.takcache.TakCacheImpl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CacheRefreshTest extends AbstractTestCase {
    static SokVagvalsInfoMockInput svimi = new SokVagvalsInfoMockInput();

    private HsaCacheImpl hsaCache;
    private TakCacheImpl takCache;

    private static final String vardgivareB = "SE0000000003-1234";
    private static final String konsumentA = "konsumentA";
    private static final String konsumentB = "konsumentB";


    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("takcache-test-config");

    @Override
    public void doSetUp() throws Exception {
        hsaCache = muleContext.getRegistry().lookupObject("hsaCache");
        takCache = muleContext.getRegistry().lookupObject("takCache");
    }

    @Override
    protected void doSetUpBeforeMuleContextCreation() throws Exception {
        setupTjanstekatalogen();
    }

    private void setupTjanstekatalogen() {
        List<VagvalMockInputRecord> vagvalInputs = new ArrayList<>();
        vagvalInputs.add(createVagvalRecord(vardgivareB, "rivtabp20", konsumentA, rb.getString("TAK_TJANSTEKONTRAKT")));
        vagvalInputs.add(createVagvalRecord(vardgivareB, "rivtabp21", konsumentA, rb.getString("TAK_TJANSTEKONTRAKT")));
        vagvalInputs.add(createVagvalRecord(vardgivareB, "rivtabp21", konsumentA, rb.getString("TAK_TJANSTEKONTRAKT")
        ));
        vagvalInputs.add(createVagvalRecord(vardgivareB, "rivtabp20", konsumentB, "urn:riv:ehr:accesscontrol:AssertCareEngagementResponder:1"));
        svimi.setVagvalInputs(vagvalInputs);
    }

    private static VagvalMockInputRecord createVagvalRecord(String receiverId, String rivVersion, String senderId, String serviceNameSpace) {
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

    @Test
    public void testRefreshCache()  {
        assertEquals("HSA cache has been updated incorrectly", 7, hsaCache.getHSACacheSize());
        assertEquals("TAK cache has been updated incorrectly", 3, takCache.getAnropsBehorighetsInfos().size());
    }

    @After
    public void cleanUpAfterTest() throws IOException {
        Files.deleteIfExists(FileSystems.getDefault().getPath(rb.getString("takcache.persistent.file.name")));
    }
}
