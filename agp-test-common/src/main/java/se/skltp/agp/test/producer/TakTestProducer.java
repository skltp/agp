package se.skltp.agp.test.producer;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.jws.WebService;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaAnropsBehorigheterResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.SokVagvalsInfoInterface;
import se.skltp.agp.riv.vagvalsinfo.v2.VirtualiseringsInfoType;

@WebService(serviceName = "SokVagvalsServiceSoap11LitDocService",
targetNamespace = "urn:skl:tp:vagvalsinfo:v2",
endpointInterface = "se.skltp.agp.riv.vagvalsinfo.v2.SokVagvalsInfoInterface",
        portName = "SokVagvalsSoap11LitDocPort")
public class TakTestProducer implements SokVagvalsInfoInterface {
	
	private static final String TEST_ADDRESS = "TEST_ADDRESS";
	private static final String TEST_RECIVER_ID = "TEST_RECIVER_ID";
	private static final String TEST_RIV_PROFIL = "TEST_RIV_PROFIL";
	private static final String TEST_VIRTUALISERING_INFO = "TEST_VIRTUALISERING_INFO";
	
	private long serviceTimeout;
	private String targetNamespace;

	public long getServiceTimeout() {
		return serviceTimeout;
	}

	public void setServiceTimeout(long serviceTimeout) {
		this.serviceTimeout = serviceTimeout;
	}


	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}


	@Override
	public HamtaAllaAnropsBehorigheterResponseType hamtaAllaAnropsBehorigheter(Object parameters) {
		final HamtaAllaAnropsBehorigheterResponseType resp = new HamtaAllaAnropsBehorigheterResponseType();
		return resp;
	}

	@Override
	public HamtaAllaVirtualiseringarResponseType hamtaAllaVirtualiseringar(Object parameters) {
		return createStubData();
	}
	
	protected HamtaAllaVirtualiseringarResponseType createStubData() {
		final HamtaAllaVirtualiseringarResponseType type = new HamtaAllaVirtualiseringarResponseType();
		//Add entry with correct namespace
		type.getVirtualiseringsInfo().add(infoType(targetNamespace));
		//Add some faulty ones
		for(int i = 0; i < 5; i++) {
			type.getVirtualiseringsInfo().add(infoType(UUID.randomUUID().toString()));
		}
		return type;
	}
	
	protected VirtualiseringsInfoType infoType(final String targetNamespace) {
		final VirtualiseringsInfoType type = new VirtualiseringsInfoType();
		type.setAdress(TEST_ADDRESS);
		type.setFromTidpunkt(xmlDate());
		type.setReceiverId(TEST_RECIVER_ID);
		type.setRivProfil(TEST_RIV_PROFIL);
		type.setTjansteKontrakt(targetNamespace);
		type.setTomTidpunkt(type.getFromTidpunkt());
		type.setVirtualiseringsInfoId(TEST_VIRTUALISERING_INFO);
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
