package se.skltp.aggregatingservices.logging;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Error handler that collects SAX parse errors along with their locator info and
 * supports optional hashing of the message via a HashStrategy.
 */
public class CollectingErrorHandler implements ErrorHandler {

  public static final String LEVEL_WARN = "Validation warning";
  public static final String LEVEL_ERROR = "Validation error";
  public static final String LEVEL_FATAL = "Fatal validation error";

  public record LocatorInfo(String publicId, String systemId, int lineNumber, int columnNumber) {
  }

  public record ErrorRecord(LocatorInfo locator, String message) {
  }

  private final List<ErrorRecord> errors = new ArrayList<>();
  private final HashStrategy hashStrategy;

  public CollectingErrorHandler() {
    this(new Sha256HashStrategy());
  }

  public CollectingErrorHandler(HashStrategy hashStrategy) {
    this.hashStrategy = hashStrategy != null ? hashStrategy : new Sha256HashStrategy();
  }

  @Override
  public void warning(SAXParseException exception) {
    errors.add(createRecord(LEVEL_WARN, exception));
  }

  @Override
  public void error(SAXParseException exception) {
    errors.add(createRecord(LEVEL_ERROR, exception));
  }

  @Override
  public void fatalError(SAXParseException exception) {
    errors.add(createRecord(LEVEL_FATAL, exception));
  }

  boolean hasErrors() {
    return !errors.isEmpty();
  }

  List<ErrorRecord> getErrors() {
    return errors;
  }

  private ErrorRecord createRecord(String level, SAXParseException e) {
    LocatorInfo locator = new LocatorInfo(e.getPublicId(), e.getSystemId(), e.getLineNumber(), e.getColumnNumber());
    String messagePart = e.getMessage();
    if (messagePart == null) {
      messagePart = "";
    }
    // Hashing (or noop) is controlled by the provided HashStrategy implementation.
    String message = level + ": " + this.hashStrategy.hash(messagePart);
    return new ErrorRecord(locator, message);
  }
}
