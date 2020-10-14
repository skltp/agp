package se.skltp.aggregatingservices.utils;

import static se.skltp.aggregatingservices.constants.AgpProperties.ENGAGEMENT_PROCESSING_RESULT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.cxf.message.MessageContentsList;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.v1.EngagementType;

public class EngagementProcessingStatusUtil {
  public static final String RESULT_OK = "OK";
  public static final String RESULT_FILTERED_BY_TAK = "FT";
  public static final String RESULT_FILTERED_BY_SERVICE = "FS";
  public static final String RESULT_ERROR = "ERR";

  // Utility class
  private EngagementProcessingStatusUtil() {
  }


  public static Map<String, String> initAllAsFiltered(FindContentResponseType findContentResponse, Exchange exchange) {
    Map<String, String> engagementProcessingResult = new HashMap<>();

    for (EngagementType engagementType : findContentResponse.getEngagement()) {
      engagementProcessingResult.put(engagementType.getSourceSystem(), RESULT_FILTERED_BY_TAK);
    }
    exchange.setProperty(ENGAGEMENT_PROCESSING_RESULT, engagementProcessingResult);

    return engagementProcessingResult;
  }

  public static Map<String, String> updateWithNotFilteredByTak(FindContentResponseType findContentResponse, Exchange exchange) {
    Map <String, String> engagementProcessingResult = exchange.getProperty(ENGAGEMENT_PROCESSING_RESULT, Map.class);

    for(EngagementType engagementType : findContentResponse.getEngagement()){
      if(engagementProcessingResult.containsKey(engagementType.getSourceSystem())) {
        engagementProcessingResult.put(engagementType.getSourceSystem(), RESULT_FILTERED_BY_SERVICE);
      }
    }

    exchange.setProperty(ENGAGEMENT_PROCESSING_RESULT, engagementProcessingResult);
    return engagementProcessingResult;
  }

  public static Map<String, String> updateWithNotFilteredByService(List<MessageContentsList> queryObjects, Exchange exchange) {
    Map <String, String> engagementProcessingResult = exchange.getProperty(ENGAGEMENT_PROCESSING_RESULT, Map.class);
    for(MessageContentsList messageContentsList : queryObjects){
      String sourceSystem = (String)messageContentsList.get(0);
      if( engagementProcessingResult.containsKey(sourceSystem)){
        engagementProcessingResult.put(sourceSystem, RESULT_ERROR);
      }
    }
    exchange.setProperty(ENGAGEMENT_PROCESSING_RESULT, engagementProcessingResult);
    return engagementProcessingResult;
  }

  public static Map<String, String> updateOK(String sourceSystem, Exchange exchange) {
    Map <String, String> engagementProcessingResult = exchange.getProperty(ENGAGEMENT_PROCESSING_RESULT, Map.class);
    engagementProcessingResult.put(sourceSystem, RESULT_OK);
    exchange.setProperty(ENGAGEMENT_PROCESSING_RESULT, engagementProcessingResult);
    return engagementProcessingResult;
  }

  public static String logFormat(Exchange exchange) {
    Map <String, String> engagementProcessingResult = exchange.getProperty(ENGAGEMENT_PROCESSING_RESULT, Map.class);
    if(engagementProcessingResult==null){
      return "Error";
    }else if(engagementProcessingResult.isEmpty()){
      return "No match from findContent";
    }
    return engagementProcessingResult.toString();
  }
}
