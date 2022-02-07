package se.skltp.aggregatingservices.cxf;

import lombok.extern.log4j.Log4j2;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class CloseConnectionInterceptor extends AbstractPhaseInterceptor<Message> {

  private AtomicBoolean preStopStatus;

  public CloseConnectionInterceptor() {
    super(Phase.POST_PROTOCOL);
  }

  public CloseConnectionInterceptor(AtomicBoolean preStopStatus) {
    this();
    this.preStopStatus = preStopStatus;
  }

  @Override
  public void handleMessage(Message message) throws Fault {

    if (!preStopStatus.get()) return;

    try {
      Map<String, List> headers = CastUtils.cast((Map) message.get(Message.PROTOCOL_HEADERS));
      if (headers==null) {
        log.error("Headers are null");
        return;
      }
      headers.put("Connection", Collections.singletonList("close"));
    } catch (Exception e) {
      throw new Fault(e);
    }
  }
}