/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.configuration;

import lombok.Data;

@Data
public class AgpServiceConfiguration {
  String serviceName;

  String targetNamespace;

  String inboundServiceWsdl;
  String inboundServiceURL;
  String inboundServiceClass;
  String inboundPortName;

  String outboundServiceURL;
  String outboundServiceWsdl;
  String outboundServiceClass;
  String outboundPortName;

  String takContract;

  String eiServiceDomain;
  String eiCategorization;

  String serviceFactoryClass;

  int receiveTimeout = -1;
  int connectTimeout = -1;
  int aggregatedServiceTimeout = -1;

  int messageContentListQueryIndex = 1;

  boolean enableSchemaValidation = false;

}
