package se.skltp.agp.service.transformers;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.AgpConstants;

public class CheckSenderIdAndOriginalConsumerTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(CheckSenderIdAndOriginalConsumerTransformer.class);

    /**
     * Message aware transformer that ...
     */
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

    	String senderId         = (String)message.getInboundProperty(AgpConstants.X_VP_SENDER_ID);
    	String originalConsumer = (String)message.getInboundProperty(AgpConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);

    	if (log.isDebugEnabled()) {
    		log.debug(AgpConstants.X_VP_SENDER_ID + " = " + senderId + ", " + AgpConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID + " = " + originalConsumer);
    	}

    	String errMsg = null;
    	if (senderId == null && originalConsumer == null) {
    		errMsg = "Mandatory HTTP headers " + AgpConstants.X_VP_SENDER_ID + " and " + AgpConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID + " are missing";
    		
    	} else if (senderId == null) {
    		errMsg = "Mandatory HTTP header " + AgpConstants.X_VP_SENDER_ID + " is missing";
    		
    	} else if (originalConsumer == null) {
    		errMsg = "Mandatory HTTP header " + AgpConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID + " is missing";
    	}

    	if (errMsg != null) {
    		throw new TransformerException(this, new RuntimeException(errMsg));
    	}
    	
    	return message;
    }
}