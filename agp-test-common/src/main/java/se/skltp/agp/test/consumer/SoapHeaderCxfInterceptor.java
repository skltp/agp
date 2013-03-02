package se.skltp.agp.test.consumer;

import java.util.List;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.EndpointSelectionInterceptor;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusType;

public class SoapHeaderCxfInterceptor extends AbstractSoapInterceptor {

	private final JaxbUtil ju = new JaxbUtil(ProcessingStatusType.class);

	private static ProcessingStatusType lastFoundProcessingStatus = null;
	
	public static ProcessingStatusType getLastFoundProcessingStatus() {
		return lastFoundProcessingStatus;
	}

	public SoapHeaderCxfInterceptor() {
        super(Phase.READ);
        addAfter(ReadHeadersInterceptor.class.getName());
        addAfter(EndpointSelectionInterceptor.class.getName());
    }

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		List<Header> headers = message.getHeaders();
		for (Header header : headers) {
			if ("ProcessingStatus".equals(header.getName().getLocalPart()) && 
				"urn:riv:interoperability:headers:1".equals(header.getName().getNamespaceURI())) {

				try {
					ProcessingStatusType ps = (ProcessingStatusType)ju.unmarshal(header.getObject());
					lastFoundProcessingStatus = ps;
				} catch (Throwable ex) {
				}
			}
		}
	}
}