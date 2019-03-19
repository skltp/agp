package se.skltp.agp.test.consumer;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;

import se.skltp.agp.AgpConstants;

public abstract class AbstractTestConsumer<ServiceInterface> {

	public static final String SAMPLE_SENDER_ID               = "sample-sender-id";
	public static final String SENDER_MED_STANDARD_BEHORIGHET_ID = "standart-behorighet-sender-id";
	public static final String SAMPLE_ORIGINAL_CONSUMER_HSAID = "sample-original-consumer-hsaid";
    public static final String SAMPLE_CORRELATION_ID          = "sample-correlation-id";
	
	protected ServiceInterface _service = null;	

    private Class<ServiceInterface> _serviceType;

    /**
     * Constructs a test consumer with a web service proxy setup for communication using HTTPS with Mutual Authentication (can also be used for plain http comunication)
     * 
     * @param serviceType, required to be able to get the generic class at runtime, see http://stackoverflow.com/questions/3403909/get-generic-type-of-class-at-runtime
     * @param serviceAddress
     * @param senderId mandatory http header x-vp-sender-id
     * @param originalConsumerHsaId mandatory http header x-rivta-original-serviceconsumer-hsaid
     * @param correlationId mandatory http header x-skltp-correlation-id
     */
	public AbstractTestConsumer(Class<ServiceInterface> serviceType, String serviceAddress, String senderId, String originalConsumerHsaId, String correlationId) {

		_serviceType = serviceType;
		
		JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
		proxyFactory.setServiceClass(getServiceType());
		proxyFactory.setAddress(serviceAddress);

		// Used for HTTPS
		SpringBusFactory bf = new SpringBusFactory();
		URL cxfConfig = this.getClass().getClassLoader().getResource("agp-cxf-test-consumer-config.xml");
		if (cxfConfig != null) {
			proxyFactory.setBus(bf.createBus(cxfConfig));
		}

		_service = proxyFactory.create(getServiceType()); 
		
		setSenderOriginalConsumerCorrelation(_service, senderId, originalConsumerHsaId, correlationId);
	}

    Class<ServiceInterface> getServiceType() {
    	return _serviceType;
    }
    
    private void setSenderOriginalConsumerCorrelation (ServiceInterface service, String senderId, String originalConsumerHsaId, String correlationId) {

    	// Get the underlying Client object from the proxy object of service interface
    	Client proxy = ClientProxy.getClient(service);
    	 
    	// Creating HTTP headers
    	// Allow the mandatory headers to be null, i.e. skip setting them, 
    	// to be able to construct negative tests that verify error handling when one or both header are missing
    	Map<String, List<String>> headers = new HashMap<String, List<String>>();
    	if (senderId != null) {
    		headers.put(AgpConstants.X_VP_SENDER_ID, Arrays.asList(senderId));
    	}
    	if (originalConsumerHsaId != null) {
    		headers.put(AgpConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, Arrays.asList(originalConsumerHsaId));
    	}
        if (correlationId != null) {
            headers.put(AgpConstants.X_SKLTP_CORRELATION_ID, Arrays.asList(correlationId));
        }
    	 
    	// Add HTTP headers to the web service request
    	proxy.getRequestContext().put(Message.PROTOCOL_HEADERS, headers);
    	 
//    	// If you want to log the SOAP XML of outgoing requests and incoming responses at client side, you can leave this uncommented. It'll be helpful in debugging.
//    	proxy.getOutInterceptors().add(new LoggingOutInterceptor());
//    	proxy.getInInterceptors().add(new LoggingInInterceptor());    	
    }
}