package se.skltp.agp.monitoring;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

public class PingForconfigurationRequestTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(PingForconfigurationRequestTransformer.class);
	
	private String engagemangsIndexHsaId;
	
	/**
	 * The logical address to use when addressing EI, needed if PingForConfiguration goes through VP
	 * @param engagemangsIndexHsaId
	 */
	public void setEngagemangsIndexHsaId(String engagemangsIndexHsaId) {
		this.engagemangsIndexHsaId = engagemangsIndexHsaId;
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
		
		//Forward the original request, but with logicalAddress=engagemangsIndexHsaId
		Object[] reqList = (Object[])src;
		PingForConfigurationType forwardRequestIn = (PingForConfigurationType)reqList[1];
		Object[] reqOutList = new Object[] {engagemangsIndexHsaId, forwardRequestIn};

		log.debug("Forward PingForConfiguration request to EI using logical address {}", engagemangsIndexHsaId);
		
		log.debug("Transformed payload: {}", reqOutList);
		
		return reqOutList;
	}
}