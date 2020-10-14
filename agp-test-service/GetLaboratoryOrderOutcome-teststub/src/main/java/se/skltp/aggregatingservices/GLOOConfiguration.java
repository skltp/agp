package se.skltp.aggregatingservices;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcome.v4.rivtabp21.GetLaboratoryOrderOutcomeResponderInterface;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcome.v4.rivtabp21.GetLaboratoryOrderOutcomeResponderService;
import se.skltp.aggregatingservices.config.TestProducerConfiguration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "gloo.teststub")
public class GLOOConfiguration extends TestProducerConfiguration {
  public static final String SCHEMA_PATH = "/schemas/clinicalprocess-healthcond-actoutcome/interactions/GetLaboratoryOrderOutcomeInteraction/GetLaboratoryOrderOutcomeInteraction_3.1_RIVTABP21.wsdl";

  public GLOOConfiguration() {
    setProducerAddress("http://localhost:8083/vp");
    setServiceClass(GetLaboratoryOrderOutcomeResponderInterface.class.getName());
    setServiceNamespace("urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcomeResponder:3");
    setPortName(GetLaboratoryOrderOutcomeResponderService.GetLaboratoryOrderOutcomeResponderPort.toString());
    setWsdlPath(SCHEMA_PATH);
    setTestDataGeneratorClass(GLOOTestDataGenerator.class.getName());
    setServiceTimeout(27000);
  }
}
