package se.skltp.aggregatingservices.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.util.StringUtils;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.v1.EngagementType;

@Log4j2
public class RequestListUtil {

  // Static utility class
  private RequestListUtil() {
  }

  public static List<MessageContentsList> createRequestMessageContentsLists(FindContentResponseType eiResp,
      MessageContentsList originalRequest, String filterOnCareUnit) {
    Set<String> sourceSystems = getUniqueSourceSystems(eiResp, filterOnCareUnit);

    List<MessageContentsList> reqList = new ArrayList<>();

    for (String sourceSystem : sourceSystems) {
      log.info("Calling source system using logical address {}", sourceSystem);
      reqList.add(createRequest(sourceSystem, originalRequest));
    }
    return reqList;
  }


  public static Set<String> getUniqueSourceSystems(FindContentResponseType findContentResponseType, String filterOnCareUnit) {
    Set<String> sourceSystems = new HashSet<>(); // set of unique source system hsa ids
    for (EngagementType engagement : findContentResponseType.getEngagement()) {
      if (isPartOf(engagement, filterOnCareUnit)) {
        sourceSystems.add(engagement.getSourceSystem());
      }
    }
    return sourceSystems;
  }

  public static boolean isPartOf(EngagementType engagement, String careUnitId) {
    String careUnit = engagement.getLogicalAddress();
    return StringUtils.isEmpty(careUnitId) || careUnitId.equals(careUnit);
  }

  public static MessageContentsList createRequest(String sourceSystem, MessageContentsList contentsList) {
    MessageContentsList requestList = new MessageContentsList();
    requestList.add(sourceSystem);
    for (int i = 1; i < contentsList.size(); i++) {
      requestList.add(contentsList.get(i));
    }
    return requestList;
  }
}
