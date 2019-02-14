package se.skltp.agp.cache;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.TakCacheLog;

import java.io.UnsupportedEncodingException;

public class ResetTakCache implements Callable {

    private static final Logger LOG = LoggerFactory.getLogger(ResetTakCache.class);
    private TakCache takCache;
    private String tjanstekontrakt;

    public void setTakCache(TakCache takCache) {
        this.takCache = takCache;
    }

    public void setTjanstekontrakt(String tjanstekontrakt) {
        this.tjanstekontrakt = tjanstekontrakt;
    }

    public Object onCall(final MuleEventContext eventContext) throws UnsupportedEncodingException {
        final String content = resetCache();
        setResponceProperties(eventContext, content);
        eventContext.setStopFurtherProcessing(true);
        return content;
    }

    private String resetCache() {
        TakCacheLog takCacheLog = takCache.refresh(tjanstekontrakt);
        return getResultAsString(takCacheLog);
    }

    private void setResponceProperties(final MuleEventContext eventContext, final String content) throws UnsupportedEncodingException {
        eventContext.getMessage().clearProperties(PropertyScope.INVOCATION);
        eventContext.getMessage().setProperty("Content-Length",
                Integer.toString(content.getBytes(eventContext.getEncoding()).length),
                PropertyScope.INBOUND);
        eventContext.getMessage().setProperty("Content-Type", "text/html; charset=" + eventContext.getEncoding(),
                PropertyScope.INBOUND);
    }


    private String getResultAsString(TakCacheLog takCacheLog) {
        StringBuilder resultAsString = new StringBuilder();
        for (String processingLog : takCacheLog.getLog()) {
            resultAsString.append("<br>").append(processingLog);
        }
        return resultAsString.toString();
    }
}
