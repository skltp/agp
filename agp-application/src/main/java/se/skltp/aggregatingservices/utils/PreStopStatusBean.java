package se.skltp.aggregatingservices.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Log4j2
public class PreStopStatusBean {
    private File preStopFile;
    private boolean preStopStatus;

    public void setPreStopFile(File preStopFile) {
        this.preStopFile = preStopFile;
    }

    public boolean getPreStopStatus() {
        return this.preStopStatus;
    }

    public void checkPreStopFile() {
        if (preStopFile.exists()) {
            log.warn("PreStopFile found, setting PreStopStatus active.");
            preStopStatus = true;
        }
        else {
            log.warn("checkPreStopFile called, PreStopFile not found.");
        }
    }



}
