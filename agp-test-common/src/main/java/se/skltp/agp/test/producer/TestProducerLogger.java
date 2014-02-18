package se.skltp.agp.test.producer;

import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.AgpConstants;

public class TestProducerLogger extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(TestProducerLogger.class);

	private static String lastSenderId = null;
	private static String lastOriginalConsumer = null;
	private static String lastVpInstance = null;
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		@SuppressWarnings("unchecked")
		Map<String, Object> httpHeaders = (Map<String, Object>)message.getInboundProperty("http.headers");
		
		String senderId = (String)httpHeaders.get(AgpConstants.X_VP_SENDER_ID);
		log.info("Test producer called with {}: {}", AgpConstants.X_VP_SENDER_ID, senderId);
		lastSenderId = senderId;
		
		String vpInstance = (String)httpHeaders.get(AgpConstants.X_VP_INSTANCE_ID);
		log.info("Test producer called with {}: {}", AgpConstants.X_VP_INSTANCE_ID, vpInstance);
		lastVpInstance = vpInstance;
		
		String orgConsumer = (String)httpHeaders.get(AgpConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
		log.info("Test producer called with {}: {}", AgpConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, orgConsumer);
		lastOriginalConsumer = orgConsumer;

		return message;
	}

	public static String getLastVpInstance() {
		return lastVpInstance;
	}

	public static String getLastSenderId() {
		return lastSenderId;
	}

	public static String getLastOriginalConsumer() {
		return lastOriginalConsumer;
	}
}
