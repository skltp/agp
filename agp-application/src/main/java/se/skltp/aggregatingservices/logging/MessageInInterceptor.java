package se.skltp.aggregatingservices.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.ext.logging.WireTapIn;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Log4j2
public class MessageInInterceptor extends AbstractPhaseInterceptor {

  private MessageLogEventSender sender;

  protected int limit = 49152;

  public MessageInInterceptor(MessageLogEventSender sender) {
    super("pre-invoke");
    this.sender = sender;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  @Override
  public Collection<PhaseInterceptor<? extends Message>> getAdditionalInterceptors() {
    Collection<PhaseInterceptor<? extends Message>> ret = new ArrayList();
    ret.add(new WireTapIn(this.getWireTapLimit(), -1));
    return ret;
  }

  @Override
  public void handleMessage(Message message) {
    changeEncodingToUTF8(message);

    createExchangeId(message);
    final LogEntry event = LogEntryMapper.map(message);
    Logger logger = getLogger(event.getLogEvent());
    if (logger.isDebugEnabled()) {
      addPayload(message, event.getLogEvent());
    }
    sender.send(event, logger);
  }

  void changeEncodingToUTF8(Message message) {
    String encoding = (String) message.get(Message.ENCODING);

    if (StringUtils.isEmpty(encoding) || !encoding.equals(StandardCharsets.UTF_8.name())) {
      message.put(Message.ENCODING, StandardCharsets.UTF_8.name());
    }
  }

  protected Logger getLogger(final LogEvent event) {
    final String cat = "se.skltp.aggregatingservices.logging." + event.getPortTypeName().getLocalPart() + "." + event.getType();
    return LogManager.getLogger(cat);
  }



  private void addPayload(Message message, final LogEvent event) {
    try {
      CachedOutputStream cos = message.getContent(CachedOutputStream.class);
      if (cos != null) {
        handleOutputStream(event, message, cos);
      }
    } catch (IOException e) {
      throw new Fault(e);
    }
  }

  private void handleOutputStream(final LogEvent event, Message message, CachedOutputStream cos) throws IOException {
    String encoding = (String) message.get(Message.ENCODING);
    if (StringUtils.isEmpty(encoding)) {
      encoding = StandardCharsets.UTF_8.name();
    }
    StringBuilder payload = new StringBuilder();
    cos.writeCacheTo(payload, encoding, limit);
    cos.close();
    event.setPayload(payload.toString());
    boolean isTruncated = cos.size() > limit && limit != -1;
    event.setTruncated(isTruncated);
    event.setFullContentFile(cos.getTempFile());
  }

  public void createExchangeId(Message message) {
    Exchange exchange = message.getExchange();
    String exchangeId = (String) exchange.get("exchangeId");
    if (exchangeId == null) {
      exchange.put("exchangeId", UUID.randomUUID().toString());
    }
  }

  public int getWireTapLimit() {
    if (this.limit == -1 || this.limit == Integer.MAX_VALUE) {
      return this.limit ;
    }
    return this.limit+1;
  }
}
