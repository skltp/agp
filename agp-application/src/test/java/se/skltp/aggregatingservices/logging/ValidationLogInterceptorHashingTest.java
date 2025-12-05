package se.skltp.aggregatingservices.logging;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import static org.junit.jupiter.api.Assertions.*;

class ValidationLogInterceptorHashingTest {

  static final class TestHashStrategy implements HashStrategy {
    @Override
    public String hash(String input) {
      return "HASH(" + input + ")";
    }
  }

  @Test
  void testHashingEnabledUsesStrategy() {
    var handler = new CollectingErrorHandler(new TestHashStrategy());

    handler.error(new SAXParseException("sensitive-data", "", "", 12, 34));
    assertTrue(handler.hasErrors());
    var errors = handler.getErrors();
    assertEquals(1, errors.size());
    var errorRecord = errors.get(0);
    assertTrue(errorRecord.message().contains("HASH(sensitive-data)"), "Expected hashed message using TestHashStrategy");
  }

  @Test
  void testHashingDisabledKeepsPlainMessage() {
    var handler = new CollectingErrorHandler(new NoopHashStrategy());

    handler.error(new SAXParseException("sensitive-data", "", "", 12, 34));
    assertTrue(handler.hasErrors());
    var errors = handler.getErrors();
    assertEquals(1, errors.size());
    var errorRecord = errors.get(0);
    assertTrue(errorRecord.message().contains("sensitive-data"), "Expected plain message when hashing is disabled");
    assertFalse(errorRecord.message().contains("HASH("), "Should not use hash strategy when hashing is disabled");
  }
}
