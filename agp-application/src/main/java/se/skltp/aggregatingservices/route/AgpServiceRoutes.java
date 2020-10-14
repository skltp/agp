package se.skltp.aggregatingservices.route;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_SERVICE_COMPONENT_ID;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_SERVICE_HANDLER;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_TAK_CONTRACT_NAME;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.AgpCxfEndpointConfigurer;
import se.skltp.aggregatingservices.api.AgpServiceFactory;
import se.skltp.aggregatingservices.config.VpConfig;
import se.skltp.aggregatingservices.configuration.AgpServiceConfiguration;

@Component
@Log4j2
public class AgpServiceRoutes extends RouteBuilder {

  @Autowired
  GenericApplicationContext applicationContext;

  @Autowired
  VpConfig vpConfig;


  @Value("${validate.soapAction:false}")
  Boolean validateSoapAction;

  public static final String INBOUND_SERVICE_CONFIGURATION = "cxf:%s"
      + "?wsdlURL=%s"
      + "&serviceClass=%s"
      + "&beanId=%s"
      + "&properties.ComponentId=%s"
      + "&cxfConfigurer=#%s";

  public static final String OUTBOUND_SERVICE_CONFIGURATION = "cxf:%s"
      + "?wsdlURL=%s"
      + "&serviceClass=%s"
      + "&beanId=%s"
      + "&cxfConfigurer=#%s"
      + "&properties.use.async.http.conduit=%s"
      + "&properties.org.apache.cxf.transport.http.async.MAX_CONNECTIONS=10000"
      + "&properties.org.apache.cxf.transport.http.async.MAX_PER_HOST_CONNECTIONS=2000";


  List<AgpServiceConfiguration> serviceConfigurations;

  @Autowired
  public AgpServiceRoutes(List<AgpServiceConfiguration> serviceConfigurations) {
    this.serviceConfigurations = serviceConfigurations;
  }

  @Override
  public void configure() throws Exception {

    for (AgpServiceConfiguration serviceConfiguration : serviceConfigurations) {
      createServiceRoute(serviceConfiguration);
    }
  }

  private void createServiceRoute(AgpServiceConfiguration serviceConfiguration) throws Exception {
    registerConfigurationBean(serviceConfiguration);

    String inboundServiceAddress = getInboundServiceAddress(serviceConfiguration);
    String outboundServiceAddress = getOutboundServiceAddress(serviceConfiguration);
    String serviceName = serviceConfiguration.getServiceName();

    AgpServiceFactory agpServiceFactory = getServiceFactory(serviceConfiguration);

    log.debug("inboundServiceAddress: {}", inboundServiceAddress);
    from(inboundServiceAddress).id(String.format("%s.in.route", serviceName)).streamCaching()
        .setProperty(AGP_SERVICE_HANDLER).exchange(ex -> agpServiceFactory)
        .setProperty(AGP_SERVICE_COMPONENT_ID, simple(serviceName))
        .setProperty(AGP_TAK_CONTRACT_NAME, simple(serviceConfiguration.getTakContract()))
        .setProperty("serviceAddress", simple(outboundServiceAddress))
        .to("direct:agproute");

    log.debug("outboundServiceAddress: {}", outboundServiceAddress);
    from("direct:" + serviceName).id(String.format("%s.out.route", serviceName))
        .to(outboundServiceAddress);

  }

  private String getOutboundServiceAddress(AgpServiceConfiguration serviceConfiguration) {
    final String outboundServiceURL = getOutboundServiceURL(serviceConfiguration);
    String outboundServiceAddress = String.format(OUTBOUND_SERVICE_CONFIGURATION
        , outboundServiceURL
        , serviceConfiguration.getOutboundServiceWsdl()
        , serviceConfiguration.getOutboundServiceClass()
        , serviceConfiguration.getServiceName()
        , serviceConfiguration.getServiceName()
        , vpConfig.getUseAyncHttpConduit());
    if (serviceConfiguration.getOutboundPortName() != null) {
      return outboundServiceAddress + "&portName=" + serviceConfiguration.getOutboundPortName();
    }
    return outboundServiceAddress;
  }

  private String getInboundServiceAddress(AgpServiceConfiguration serviceConfiguration) {
    String inboundServiceAddress = String.format(INBOUND_SERVICE_CONFIGURATION
        , serviceConfiguration.getInboundServiceURL()
        , serviceConfiguration.getInboundServiceWsdl()
        , serviceConfiguration.getInboundServiceClass()
        , serviceConfiguration.getServiceName()
        , serviceConfiguration.getServiceName()
        , serviceConfiguration.getServiceName());
    if (serviceConfiguration.getInboundPortName() != null) {
      return inboundServiceAddress + "&portName=" + serviceConfiguration.getInboundPortName();
    }
    return inboundServiceAddress;
  }

  private String getOutboundServiceURL(AgpServiceConfiguration serviceConfiguration) {
    return isEmpty(serviceConfiguration.getOutboundServiceURL()) ?
        vpConfig.getDefaultServiceURL() :
        serviceConfiguration.getOutboundServiceURL();
  }

  private AgpServiceFactory getServiceFactory(AgpServiceConfiguration configuration)
      throws Exception {
    final AgpServiceFactory agpServiceFactory = (AgpServiceFactory) Class.forName(configuration.getServiceFactoryClass())
        .getConstructor().newInstance();
    agpServiceFactory.setAgpServiceConfiguration(configuration);
    return agpServiceFactory;
  }

  private void registerConfigurationBean(AgpServiceConfiguration serviceConfiguration) {
    int receiveTimeout = serviceConfiguration.getReceiveTimeout() >= 0 ? serviceConfiguration.getReceiveTimeout()
        : vpConfig.getDefaultReceiveTimeout();
    int connectTimeout = serviceConfiguration.getConnectTimeout() >= 0 ? serviceConfiguration.getConnectTimeout()
        : vpConfig.getDefaultConnectTimeout();
    boolean enableSchemaValidation = serviceConfiguration.isEnableSchemaValidation();
    log.info("Setting receiveTimeout={}, connectTimeout={} and schemaValidation={} for {}", receiveTimeout, connectTimeout
        , enableSchemaValidation, serviceConfiguration.getServiceName());

    applicationContext.registerBean(serviceConfiguration.getServiceName(), AgpCxfEndpointConfigurer.class,
        () -> new AgpCxfEndpointConfigurer(receiveTimeout, connectTimeout, enableSchemaValidation, validateSoapAction));
  }

}