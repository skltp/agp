package se.skltp.aggregatingservices;

import static org.apache.cxf.message.Message.SCHEMA_VALIDATION_ENABLED;

import org.apache.camel.component.cxf.jaxws.CxfConfigurer;
import org.apache.cxf.annotations.SchemaValidation.SchemaValidationType;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.AbstractWSDLBasedEndpointFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import se.skltp.aggregatingservices.cxf.SoapActionRemoverInInterceptor;
import se.skltp.aggregatingservices.logging.MessageLoggingFeature;

public class AgpCxfEndpointConfigurer implements CxfConfigurer {

  @Autowired
  MessageLoggingFeature messageLoggingFeature;

  private int receiveTimeout = 30000;

  private int connectTimeout = 5000;

  private boolean schemaValidationEnabled = false;

  private boolean validateSoapAction = false;


  public AgpCxfEndpointConfigurer(int receiveTimeout) {
    this.receiveTimeout = receiveTimeout;
  }

  public AgpCxfEndpointConfigurer(int receiveTimeout, int connectTimeout, boolean schemaValidation, boolean validateSoapAction) {
    this.receiveTimeout = receiveTimeout;
    this.connectTimeout = connectTimeout;
    this.schemaValidationEnabled = schemaValidation;
    this.validateSoapAction = validateSoapAction;
  }


  @Override
  public void configure(AbstractWSDLBasedEndpointFactory factoryBean) {
    addMessageLoggingFeature(factoryBean);
    addSoapActionInterceptor(factoryBean);
  }

  @Override
  public void configureClient(Client client) {
    setClientTimeouts(client);

    if (schemaValidationEnabled) {
      client.getEndpoint().put(SCHEMA_VALIDATION_ENABLED, SchemaValidationType.IN);
    } else {
      client.getEndpoint().put(SCHEMA_VALIDATION_ENABLED, SchemaValidationType.NONE);
    }

  }

  private void setClientTimeouts(Client client) {
    HTTPConduit conduit = (HTTPConduit) client.getConduit();
    HTTPClientPolicy policy = new HTTPClientPolicy();
    policy.setReceiveTimeout(receiveTimeout);
    policy.setConnectionTimeout(connectTimeout);
    conduit.setClient(policy);
  }

  private boolean addMessageLoggingFeature(AbstractWSDLBasedEndpointFactory factoryBean) {
    return factoryBean.getFeatures().add(messageLoggingFeature);
  }

  private void addSoapActionInterceptor(AbstractWSDLBasedEndpointFactory factoryBean) {
    if (!validateSoapAction) {
      factoryBean.getInInterceptors().add(new SoapActionRemoverInInterceptor());
    }
  }

  @Override
  public void configureServer(Server server) {
    // Do nothing here
  }

}