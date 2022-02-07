package se.skltp.aggregatingservices.processors;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Log4j2
public class PreStopProcessor implements Processor {
    private File preStopFile;

    @Autowired
    private AtomicBoolean preStopStatus;

    public void setPreStopFile(File preStopFile) {
        this.preStopFile = preStopFile;
    }

    public void process(Exchange exchange)  {
        boolean fileExists = preStopFile.exists();
        log.warn("PreStopFile changed, PreStopStatus {}.", fileExists);
        preStopStatus.set(fileExists);
    }
}
