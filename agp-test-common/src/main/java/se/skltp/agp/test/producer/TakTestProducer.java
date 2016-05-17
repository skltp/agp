package se.skltp.agp.test.producer;

import java.util.GregorianCalendar;
import java.util.UUID;

import javax.jws.WebService;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;

import se.skltp.agp.riv.vagvalsinfo.v2.AnropsBehorighetsInfoType;
import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaAnropsBehorigheterResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.SokVagvalsInfoInterface;
import se.skltp.agp.riv.vagvalsinfo.v2.VirtualiseringsInfoType;
import se.skltp.agp.test.consumer.AbstractTestConsumer;

@WebService(serviceName = "SokVagvalsServiceSoap11LitDocService", portName = "SokVagvalsSoap11LitDocPort", targetNamespace = "urn:skl:tp:vagvalsinfo:v2", endpointInterface = "se.skltp.agp.riv.vagvalsinfo.v2.SokVagvalsInfoInterface")
public class TakTestProducer implements SokVagvalsInfoInterface {

    private static final Logger log = LoggerFactory.getLogger(TakTestProducer.class);

    private static final String TEST_ADDRESS = "TEST_ADDRESS";
    private static final String TEST_RIV_PROFIL = "TEST_RIV_PROFIL";
    private static final String TEST_VIRTUALISERING_INFO = "TEST_VIRTUALISERING_INFO";

    private static final String[] receivers = { TestProducerDb.TEST_LOGICAL_ADDRESS_1, TestProducerDb.TEST_LOGICAL_ADDRESS_2,
            TestProducerDb.TEST_LOGICAL_ADDRESS_3, TestProducerDb.TEST_LOGICAL_ADDRESS_4, TestProducerDb.TEST_LOGICAL_ADDRESS_5,
            TestProducerDb.TEST_LOGICAL_ADDRESS_6 };
    
    public void log() {
        log.info(toString());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TakTestProducer\n");

        sb.append("targetNamespace:");
        sb.append(targetNamespace);
        sb.append("\n");
        sb.append("targetNamespaceAnotherMajorVersion:");
        sb.append(targetNamespaceAnotherMajorVersion);
        sb.append("\n");
        sb.append("targetNamespaceYetAnotherMajorVersion:");
        sb.append(targetNamespaceYetAnotherMajorVersion);
        sb.append("\n");

        sb.append("serviceTimeout:");
        sb.append(serviceTimeout);
        sb.append("\n");

        return sb.toString();
    }

    private long serviceTimeout;
    public void setServiceTimeout(long serviceTimeout) {
        this.serviceTimeout = serviceTimeout;
        log.info("service timeout is now: " + serviceTimeout);
    }

    private String targetNamespace;
    private String targetNamespaceAnotherMajorVersion;
    private String targetNamespaceYetAnotherMajorVersion;

    public void setTargetNamespace(String n) {
        this.targetNamespace = n;
        if (targetNamespace == null || targetNamespace.isEmpty()) {
            throw new BeanInitializationException("targetNamespace is mandatory");
        } else if (!targetNamespace.matches("^.+?\\d$")) {
            throw new BeanInitializationException("targetNamespace must end with a numeric");
        } else {
            try {
                int i = Integer.parseInt(targetNamespace.substring(targetNamespace.length() - 1));
                if (i < 1) {
                    targetNamespaceAnotherMajorVersion = targetNamespace.substring(0, targetNamespace.length() - 1) + "1";
                    targetNamespaceYetAnotherMajorVersion = targetNamespace.substring(0, targetNamespace.length() - 1) + "2";
                } else {
                    targetNamespaceAnotherMajorVersion = targetNamespace.substring(0, targetNamespace.length() - 1) + (i + 1);
                    targetNamespaceYetAnotherMajorVersion = targetNamespace.substring(0, targetNamespace.length() - 1) + (i + 2);
                }
            } catch (NumberFormatException nn) {
                throw new BeanInitializationException("targetNamespace last character not numeric? " + nn.getLocalizedMessage());
            }
        }
    }

    @Override
    public HamtaAllaAnropsBehorigheterResponseType hamtaAllaAnropsBehorigheter(Object parameters) {
        log.info("tak stub received request hamtaAllaAnropsBehorigheter");
        final HamtaAllaAnropsBehorigheterResponseType resp = createBehorighetStubData();
        log.info("tak stub returning {} AnropsBehorighetsInfo", resp.getAnropsBehorighetsInfo().size());
        return resp;
    }

    @Override
    public HamtaAllaVirtualiseringarResponseType hamtaAllaVirtualiseringar(Object parameters) {
        log.info("tak stub received request hamtaAllaVirtualiseringar");
        HamtaAllaVirtualiseringarResponseType result = createVirtualiseringStubData();
        log.info("tak stub returning {} VirtualiseringInfo", result.getVirtualiseringsInfo().size());
        return result;
    }

