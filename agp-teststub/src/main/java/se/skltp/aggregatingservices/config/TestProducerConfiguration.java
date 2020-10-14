package se.skltp.aggregatingservices.config;

import lombok.Data;

@Data
public abstract class TestProducerConfiguration {
  String producerAddress;
  String wsdlPath;
  String serviceClass;
  String serviceNamespace;
  String testDataGeneratorClass;
  String portName;
  int serviceTimeout;
}
