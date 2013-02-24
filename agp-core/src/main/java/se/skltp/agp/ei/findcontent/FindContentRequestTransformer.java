package se.skltp.agp.ei.findcontent;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentType;
import se.skltp.agp.service.api.QueryObject;

public class FindContentRequestTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(FindContentRequestTransformer.class);
	
	private String engagemangsIndexHsaId;
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

		QueryObject qo = (QueryObject)src;

		FindContentType reqOut = qo.getFindContent();
		
		Object[] reqOutList = new Object[] {engagemangsIndexHsaId, reqOut};

		log.info("Calling EI using logical address {} for subject of care id {}", engagemangsIndexHsaId, reqOut.getRegisteredResidentIdentification());
		
		log.debug("Transformed payload: {}, pid: {}", reqOutList, reqOut.getRegisteredResidentIdentification());
		
		return reqOutList;
	}
}