    protected HamtaAllaVirtualiseringarResponseType createVirtualiseringStubData() {
        final HamtaAllaVirtualiseringarResponseType type = new HamtaAllaVirtualiseringarResponseType();
        // Add entries with major version supported by the aggregating service
        for (int i = 0; i < 6; i++) {
            type.getVirtualiseringsInfo().add(virtualiseringsInfoType(targetNamespace, receivers[i]));
        }
        // Add some alternative major versions
        type.getVirtualiseringsInfo().add(virtualiseringsInfoType(targetNamespaceAnotherMajorVersion, "HSA-ID-11"));
        type.getVirtualiseringsInfo().add(virtualiseringsInfoType(targetNamespaceAnotherMajorVersion, "HSA-ID-12"));
        type.getVirtualiseringsInfo().add(virtualiseringsInfoType(targetNamespaceYetAnotherMajorVersion, "HSA-ID-31"));
        type.getVirtualiseringsInfo().add(virtualiseringsInfoType(targetNamespaceYetAnotherMajorVersion, "HSA-ID-32"));
        // Add some faulty ones
        type.getVirtualiseringsInfo().add(virtualiseringsInfoType(UUID.randomUUID().toString(), "HSA-ID-FEL"));
        type.getVirtualiseringsInfo().add(virtualiseringsInfoType(UUID.randomUUID().toString(), "HSA-ID-FEL"));
        return type;
    }
    
    protected HamtaAllaAnropsBehorigheterResponseType createBehorighetStubData() {
    	final HamtaAllaAnropsBehorigheterResponseType type = new HamtaAllaAnropsBehorigheterResponseType();
    	
    	for (int i = 0; i < 6; i++) {
            type.getAnropsBehorighetsInfo().add(anropsBehorighetsInfoType(targetNamespace, receivers[i], AbstractTestConsumer.SAMPLE_SENDER_ID));
        }
    	
    	type.getAnropsBehorighetsInfo().add(anropsBehorighetsInfoType(targetNamespaceAnotherMajorVersion, "HSA-ID-11", AbstractTestConsumer.SAMPLE_ORIGINAL_CONSUMER_HSAID));
    	type.getAnropsBehorighetsInfo().add(anropsBehorighetsInfoType(targetNamespaceAnotherMajorVersion, "HSA-ID-12", AbstractTestConsumer.SAMPLE_ORIGINAL_CONSUMER_HSAID));
    	type.getAnropsBehorighetsInfo().add(anropsBehorighetsInfoType(targetNamespaceAnotherMajorVersion, "HSA-ID-31", AbstractTestConsumer.SAMPLE_ORIGINAL_CONSUMER_HSAID));
    	type.getAnropsBehorighetsInfo().add(anropsBehorighetsInfoType(targetNamespaceAnotherMajorVersion, "HSA-ID-32", AbstractTestConsumer.SAMPLE_ORIGINAL_CONSUMER_HSAID));
    	
    	type.getAnropsBehorighetsInfo().add(anropsBehorighetsInfoType(UUID.randomUUID().toString(), "HSA-ID-FEL", "TK_" + "HSA-ID-FEL"));
        type.getAnropsBehorighetsInfo().add(anropsBehorighetsInfoType(UUID.randomUUID().toString(), "HSA-ID-FEL", "TK_" + "HSA-ID-FEL"));
    	
    	return type;
    }

    protected VirtualiseringsInfoType virtualiseringsInfoType(final String ns, final String ls) {
        final VirtualiseringsInfoType type = new VirtualiseringsInfoType();
        type.setAdress(TEST_ADDRESS);
        type.setFromTidpunkt(xmlDate());
        type.setReceiverId(ls);
        type.setRivProfil(TEST_RIV_PROFIL);
        type.setTjansteKontrakt(ns);
        type.setTomTidpunkt(type.getFromTidpunkt());
        type.setVirtualiseringsInfoId(TEST_VIRTUALISERING_INFO);
        
        System.out.println("Adress: " + type.getAdress() + ", From: " + type.getFromTidpunkt() + " To: " + type.getTomTidpunkt() + ", ReceiverId: " + type.getReceiverId()
        	+ ", RIVprofil: " + type.getRivProfil() + ", Kontrakt: " + type.getTjansteKontrakt());
        return type;
    }
    
    protected AnropsBehorighetsInfoType anropsBehorighetsInfoType(final String ns, final String rId, final String sId) {
    	final AnropsBehorighetsInfoType type = new AnropsBehorighetsInfoType();
    	type.setTjansteKontrakt(ns);
    	type.setReceiverId(rId);
    	type.setSenderId(sId);
    	type.setFromTidpunkt(xmlDate());
    	type.setTomTidpunkt(type.getFromTidpunkt());
    	return type;
    }

    private XMLGregorianCalendar xmlDate() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance());
        } catch (Exception err) {
            return null;
        }
    }
}
