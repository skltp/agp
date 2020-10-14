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
