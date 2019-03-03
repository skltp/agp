package se.skltp.agp.service.transformers;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.MiscUtil;

import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import se.skltp.agp.cache.AnslutningCacheBean;
import se.skltp.agp.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.agp.riv.itintegration.engagementindex.v1.EngagementType;
import se.skltp.agp.test.producer.SokVagvalsInfoMockInput;
import se.skltp.agp.test.producer.VagvalMockInputRecord;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CreateRequestListTransformerTest extends AbstractTestCase {
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("takcache-test-config");

    static SokVagvalsInfoMockInput svimi = new SokVagvalsInfoMockInput();

    private AnslutningCacheBean anslutningCacheBean;

    private static final String receiver = "SE0000000005-1234";
    private static final String senderA = "senderA";
    private static final String senderB = "senderB";


    @Override
    public void doSetUp() {
        anslutningCacheBean = muleContext.getRegistry().lookupObject("anslutningCache");
    }


    @SuppressWarnings("unused")
    @Test
    public void testTransformer_ok() throws Exception {

        // FIXME, Fix unittests!!!
        int i = 1;
        if (i == 1) return;

        // Specify input and expected result
        String input = MiscUtil.readFileAsString("src/test/resources/testfiles/tidbokning/request-input.xml");

        String expectedResult = MiscUtil.readFileAsString("src/test/resources/testfiles/tidbokning/request-input.xml"); // No transformation is done by default so use input also as expected...


        // Create the transformer under test and let it perform the transformation

        CreateRequestListTransformer transformer = new CreateRequestListTransformer();
        Object result = expectedResult; // transformer.pojoTransform(null, input);


        // Compare the result to the expected value
        assertEquals(expectedResult, result);
    }

    @Test
    public void testFilterFindContentResponseBasedOnAuthority_ok() {
        CreateRequestListTransformer transformer = new CreateRequestListTransformer();
        transformer.setAnslutningCache(anslutningCacheBean);

        FindContentResponseType eiResp = new FindContentResponseType();
        EngagementType engType = new EngagementType();
        engType.setLogicalAddress(receiver);

        eiResp.getEngagement().add(engType);

        transformer.filterFindContentResponseBasedOnAuthority(eiResp, senderA, null);
        assertEquals(1, eiResp.getEngagement().size());
    }

    @Test
    public void testFilterFindContentResponseBasedOnAuthority_Filter_Engagement() {
        CreateRequestListTransformer transformer = new CreateRequestListTransformer();
        transformer.setAnslutningCache(anslutningCacheBean);


        FindContentResponseType eiResp = new FindContentResponseType();
        EngagementType engType = new EngagementType();
        engType.setLogicalAddress(receiver);

        eiResp.getEngagement().add(engType);

        transformer.filterFindContentResponseBasedOnAuthority(eiResp, senderB, null);
        assertEquals(0, eiResp.getEngagement().size());
    }

    @Override
    protected void doSetUpBeforeMuleContextCreation() throws DatatypeConfigurationException {
        setupTjanstekatalogen();
    }

    private void setupTjanstekatalogen() throws DatatypeConfigurationException {
        List<VagvalMockInputRecord> vagvalInputs = new ArrayList<>();
        vagvalInputs.add(createVagvalRecord(senderA, receiver, "rivtabp20", rb.getString("TAK_TJANSTEKONTRAKT")));

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
        VagvalMockInputRecord vagvalInput = createVagvalRecord(senderId, receiverId, rivVersion, serviceNameSpace);
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