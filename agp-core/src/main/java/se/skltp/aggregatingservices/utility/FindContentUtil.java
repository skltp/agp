package se.skltp.aggregatingservices.utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import se.skltp.aggregatingservices.configuration.AgpServiceConfiguration;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentType;

public class FindContentUtil {

  // Static utility class
  private FindContentUtil() {
  }

  public static FindContentType createFindContent(String patientId, String serviceDomain, String categorization) {
    FindContentType fc = new FindContentType();
    fc.setRegisteredResidentIdentification(patientId);
    fc.setServiceDomain(serviceDomain);
    fc.setCategorization(categorization);
    return fc;
  }

  public static List<String> getEiCategorizations(AgpServiceConfiguration agpServiceConfiguration) {
    if (agpServiceConfiguration.getEiCategorization() == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(agpServiceConfiguration.getEiCategorization().split(","));
  }


}