package se.skltp.agp.service.transformers;

import java.util.Iterator;
import java.util.List;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.cache.TakCacheBean;
import se.skltp.agp.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.agp.riv.itintegration.engagementindex.v1.EngagementType;
import se.skltp.agp.service.api.QueryObject;
import se.skltp.agp.service.api.RequestListFactory;

public class CreateRequestListTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(CreateRequestListTransformer.class);
	
	private TakCacheBean takCache;
    public void setTakCache(TakCacheBean takCache) {
        this.takCache = takCache;
    }
    
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
    	FindContentResponseType eiResp;
    	List<Object[]> transformedPayload;

		String originalServiceConsumerId = getOriginalServiceConsumerId(message);
		String senderId = getSenderId(message);

    	if(qo.getFindContent() == null || qo.getFindContent().getServiceDomain() == null) {
    		List<String> src = takCache.getReceivers(senderId, originalServiceConsumerId);
    		
			// Perform any message aware processing here, otherwise delegate as much as possible to pojoTransform() for easier unit testing
	    	transformedPayload = requestListFactory.createRequestList(qo, src);
    	} else {
  	
			eiResp = (FindContentResponseType)message.getPayload();
			
			filterFindContentResponseBasedOnAuthority(eiResp, senderId, originalServiceConsumerId);

			// Perform any message aware processing here, otherwise delegate as much as possible to pojoTransform() for easier unit testing
	    	transformedPayload = requestListFactory.createRequestList(qo, eiResp);

	    	log.info("findContent.patientId: {}, findContent.serviceDomain: {}, findContentResponse.size: {}", new Object[] {qo.getFindContent().getRegisteredResidentIdentification(), qo.getFindContent().getServiceDomain(), eiResp.getEngagement().size()});
    	}
    	
    	// Set the expected number of responses so that the aggregator knows when to stop, update the message payload and return the message for further processing
    	message.setCorrelationGroupSize(transformedPayload.size());
    	message.setPayload(transformedPayload);
    	return message;
    }
    
    private String getOriginalServiceConsumerId(MuleMessage muleMessage) {
    	 return (String) muleMessage.getProperty("originalServiceconsumerHsaid", PropertyScope.SESSION);
    }
    
    private String getSenderId(MuleMessage muleMessage) {
    	return (String) muleMessage.getProperty("senderid", PropertyScope.SESSION);
    }
   
    protected void filterFindContentResponseBasedOnAuthority(FindContentResponseType eiResp, String senderId, String originalServiceConsumerId) {
    	Iterator<EngagementType> iterator = eiResp.getEngagement().iterator();
    	
    	while (iterator.hasNext()) {
    		EngagementType engagementType = iterator.next();
    		if (takCache.contains(engagementType.getLogicalAddress())) {
    			log.debug("takCache contains logical adress:" + engagementType.getLogicalAddress());
    			if (!takCache.getAuthorizedConsumers(engagementType.getLogicalAddress()).contains(senderId, originalServiceConsumerId)) {
    				iterator.remove();
    				log.info("Source system: senderId {} / originalServiceConsumerId {} is not authorized to access EngagementType:{} dispatched by FindContent", 
    						new Object[] { senderId, originalServiceConsumerId, engagementType.getLogicalAddress() });
    			}
    		} else {
    			iterator.remove();
    			log.info("No virtualisering found for logical address {} ", new Object[] { engagementType.getLogicalAddress() });
    		}
    	}
    }

    protected void filterFindContentResponseBasedOnAuthority2(FindContentResponseType eiResp, String senderId, String originalServiceConsumerId) {

    	Iterator<EngagementType> iterator = eiResp.getEngagement().iterator();
    	
    	while (iterator.hasNext()) {
    		EngagementType engagementType = iterator.next();
    		if (takCache.contains(engagementType.getLogicalAddress())) {
    			log.debug("takCache contains logical adress:" + engagementType.getLogicalAddress());
    			if (!takCache.getAuthorizedConsumers(engagementType.getLogicalAddress()).contains(senderId, originalServiceConsumerId)) {
    				iterator.remove();
    				log.info("Source system: senderId {} / originalServiceConsumerId {} is not authorized to access EngagementType:{} dispatched by FindContent", 
    						new Object[] { senderId, originalServiceConsumerId, engagementType.getLogicalAddress() });
    			}
    		} else {
    			iterator.remove();
    			log.info("No virtualisering found for logical address {} ", new Object[] { engagementType.getLogicalAddress() });
    		}
    	}
    }

}