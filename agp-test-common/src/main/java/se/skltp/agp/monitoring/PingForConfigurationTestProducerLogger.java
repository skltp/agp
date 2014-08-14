package se.skltp.agp.monitoring;

import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.AgpConstants;

public class PingForConfigurationTestProducerLogger extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(PingForConfigurationTestProducerLogger.class);

	private static String lastConsumer = null;
	private static String lastVpInstance = null;
	
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		@SuppressWarnings("unchecked")
		Map<String, Object> httpHeaders = (Map<String, Object>)message.getInboundProperty("http.headers");
		
		String consumer = (String)httpHeaders.get(AgpConstants.X_VP_SENDER_ID);
		log.debug("Test producer called with {}: {}", AgpConstants.X_VP_SENDER_ID, consumer);
		lastConsumer = consumer;
		
		String vpInstance = (String)httpHeaders.get(AgpConstants.X_VP_INSTANCE_ID);
		log.debug("Test producer called with {}: {}", AgpConstants.X_VP_INSTANCE_ID, vpInstance);
		lastVpInstance = vpInstance;

		return message;
	}

	public static String getLastConsumer() {
		return lastConsumer;
	}

	public static String getLastVpInstance() {
		return lastVpInstance;
	}
	
}
