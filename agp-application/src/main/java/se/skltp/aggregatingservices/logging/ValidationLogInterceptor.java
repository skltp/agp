package se.skltp.aggregatingservices.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
public class ValidationLogInterceptor extends AbstractPhaseInterceptor<Message> {

  private static final XMLInputFactory XML_INPUT_FACTORY;

  static {
    XML_INPUT_FACTORY = XMLInputFactory.newFactory();
    XML_INPUT_FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    XML_INPUT_FACTORY.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
  }

  private final String serviceName;
  private final ValidationLogger validationLogger;
  Schema schema;
  private final Object schemaLock = new Object();
  private final HashStrategy hashStrategy;

  public ValidationLogInterceptor(String serviceName, ValidationLogger validationLogger) {
    this(serviceName, validationLogger, new Sha256HashStrategy());
  }

  public ValidationLogInterceptor(String serviceName, ValidationLogger validationLogger, HashStrategy hashStrategy) {
    super(Phase.POST_STREAM);
    this.serviceName = serviceName;
    this.validationLogger = validationLogger;
    this.hashStrategy = hashStrategy != null ? hashStrategy : new Sha256HashStrategy();
    this.validationLogger.register(serviceName);
  }

  @Override
  public void handleMessage(Message message) throws Fault {
    boolean inbound = Boolean.TRUE.equals(message.get(Message.INBOUND_MESSAGE));
    if (!inbound) {
      return;
    }

    InputStream originalIs = message.getContent(InputStream.class);
    if (originalIs == null) {
      log.warn("No input stream found in message.");
      return;
    }

    byte[] xmlBytes;
    try {
      xmlBytes = originalIs.readAllBytes();
    } catch (IOException e) {
      log.error("Failed to read message stream", e);
      return;
    }

    Schema localSchema = getOrCreateSchema(message);
    Validator validator = localSchema != null ? localSchema.newValidator() : null;
    processMessage(message, xmlBytes, validator);
  }

  private Schema getOrCreateSchema(Message message) {
    synchronized (schemaLock) {
      if (schema == null) {
        schema = resolveSchema(message);
      }
    }
    return schema;
  }

  @SuppressWarnings("java:S2093") // ByteArrayInputStream should be left open
  private void processMessage(Message message, byte[] xmlBytes, Validator validator) {
    try {
      if (validator != null) {
        validateAgainstSchema(xmlBytes, validator, message);
      } else {
        log.info("No validator could be instantiated, skipping validation");
      }
    } catch (Exception ex) {
      log.error("Unexpected validation error", ex);
    } finally {
      // This must always run, regardless of errors above
      message.setContent(InputStream.class, new ByteArrayInputStream(xmlBytes));
    }
  }


  private void validateAgainstSchema(byte[] xmlBytes, Validator validator, Message message) throws XMLStreamException, IOException, SAXException {
    CollectingErrorHandler handler = new CollectingErrorHandler(this.hashStrategy);
    validator.setErrorHandler(handler);

    XMLStreamReader xsr = getXmlStreamReader(xmlBytes);

    // Discard SOAP-envelope
    while (xsr.hasNext()) {
      if (xsr.isStartElement() && "Body".equals(xsr.getLocalName())) {
        xsr.nextTag();
        break;
      }
      xsr.next();
    }
    validator.validate(new StAXSource(xsr));

    if (handler.hasErrors()) {
      validationLogger.addErrors(handler.getErrors(), serviceName, message);
    }
  }

  private XMLStreamReader getXmlStreamReader(byte[] xmlBytes) throws XMLStreamException {
    return XML_INPUT_FACTORY.createXMLStreamReader(new ByteArrayInputStream(xmlBytes));
  }

  private Schema resolveSchema(Message message) {

    EndpointInfo endpointInfo = message.getExchange()
      .getEndpoint()
      .getEndpointInfo();

    ServiceInfo serviceInfo = endpointInfo.getService();
    if (serviceInfo == null) {
      log.warn("Endpoint has no ServiceInfo.");
      return null;
    }

    List<Source> sources = new ArrayList<>();

    for (SchemaInfo schemaInfo : serviceInfo.getSchemas()) {
      collectPrimarySchema(schemaInfo, sources);
      collectExternalSchemas(schemaInfo, sources);
    }

    if (sources.isEmpty()) {
      return null;
    }

    try {
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      return factory.newSchema(sources.toArray(Source[]::new));
    } catch (Exception ex) {
      log.error("Failed to create schema from schema sources", ex);
      return null;
    }
  }

  private void collectPrimarySchema(SchemaInfo schemaInfo, List<Source> sources) {
    String systemId = schemaInfo.getSystemId();
    if (systemId == null) {
      return;
    }

    URL url = resolveLocation(null, systemId);
    log.debug("Schema location: {}", url);

    if (url != null && isXsd(url)) {
      sources.add(new StreamSource(url.toString()));
    }
  }

  private void collectExternalSchemas(SchemaInfo schemaInfo, List<Source> sources) {
    XmlSchema xmlSchema = schemaInfo.getSchema();
    if (xmlSchema == null) {
      return;
    }

    for (XmlSchemaExternal external : xmlSchema.getExternals()) {
      String location = extractLocation(external);
      if (location == null) {
        continue;
      }

      URL url = resolveLocation(xmlSchema.getSourceURI(), location);
      log.debug("Import/include: {}", url);

      if (url != null && isXsd(url)) {
        sources.add(new StreamSource(url.toString()));
      }
    }
  }

  private String extractLocation(XmlSchemaExternal external) {
    if (external instanceof XmlSchemaImport imp) {
      return imp.getSchemaLocation();
    }
    if (external instanceof XmlSchemaInclude inc) {
      return inc.getSchemaLocation();
    }
    return null;
  }

  private boolean isXsd(URL url) {
    return url.toString().toLowerCase(Locale.ROOT).endsWith(".xsd");
  }

  private URL resolveLocation(String base, String location) {
    try {
      if (base == null) {
        return new URL(location);
      } else {
        return new URL(new URL(base), location);
      }
    } catch (MalformedURLException ignore) {
      // If not an URL, try to get it directly as a resource path.
    }
    return Thread.currentThread().getContextClassLoader().getResource(location);
  }
}
