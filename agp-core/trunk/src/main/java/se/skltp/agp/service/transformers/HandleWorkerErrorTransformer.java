package se.skltp.agp.service.transformers;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.riv.interoperability.headers.v1.CausingAgentEnum;
import se.riv.interoperability.headers.v1.LastUnsuccessfulSynchErrorType;

public class HandleWorkerErrorTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(HandleWorkerErrorTransformer.class);

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		ExceptionPayload ep = message.getExceptionPayload();
		Throwable e = ep.getException();
		Throwable re = ep.getRootException();
		String cid = message.getCorrelationId();
		log.warn("CorrId: {}, Error: {}, {}", new Object[] {cid, e, re});

		String errorText = e.getMessage();
		if (re != null) {
			errorText += ", " + re.getMessage();
		}

		LastUnsuccessfulSynchErrorType error = new LastUnsuccessfulSynchErrorType();
		error.setCausingAgent(CausingAgentEnum.VIRTUALIZATION_PLATFORM);
		error.setCode(Integer.toString(ep.getCode()));
		error.setText(errorText);
		
    	String logicalAddress = message.getInvocationProperty("logical-address");
    	
		message.setExceptionPayload(null);
		message.setPayload(new Object[] {logicalAddress, error});

		return message;
	}
}