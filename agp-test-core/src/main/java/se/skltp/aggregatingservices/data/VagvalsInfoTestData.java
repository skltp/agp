package se.skltp.aggregatingservices.data;

import static se.skltp.aggregatingservices.data.TestDataDefines.HSA_ID_FEL;
import static se.skltp.aggregatingservices.data.TestDataDefines.SAMPLE_ORIGINAL_CONSUMER_HSAID;
import static se.skltp.aggregatingservices.data.TestDataDefines.SAMPLE_SENDER_ID;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_1;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_11;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_12;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_31;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_32;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_77;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import se.skltp.agp.riv.vagvalsinfo.v2.AnropsBehorighetsInfoType;
import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaAnropsBehorigheterResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.VirtualiseringsInfoType;

@Log4j2
@Service
public class VagvalsInfoTestData {


  private static final String[] receivers = {TEST_LOGICAL_ADDRESS_1, TestDataDefines.TEST_LOGICAL_ADDRESS_2,
      TestDataDefines.TEST_LOGICAL_ADDRESS_3, TestDataDefines.TEST_LOGICAL_ADDRESS_4, TestDataDefines.TEST_LOGICAL_ADDRESS_5,
      TestDataDefines.TEST_LOGICAL_ADDRESS_6};

  public static final String RIVTABP_21 = "rivtabp21";

  private HamtaAllaAnropsBehorigheterResponseType anropsBehorigheterResponse = new HamtaAllaAnropsBehorigheterResponseType();
  private HamtaAllaVirtualiseringarResponseType vagvalResponse = new HamtaAllaVirtualiseringarResponseType();

  public void resetTestData() {
    anropsBehorigheterResponse = new HamtaAllaAnropsBehorigheterResponseType();
    vagvalResponse = new HamtaAllaVirtualiseringarResponseType();
  }

  public HamtaAllaVirtualiseringarResponseType getVagvalResponse() {
    return vagvalResponse;
  }

  public HamtaAllaAnropsBehorigheterResponseType getAnropsBehorigheterResponse() {
    return anropsBehorigheterResponse;
  }

  // Både vägval och behorighet:
  // HSA-ID-1 - HSA-ID-6 (sample-sender-id)
  // HSA-ID-1 (sample-original-consumer-hsaid)
  // HSA-ID-FEL med random targetNamespace
  // HSA-ID-FEL med annat random targetNamespace
  // Bara behörighet:
  // HSA-ID-11 (sample-original-consumer-hsaid)
  // HSA-ID-12 (sample-original-consumer-hsaid)
  // HSA-ID-31 (sample-original-consumer-hsaid)
  // HSA-ID-32 (sample-original-consumer-hsaid)
  // Bara vägval:
  // HSA-ID-77
  public void generateDefaultTestData(String targetNamespaceString) {
    final List<AnropsBehorighetsInfoType> anropsBehorighetsInfo = anropsBehorigheterResponse.getAnropsBehorighetsInfo();
    final List<VirtualiseringsInfoType> virtualiseringsInfo = vagvalResponse.getVirtualiseringsInfo();

    for (int i = 0; i < 6; i++) {
      anropsBehorighetsInfo.add(anropsBehorighetsInfoType(targetNamespaceString, receivers[i], SAMPLE_SENDER_ID));
      virtualiseringsInfo.add(createVirtualiseringsInfo(receivers[i], RIVTABP_21, targetNamespaceString));
    }

    anropsBehorighetsInfo.add(anropsBehorighetsInfoType(targetNamespaceString, TEST_LOGICAL_ADDRESS_1,
        SAMPLE_ORIGINAL_CONSUMER_HSAID));
    anropsBehorighetsInfo.add(anropsBehorighetsInfoType(targetNamespaceString, TEST_LOGICAL_ADDRESS_11,
        SAMPLE_ORIGINAL_CONSUMER_HSAID));
    anropsBehorighetsInfo.add(anropsBehorighetsInfoType(targetNamespaceString, TEST_LOGICAL_ADDRESS_12,
        SAMPLE_ORIGINAL_CONSUMER_HSAID));
    anropsBehorighetsInfo.add(anropsBehorighetsInfoType(targetNamespaceString, TEST_LOGICAL_ADDRESS_31,
        SAMPLE_ORIGINAL_CONSUMER_HSAID));
    anropsBehorighetsInfo.add(anropsBehorighetsInfoType(targetNamespaceString, TEST_LOGICAL_ADDRESS_32,
        SAMPLE_ORIGINAL_CONSUMER_HSAID));


    // We should not have permissions to this one
    virtualiseringsInfo.add(createVirtualiseringsInfo(TEST_LOGICAL_ADDRESS_77, RIVTABP_21, targetNamespaceString));

    // Some faulty random permissions
    String random = UUID.randomUUID().toString();
    anropsBehorighetsInfo.add(anropsBehorighetsInfoType(random, HSA_ID_FEL, "TK_" + HSA_ID_FEL));
    virtualiseringsInfo.add(createVirtualiseringsInfo(HSA_ID_FEL, RIVTABP_21, random));

    String random2 = UUID.randomUUID().toString();
    anropsBehorighetsInfo.add(anropsBehorighetsInfoType(random2, HSA_ID_FEL, "TK_" + HSA_ID_FEL));
    virtualiseringsInfo.add(createVirtualiseringsInfo(HSA_ID_FEL, RIVTABP_21, random2));

  }

  protected AnropsBehorighetsInfoType anropsBehorighetsInfoType(final String ns, final String rId, final String sId) {
    final AnropsBehorighetsInfoType type = new AnropsBehorighetsInfoType();
    type.setTjansteKontrakt(ns);
    type.setReceiverId(rId);
    type.setSenderId(sId);
    type.setFromTidpunkt(xmlDate());
    type.setTomTidpunkt(xmlDateAdd(type.getFromTidpunkt(), Calendar.HOUR, 1));
    return type;
  }

  private VirtualiseringsInfoType createVirtualiseringsInfo(String receiverId, String rivVersion, String contractNS) {
    VirtualiseringsInfoType vi = new VirtualiseringsInfoType();
    vi.setAdress(null);
    vi.setFromTidpunkt(xmlDateAdd(xmlDate(), Calendar.HOUR, -1));
    vi.setTomTidpunkt(xmlDateAdd(xmlDate(), Calendar.YEAR, 100));
    vi.setReceiverId(receiverId);
    vi.setRivProfil(rivVersion);
    vi.setTjansteKontrakt(contractNS);
    return vi;
  }

  private XMLGregorianCalendar xmlDate() {
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance());
    } catch (Exception err) {
      return null;
    }
  }

  private XMLGregorianCalendar xmlDateAdd(XMLGregorianCalendar orgDate, int field, int amount) {
    if(orgDate==null){
      return null;
    }

    try {
      GregorianCalendar calendar = orgDate.toGregorianCalendar();
      calendar.add(field, amount);
      return (DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
    } catch (Exception err) {
      return null;
    }
  }


}
