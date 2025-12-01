package se.skltp.aggregatingservices.logging;

import org.apache.cxf.message.Message;

import java.util.List;

public interface ValidationLogger {
  void addErrors(List<String> errors, String service, Message message);

  void flush();

  void register(String serviceName);
}
