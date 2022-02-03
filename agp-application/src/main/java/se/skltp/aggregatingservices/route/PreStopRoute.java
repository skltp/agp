package se.skltp.aggregatingservices.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.utils.PreStopStatusBean;

import java.io.File;

@Component
public class PreStopRoute extends RouteBuilder {

  public static final String PRE_STOP_ROUTE = "prestop-route";

  @Autowired
  PreStopStatusBean preStopStatusBean;

  private String preStopFilePath = "/var/spool/agp-camel/preStopFile";

  @Override
  public void configure() {
    log.info("preStopFilePath {}", preStopFilePath);
    if (preStopFilePath == null || preStopFilePath.length() == 0) return;

    File preStopFile = new File(preStopFilePath);
    File directory = preStopFile.getParentFile();
    if (!directory.exists() || !directory.isDirectory()) {
      log.error("PreStopFile directory {} does not exist.", directory);
      return;
    }

    preStopStatusBean.setPreStopFile(preStopFile);
    String fileName = preStopFile.getName();
    String directoryName = directory.toString();
    log.info("Watching {} for preStopFile {}", directoryName, fileName);

    String fileWatchUri = String.format("file-watch:%s?antInclude=%s", directoryName, fileName);

    from(fileWatchUri)
            .routeId(PRE_STOP_ROUTE)
            .bean(preStopStatusBean, "checkPreStopFile");
  }
}