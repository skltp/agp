package se.skltp.aggregatingservices.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

// note: class name need not match the @Plugin name.
@Plugin(name = "TestLogAppender", category = "Core", elementType = "appender", printObject = true)
public class TestLogAppender extends AbstractAppender {

  private static TestLogAppender instance;

  public static TestLogAppender getInstance() {
    if (instance != null && !instance.isStarted()) {
      instance.start();
    }

    if (instance != null) {
      instance.clearEvents();
    }

    return instance;
  }

  public static TestLogAppender getInstance(String name, Filter filter, Layout<? extends Serializable> layout,
      boolean ignoreExceptions) {
    if (instance == null) {
      instance = new TestLogAppender(name, filter, layout, true);
    }
    return instance;
  }

  private static List<LogEvent> events = Collections.synchronizedList(new ArrayList<>());

  private TestLogAppender(String name, Filter filter, Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
    super(name, filter, layout, ignoreExceptions);
  }

  @Override
  public void append(final LogEvent event) {

    System.out.println("** APPEND ** "+ event.getLoggerName());
    // Clear events if start if new incomming message to VP
    if (event.getLoggerName().endsWith(EventType.REQ_IN.name())) {
      clearEvents();
    }
    events.add(event.toImmutable());
  }

  public static void clearEvents() {
    events.clear();
  }

  public static String getEventMessage(String loggerName, int index) {

    List<LogEvent> newEvents = getEvents(loggerName);

    if (newEvents.size() < index) {
      return null;
    }

    return newEvents.get(index).getMessage().getFormattedMessage();
  }

  public static LogEvent getEvent(String loggerName, int index) {

    List<LogEvent> newEvents = getEvents(loggerName);

    if (newEvents.size() < index) {
      return null;
    }

    return newEvents.get(index);
  }

  public static List<LogEvent> getEvents(String loggerName) {
    return events.stream().filter(lg -> loggerName.equals(lg.getLoggerName())).collect(Collectors.toList());
  }

  public static int getNumEvents(String loggerName) {
    return events.stream().filter(lg -> loggerName.equals(lg.getLoggerName())).collect(Collectors.toList()).size();
  }

  public static List<LogEvent> getEvents() {
    return events;
  }


  // Your custom appender needs to declare a factory method
  // annotated with `@PluginFactory`. Log4j will parse the configuration
  // and call this factory method to construct an appender instance with
  // the configured attributes.
  @PluginFactory
  public static TestLogAppender createAppender(
      @PluginAttribute("name") String name,
      @PluginElement("Layout") Layout<? extends Serializable> layout,
      @PluginElement("Filter") final Filter filter,
      @PluginAttribute("otherAttribute") String otherAttribute) {

    if (name == null) {
      LOGGER.error("No name provided for TestLogAppender");
      return null;
    }
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }

    return getInstance(name, filter, layout, true);
  }
}