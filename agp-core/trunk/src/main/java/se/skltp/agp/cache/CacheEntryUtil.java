package se.skltp.agp.cache;

import static org.soitoolkit.commons.xml.XPathUtil.createDocument;
import static org.soitoolkit.commons.xml.XPathUtil.getBuilder;
import static org.soitoolkit.commons.xml.XPathUtil.getXPathResult;
import static org.soitoolkit.commons.xml.XPathUtil.getXml;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.mule.api.MuleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import se.riv.interoperability.headers.v1.ObjectFactory;
import se.riv.interoperability.headers.v1.ProcessingStatusType;

public class CacheEntryUtil {
	
	private static final Logger log = LoggerFactory.getLogger(CacheEntryUtil.class);
	private static final Map<String, String> namespaceMap = new HashMap<String, String>();
	private static final JaxbUtil ju = new JaxbUtil(ProcessingStatusType.class);
	private static final ObjectFactory of = new ObjectFactory();

	static {
		namespaceMap.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
		namespaceMap.put("hdr", "urn:riv:interoperability:headers:1");
	}
	
	private final MuleEvent cachedObject;

	public CacheEntryUtil(MuleEvent cachedObject) {
		this.cachedObject = cachedObject;
	}
	
	public ProcessingStatusType getProcessingStatus() {
		Node processingStatusNode = getProcessingStatusNode();
		ProcessingStatusType processingStatus = (ProcessingStatusType)ju.unmarshal(processingStatusNode);
		return processingStatus;
	}

	public void setProcessingStatus (ProcessingStatusType processingStatus) {
		String processingStatusXml = ju.marshal(of.createProcessingStatus(processingStatus));

		try {
			DocumentBuilder docBuilder = getBuilder();
			Node newProcessingStatusNode = docBuilder.parse(new InputSource(new StringReader(processingStatusXml))).getDocumentElement();

			Document doc = getSoapEnvelope();
			newProcessingStatusNode = doc.importNode(newProcessingStatusNode, true);
			
			Node oldProcessingStatusNode = getProcessingStatusNode();
			Node soapHeaderNode = oldProcessingStatusNode.getParentNode();
			
			soapHeaderNode.replaceChild(newProcessingStatusNode, oldProcessingStatusNode);
			updateSoapEnvelope(getXml(doc));
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
	}

	public String getSoapBody() {

		Node bodyNode = getBodyNode();
		String xml = getXml(bodyNode);
		log.debug("Return body: " + xml);
		
		return xml;
	}

	public void setSoapBody (String soapBody) {
		try {

			DocumentBuilder docBuilder = getBuilder();
			Node newBodyNode = docBuilder.parse(new InputSource(new StringReader(soapBody))).getDocumentElement();

			Document doc = getSoapEnvelope();
			newBodyNode = doc.importNode(newBodyNode, true);
			
			Node oldBodyNode = getBodyNode();
			Node soapBodyNode = oldBodyNode.getParentNode();
			
			soapBodyNode.replaceChild(newBodyNode, oldBodyNode);
			updateSoapEnvelope(getXml(doc));
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ----------------
	// Private methods
	// ----------------
	
	private Document soapEnvelope = null;
	private Document getSoapEnvelope() {
		if (soapEnvelope == null) {
			soapEnvelope = createDocument((String)cachedObject.getMessage().getPayload());
		}

		return soapEnvelope;
	}
	
	private Node getBodyNode() {
		NodeList list = getXPathResult(getSoapEnvelope(), namespaceMap, "/soap:Envelope/soap:Body/*[1]");
		log.debug("Found " + list.getLength() + " elements");

		Node bodyNode = list.item(0);
		return bodyNode;
	}

	private Node getProcessingStatusNode() {
		NodeList list = getXPathResult(getSoapEnvelope(), namespaceMap, "/soap:Envelope/soap:Header/hdr:ProcessingStatus");
		log.debug("Found " + list.getLength() + " elements");

		Node processingStatusNode = list.item(0);
		return processingStatusNode;
	}
	
	private void updateSoapEnvelope(String xml) {
		log.debug("New payload: \n" + xml);
		cachedObject.getMessage().setPayload(xml);
	}	
}