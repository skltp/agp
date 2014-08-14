package se.skltp.agp.monitoring;

import java.util.Date;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.ThreadSafeSimpleDateFormat;

import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.ConfigurationType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

@WebService(
		serviceName = "PingForConfigurationResponderService", 
		endpointInterface="se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface", 
		portName = "PingForConfigurationResponderPort", 
		targetNamespace = "urn:riv:itintegration:monitoring:PingForConfiguration:1:rivtabp21",
		wsdlLocation = "ServiceContracts_itintegration_monitoring/interactions/PingForConfigurationInteraction/PingForConfigurationInteraction_1.0_RIVTABP21.wsdl")
public class PingForconfigurationTestProducer implements PingForConfigurationResponderInterface{
	
	public static final String TIMEOUT_LOGICAL_ADDRESS = "timeout";
	public static final String ERROR_LOGICAL_ADDRESS = "error";
	
	private ThreadSafeSimpleDateFormat dateFormat = new ThreadSafeSimpleDateFormat("yyyyMMddhhmmss");
		
	private static final Logger log = LoggerFactory.getLogger(PingForconfigurationTestProducer.class);
	
	private String timeoutMs;
	
	public void setTimeoutMs(String timeoutMs){
		this.timeoutMs = timeoutMs;
	}

	@Override
	public PingForConfigurationResponseType pingForConfiguration(String logicalAddress,
			PingForConfigurationType parameters) {
		
		log.debug("PingForConfiguration requested for {}", "EI");
		
		if(TIMEOUT_LOGICAL_ADDRESS.equals(parameters.getLogicalAddress())){
			forceTimeout();
		}else if(ERROR_LOGICAL_ADDRESS.equals(parameters.getLogicalAddress())){
			log.info("Logical address to trigger error was used in request");
			throw new RuntimeException("Error occured trying to use EI database, see application logs for details");
		}
		
		PingForConfigurationResponseType response = new PingForConfigurationResponseType();
		response.setPingDateTime(dateFormat.format(new Date()));
		response.getConfiguration().add(createConfigurationInfo("Applikation", "EI"));
		
		log.debug("PingForConfiguration response returned for {}", "EI");
		
		return response;
	}

	private ConfigurationType createConfigurationInfo(String name, String value) {
		log.debug("PingForConfiguration config added [{}: {}]", name, value);
		
		ConfigurationType configurationInfo = new ConfigurationType();
		configurationInfo.setName(name);
		configurationInfo.setValue(value);
		return configurationInfo;
	}

	private void forceTimeout() {
        try {
        	log.debug("Logical address to trigger timeout was used in request, sleeping...");
            Thread.sleep(Long.valueOf(timeoutMs) + 1000);
        } catch (InterruptedException e) {}
    }
}
