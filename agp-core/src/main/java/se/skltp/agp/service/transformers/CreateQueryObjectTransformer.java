package se.skltp.agp.service.transformers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.module.xml.stax.MapNamespaceContext;
import org.mule.module.xml.stax.ReversibleXMLStreamReader;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.skltp.agp.service.api.QueryObject;
import se.skltp.agp.service.api.QueryObjectFactory;

public class CreateQueryObjectTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(CreateQueryObjectTransformer.class);
	private static final Map<String, String> namespaceMap = new HashMap<String, String>();

	static {
		namespaceMap.put("soap",    "http://schemas.xmlsoap.org/soap/envelope/");
		namespaceMap.put("it-int",  "urn:riv:itintegration:registry:1");
		namespaceMap.put("interop", "urn:riv:interoperability:headers:1");
	}

	private QueryObjectFactory queryObjectFactory;
	public void setQueryObjectFactory(QueryObjectFactory queryObjectFactory) {
		this.queryObjectFactory = queryObjectFactory;
	}

	/**
     * Incoming mule message contains a request from the consumer. This will contain patientId, domain, and a number of parameters.
     * The message is passed to the implementation of the QueryObjectFactory which will return a findContent request which
     * will be sent later on to the engagement index.
     */
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

        // Perform any message aware processing here, otherwise delegate as much as possible to pojoTransform() for easier unit testing
        return pojoTransform(message.getPayload(), outputEncoding);
    }

	/**
	 * Retrieve the node in the incoming xml message and pass it on to the query object factory for creating a findContent request.
	 * 
     * Simple pojo transformer method that can be tested with plain unit testing...
	 */
	protected Object pojoTransform(Object src, String encoding) throws TransformerException {

		// TODO: Do we need to convert it to a string here or can we use it as a stream for efficiency
		String xml = XmlUtil.convertReversibleXMLStreamReaderToString((ReversibleXMLStreamReader)src, "UTF-8");
		log.debug("Transforming payload: {}", xml);

		Object soapBody;
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
		    xpath.setNamespaceContext(new MapNamespaceContext(namespaceMap));

//			XPathExpression xpathLogicalAddress = xpath.compile("/soap:Envelope/soap:Header/it-int:LogicalAddress");
//			XPathExpression xpathActor = xpath.compile("/soap:Envelope/soap:Header/interop:Actor");
			XPathExpression xpathRequest = xpath.compile("/soap:Envelope/soap:Body/*[1]");

			Document reqDoc = createDocument(xml, "UTF-8");
			soapBody = xpathRequest.evaluate(reqDoc, XPathConstants.NODESET);
	        if (soapBody == null) {
	            throw new RuntimeException("Unable to find soap body in incoming message");
	        } else {
	            NodeList list = (NodeList)soapBody; 
	            if (list.getLength() < 1) {
	                throw new RuntimeException("Unable to find soap body in incoming message (length 0)");
	            } else {
	                Node node = list.item(0);
	                log.debug("Request root-element: " + node.getLocalName() + " - " + node.getNamespaceURI());
	                
	                if (!validSourceSystemHSAIdUsingXPath(reqDoc)) {
                        throw new RuntimeException("Ifyllt sourceSystemHSAId får inte skickas till en aggregerande tjänst");
	                } else {
    	                QueryObject findContentQueryObject = queryObjectFactory.createQueryObject(node);
                        return findContentQueryObject;
	                }
	            }
	        }
 		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
    /**
     * @return false if the incoming request contains a non-blank sourceSystemHSAId/sourceSystemHSAid. (SERVICE-218)
     */
	protected boolean validSourceSystemHSAIdUsingXPath(Document doc) {
	    
	    // use local-name() in order to avoid complexity surrounding namespaces
	    
	    XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            XPathExpression xpathExpression = xpath.compile("//*[local-name()='sourceSystemHSAId']");
            String sourceSystemHSAId = (String) xpathExpression.evaluate(doc, XPathConstants.STRING);
            if (StringUtils.isNotBlank(sourceSystemHSAId)) {
                return false;
            } else {
                xpathExpression = xpath.compile("//*[local-name()='sourceSystemHSAid']");
                String sourceSystemHSAid = (String) xpathExpression.evaluate(doc, XPathConstants.STRING);
                if (StringUtils.isNotBlank(sourceSystemHSAid)) {
                    return false;
                }
            }
        } catch (XPathExpressionException e) {
            log.error("Unexpected XPath error",e);
        }	
        return true;
	}
	

    /**
     * @return false if the incoming request contains a non-blank sourceSystemHSAId/sourceSystemHSAid. (SERVICE-218)
     */
	protected boolean validSourceSystemHSAId(Object request) {
        try {
            Method getSourceSystemHSAId = request.getClass().getMethod("getSourceSystemHSAId");
            try {
                Object sourceSystemHSAId = getSourceSystemHSAId.invoke(request, new Object[]{});
                if (sourceSystemHSAId != null) {
                    if (sourceSystemHSAId instanceof String) {
                        String s = (String)sourceSystemHSAId;
                        if (StringUtils.isNotBlank(s)) {
                            return false;
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                log.error("Unexpected Exception for " + request.getClass().getName(), e);
            } catch (IllegalAccessException e) {
                log.error("Unexpected Exception for " + request.getClass().getName(), e);
            } catch (InvocationTargetException e) {
                log.error("Unexpected Exception for " + request.getClass().getName(), e);
            }
        } catch (SecurityException e) {
            log.error("Unexpected SecurityException for " + request.getClass().getName(), e);
        } catch (NoSuchMethodException n) {
            // no method getSourceSystemHSAId, now need to check getSourceSystemHSAid
            try {
                Method getSourceSystemHSAid = request.getClass().getMethod("getSourceSystemHSAid");
                try {
                    Object sourceSystemHSAid = getSourceSystemHSAid.invoke(request, new Object[]{});
                    if (sourceSystemHSAid != null) {
                        if (sourceSystemHSAid instanceof String) {
                            String s = (String)sourceSystemHSAid;
                            if (StringUtils.isNotBlank(s)) {
                                return false;
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Unexpected Exception for " + request.getClass().getName(), e);
                } catch (IllegalAccessException e) {
                    log.error("Unexpected Exception for " + request.getClass().getName(), e);
                } catch (InvocationTargetException e) {
                    log.error("Unexpected Exception for " + request.getClass().getName(), e);
                }
            } catch (SecurityException e) {
                log.error("Unexpected SecurityException for " + request.getClass().getName(), e);
            } catch (NoSuchMethodException e) {
                // this is not an error - ignore
            }
        }
        return true;
    }

    private Document createDocument(String content, String charset) {
		try {
			InputStream is = new ByteArrayInputStream(content.getBytes(charset));
			return getBuilder().parse(is);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private DocumentBuilder getBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		return builder;
	}
}