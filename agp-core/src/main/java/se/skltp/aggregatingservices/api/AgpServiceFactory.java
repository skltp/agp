package se.skltp.aggregatingservices.api;

import java.util.List;
import org.apache.cxf.message.MessageContentsList;
import se.skltp.aggregatingservices.configuration.AgpServiceConfiguration;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentType;

public interface AgpServiceFactory<T> {

  void setAgpServiceConfiguration(AgpServiceConfiguration agpServiceConfiguration);
  AgpServiceConfiguration getAgpServiceConfiguration();
  FindContentType createFindContent(MessageContentsList queryObject);
  List<MessageContentsList> createRequestList(MessageContentsList queryObject, FindContentResponseType src);
  T createAggregatedResponseObject(MessageContentsList queryObject, List<MessageContentsList> aggregatedResponseList);



}
