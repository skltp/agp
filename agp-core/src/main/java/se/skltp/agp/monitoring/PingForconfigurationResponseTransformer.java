package se.skltp.agp.monitoring;

import java.util.Date;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.ThreadSafeSimpleDateFormat;

import se.riv.itintegration.monitoring.v1.ConfigurationType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;

public class PingForconfigurationResponseTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(PingForconfigurationResponseTransformer.class);
	
	private ThreadSafeSimpleDateFormat dateFormat = new ThreadSafeSimpleDateFormat("yyyyMMddHHmmss");
	
	private String appName;
	
	/**
	 * Set the application name to be used in response of a PingForCOnfiguration
	 * @param appName
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
    /**
     * Message aware transformer that ...
     */
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

        // Perform any message aware processing here, otherwise delegate as much as possible to pojoTransform() for easier unit testing
        return pojoTransform(message.getPayload(), outputEncoding);
    }

	/**
     * Simple pojo transformer method that can be tested with plain unit testing...
	 */
	protected Object pojoTransform(Object src, String encoding) throws TransformerException {

		log.debug("Transforming payload: {}", src);
		
		log.debug("PingForConfiguration requested for {}", appName);
		
		PingForConfigurationResponseType response = new PingForConfigurationResponseType();
		response.setPingDateTime(dateFormat.format(new Date()));
		response.getConfiguration().add(createConfigurationInfo("Applikation", appName));
		
		log.debug("PingForConfiguration response returned for {}", appName);
		
		log.debug("Transformed payload: ", response);
		
		return response;
	}
	
	private ConfigurationType createConfigurationInfo(String name, String value) {
		log.debug("PingForConfiguration config added [{}: {}]", name, value);
		
		ConfigurationType configurationInfo = new ConfigurationType();
		configurationInfo.setName(name);
		configurationInfo.setValue(value);
		return configurationInfo;
	}
}