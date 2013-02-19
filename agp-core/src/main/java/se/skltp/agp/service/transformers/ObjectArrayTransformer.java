package se.skltp.agp.service.transformers;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectArrayTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(ObjectArrayTransformer.class);

    /**
     * Message aware transformer that ...
     */
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

    	String logicalAddress = message.getInvocationProperty("logical-address");
    	
    	log.debug("Add logical-address to the payload as an object-array, {}", logicalAddress);

    	return new Object[] {logicalAddress, message.getPayload()};
	}
}