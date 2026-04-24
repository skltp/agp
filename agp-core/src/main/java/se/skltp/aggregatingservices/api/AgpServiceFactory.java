/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
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
