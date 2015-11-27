package se.skltp.agp.service.transformers;

import java.util.List;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.agp.service.api.QueryObject;
import se.skltp.agp.service.api.RequestListFactory;

public class CreateRequestListTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(CreateRequestListTransformer.class);

	private RequestListFactory requestListFactory;
	public void setRequestListFactory(RequestListFactory requestListFactory) {
		this.requestListFactory = requestListFactory;
	}
	
    /**
     * A findContent request has been sent to engagement index, and a findContent response has been returned.
     * This transformer now creates a list of requests - one for each producer that engagement index has returned.
     * Processing is handled by the implementation of the RequestListFactory interface.
     */
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

    	QueryObject qo = (QueryObject)message.getInvocationProperty("queryObject");
		FindContentResponseType eiResp = (FindContentResponseType)message.getPayload();
    	
    	log.info("findContent.patientId: {}, findContent.serviceDomain: {}, findContentResponse.size: {}", new Object[] {qo.getFindContent().getRegisteredResidentIdentification(), qo.getFindContent().getServiceDomain(), eiResp.getEngagement().size()});
    	
        // Perform any message aware processing here, otherwise delegate as much as possible to pojoTransform() for easier unit testing
    	List<Object[]> transformedPayload = requestListFactory.createRequestList(qo, eiResp);

    	// Set the expected number of responses so that the aggregator knows when to stop, update the message payload and return the message for further processing
    	message.setCorrelationGroupSize(transformedPayload.size());
    	message.setPayload(transformedPayload);
    	return message;
    }

}