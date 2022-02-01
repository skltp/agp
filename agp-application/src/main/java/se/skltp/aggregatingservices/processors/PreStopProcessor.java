package se.skltp.aggregatingservices.processors;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Log4j2
public class PreStopProcessor implements Processor {

    @Autowired
    CreateResponseProcessor createResponseProcessor;

    private File preStopFile;

    public void setPreStopFile(File preStopFile) {
        this.preStopFile = preStopFile;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        if (preStopFile.exists()) {
            log.info("PreStopFile found, closing connections.");
            createResponseProcessor.setCloseConnections(true);
        }
        else {
            log.debug("PreStopProcessor called, PreStopFile not found.");
        }
    }



}
