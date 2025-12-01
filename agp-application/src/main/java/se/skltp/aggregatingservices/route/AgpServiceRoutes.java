package se.skltp.aggregatingservices.route;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_SERVICE_COMPONENT_ID;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_SERVICE_HANDLER;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_TAK_CONTRACT_NAME;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.transport.common.gzip.GZIPFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.AgpCxfEndpointConfigurer;
import se.skltp.aggregatingservices.api.AgpServiceFactory;
import se.skltp.aggregatingservices.config.VpConfig;
import se.skltp.aggregatingservices.configuration.AgpServiceConfiguration;
import se.skltp.aggregatingservices.logging.ValidationLogInterceptor;
import se.skltp.aggregatingservices.logging.ValidationLogger;

@Component
@Log4j2
public class AgpServiceRoutes extends RouteBuilder {

  @Value("${validate.soapAction:false}")
  Boolean validateSoapAction;

  private final GenericApplicationContext applicationContext;

  private final VpConfig vpConfig;

  private final ValidationLogger validationLogger;

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
  public AgpServiceRoutes(GenericApplicationContext applicationContext, VpConfig vpConfig, ValidationLogger validationLogger, List<AgpServiceConfiguration> serviceConfigurations) {
    this.applicationContext = applicationContext;
    this.vpConfig = vpConfig;
    this.validationLogger = validationLogger;
    this.serviceConfigurations = serviceConfigurations;
  }

  @Override
  public void configure() throws Exception {

    for (AgpServiceConfiguration serviceConfiguration : serviceConfigurations) {
      createServiceRoute(serviceConfiguration);
    }

    from(String.format("timer:validationLoggerFlush?period=%d", vpConfig.getValidationLog().getInterval()))
      .process(e -> validationLogger.flush());
  }

  private void createServiceRoute(AgpServiceConfiguration serviceConfiguration) throws Exception {
    registerConfigurationBean(serviceConfiguration);

    String inboundServiceAddress = getInboundServiceAddress(serviceConfiguration);
    String outboundServiceAddress = getOutboundServiceAddress(serviceConfiguration);
    String serviceName = serviceConfiguration.getServiceName();

    var agpServiceFactory = getServiceFactory(serviceConfiguration);

    log.info("inboundServiceAddress: {}", inboundServiceAddress);
    from(inboundServiceAddress).id(String.format("%s.in.route", serviceName)).streamCache("true")
      .setProperty(AGP_SERVICE_HANDLER).exchange(ex -> agpServiceFactory)
      .setProperty(AGP_SERVICE_COMPONENT_ID, simple(serviceName))
      .setProperty(AGP_TAK_CONTRACT_NAME, simple(serviceConfiguration.getTakContract()))
      .setProperty("serviceAddress", simple(outboundServiceAddress))
      .to("direct:agproute");

    createOutboundRoute(outboundServiceAddress, serviceName);

  }

  private void createOutboundRoute(String outboundServiceAddress, String serviceName) {
    log.info("outboundServiceAddress: {}", outboundServiceAddress);
    CxfEndpoint cxfEndpoint = getContext().getEndpoint(outboundServiceAddress, CxfEndpoint.class);
    if (vpConfig.getValidationLog().getServices().contains(serviceName)) {
      cxfEndpoint.getInInterceptors().add(new ValidationLogInterceptor(serviceName, validationLogger));
    }
    List<Feature> features = new ArrayList<>();
    features.add(new GZIPFeature());
    cxfEndpoint.setFeatures(features);
    from("direct:" + serviceName).id(String.format("%s.out.route", serviceName))
      .to(cxfEndpoint);
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

  @SuppressWarnings({"java:S3740", "java:S112", "rawtypes"})
  private AgpServiceFactory getServiceFactory(AgpServiceConfiguration configuration)
    throws Exception {
    final var agpServiceFactory = (AgpServiceFactory) Class.forName(configuration.getServiceFactoryClass())
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