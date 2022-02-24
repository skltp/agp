package se.skltp.aggregatingservices.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.processors.PreStopProcessor;

import java.io.File;

@Component
public class PreStopRoute extends RouteBuilder {

  public static final String PRE_STOP_ROUTE = "prestop-route";

  @Autowired
  PreStopProcessor preStopProcessor;

  @Value("${agp.prestop.file.path:#{null}}")
  String preStopFilePath;

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

    preStopProcessor.setPreStopFile(preStopFile);
    String fileName = preStopFile.getName();
    String directoryName = directory.toString();
    log.info("Watching {} for preStopFile {}", directoryName, fileName);

    String fileWatchUri = String.format("file-watch:%s?antInclude=%s", directoryName, fileName);

    from(fileWatchUri)
            .routeId(PRE_STOP_ROUTE)
            .process(preStopProcessor);
  }
}