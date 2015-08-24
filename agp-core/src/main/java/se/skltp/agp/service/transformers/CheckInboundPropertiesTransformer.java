package se.skltp.agp.service.transformers;

import org.apache.commons.lang.StringUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.AgpConstants;

public class CheckInboundPropertiesTransformer extends AbstractMessageTransformer {

    private static final Logger log = LoggerFactory.getLogger(CheckInboundPropertiesTransformer.class);

    /**
     * Message aware transformer that validates inbound parameters.
     */
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

        String senderId         = (String) message.getInboundProperty(AgpConstants.X_VP_SENDER_ID);
        String originalConsumer = (String) message.getInboundProperty(AgpConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
        String correlationId    = (String) message.getInboundProperty(AgpConstants.X_SKLTP_CORRELATION_ID);

        if (log.isDebugEnabled()) {
            log.debug(AgpConstants.X_VP_SENDER_ID + " = " + senderId + ", " + 
                      AgpConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID + " = " + originalConsumer + ", " + 
                      AgpConstants.X_SKLTP_CORRELATION_ID + " = " + correlationId);
        }

        String errMsg = "";
        if (senderId == null) {
            errMsg = "Mandatory HTTP header " + AgpConstants.X_VP_SENDER_ID + " is missing";
        } 
        if (originalConsumer == null) {
            errMsg = errMsg + "\nMandatory HTTP header " + AgpConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID + " is missing";
        }
        if (correlationId == null) {
            errMsg = errMsg + "\nMandatory HTTP header " + AgpConstants.X_SKLTP_CORRELATION_ID + " is missing";
        }
        if (StringUtils.isNotEmpty(errMsg)) {
            throw new TransformerException(this, new RuntimeException(errMsg));
        }

        return message;
    }
}