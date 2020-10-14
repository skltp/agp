package se.skltp.aggregatingservices.route;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.config.TestStubConfiguration;
import se.skltp.aggregatingservices.processors.VagvalResponseBean;
import se.skltp.agp.riv.vagvalsinfo.v2.SokVagvalsInfoInterface;

@Component
public class SokVagValInfoStubRoute extends RouteBuilder {

  public static final String SOKVAGVAL_WSDL_PATH = "/schemas/TD_SOKVAGVAL_2/sokvagval-info-v2.wsdl";
  public static final String SOKVAGVAL_SERVICECLASS = SokVagvalsInfoInterface.class.getName();

  private static final String SERVICE_CONFIGURATION = "cxf:%s"
      + "?wsdlURL=%s"
      + "&serviceClass=%s"
      + "&portName={urn:skl:tp:vagvalsinfo:v2}SokVagvalsSoap11LitDocPort";

  private static final String FAULTY_SERVICE_CONFIGURATION = "jetty:%s";

  protected String serviceAddress;
  protected String faultyServiceAddress;

  @EndpointInject(uri="mock:sokvagval:input")
  MockEndpoint mock;

  @Autowired
  VagvalResponseBean vagvalResponseBean;

  @Autowired
  public SokVagValInfoStubRoute(TestStubConfiguration testStubConfiguration) {
    serviceAddress = String.format(SERVICE_CONFIGURATION, testStubConfiguration.getSokVagValInfoAddress(), SOKVAGVAL_WSDL_PATH, SOKVAGVAL_SERVICECLASS) ;
    faultyServiceAddress = String.format(FAULTY_SERVICE_CONFIGURATION, testStubConfiguration.getFaultyServiceAddress()) ;
  }

  @Override
  public void configure() throws Exception {
    from(serviceAddress).id("SokVagval.route")
        .to("mock:sokvagval:input")
        .choice()
            .when(header("operationName").isEqualTo("hamtaAllaAnropsBehorigheter"))
                .bean(vagvalResponseBean, "getBehorigheter")
            .when(header("operationName").isEqualTo("hamtaAllaVirtualiseringar"))
                .bean(vagvalResponseBean, "getVagval")
        .end();

    from(faultyServiceAddress).id("Faulty.producer")
        .setBody(simple("Hello World!"));

  }

  public MockEndpoint getMock() {
    return mock;
  }
}
