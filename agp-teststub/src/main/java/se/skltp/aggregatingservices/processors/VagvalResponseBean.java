package se.skltp.aggregatingservices.processors;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.config.TestProducerConfiguration;
import se.skltp.aggregatingservices.data.VagvalsInfoTestData;
import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaAnropsBehorigheterResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;

@Component
public class VagvalResponseBean {
  VagvalsInfoTestData vagvalsInfoTestData;

  @Autowired
  public VagvalResponseBean(List<TestProducerConfiguration> configurations, VagvalsInfoTestData vagvalsInfoTestData) {
    this.vagvalsInfoTestData = vagvalsInfoTestData;

    for(TestProducerConfiguration configuration : configurations){
      vagvalsInfoTestData.generateDefaultTestData(configuration.getServiceNamespace());
    }
  }


  public HamtaAllaVirtualiseringarResponseType getVagval() {
    return vagvalsInfoTestData.getVagvalResponse();
  }

  public HamtaAllaAnropsBehorigheterResponseType getBehorigheter() {
    return vagvalsInfoTestData.getAnropsBehorigheterResponse();
  }


}
