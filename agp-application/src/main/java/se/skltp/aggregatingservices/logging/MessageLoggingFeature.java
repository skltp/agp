package se.skltp.aggregatingservices.logging;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class MessageLoggingFeature extends AbstractFeature {

  private MessageInInterceptor loggingInInterceptor;
  private MessageOutInterceptor loggingOutInterceptor;

  public MessageLoggingFeature( @Value("${log.max.payload.size}") int maxPayloadSize) {
    MessageLogEventSender sender = new MessageLogEventSender();
    loggingInInterceptor = new MessageInInterceptor(sender);
    loggingOutInterceptor = new MessageOutInterceptor(sender);
    setLimit(maxPayloadSize);
  }

  @Override
  protected void initializeProvider(InterceptorProvider provider, Bus bus) {
    provider.getInInterceptors().add(this.loggingInInterceptor);
    provider.getInFaultInterceptors().add(this.loggingInInterceptor);
    provider.getOutInterceptors().add(this.loggingOutInterceptor);
    provider.getOutFaultInterceptors().add(this.loggingOutInterceptor);
  }

  public void setLimit(int limit) {
    this.loggingInInterceptor.setLimit(limit);
    this.loggingOutInterceptor.setLimit(limit);
  }

}
