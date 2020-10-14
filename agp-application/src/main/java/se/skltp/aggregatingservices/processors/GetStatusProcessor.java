package se.skltp.aggregatingservices.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.impl.engine.DefaultRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;
import se.skltp.aggregatingservices.configuration.AgpServiceConfiguration;
import se.skltp.aggregatingservices.constants.AgpHeaders;
import se.skltp.aggregatingservices.service.TakCacheService;
import se.skltp.aggregatingservices.utils.MemoryUtil;
import se.skltp.takcache.TakCacheLog;

@Log4j2
@Service
public class GetStatusProcessor implements Processor {

  public static final String KEY_APP_NAME = "Name";
  public static final String KEY_APP_VERSION = "Version";
  public static final String KEY_APP_BUILD_TIME = "BuildTime";
  public static final String KEY_SERVICE_STATUS = "ServiceStatus";
  public static final String KEY_UPTIME = "Uptime";
  public static final String KEY_MANAGEMENT_NAME = "ManagementName";
  public static final String KEY_JAVA_VERSION = "JavaVersion";
  public static final String KEY_CAMEL_VERSION = "CamelVersion";
  public static final String KEY_TAK_CACHE_INITIALIZED = "TakCacheInitialized";
  public static final String KEY_TAK_CACHE_RESET_INFO = "TakCacheResetInfo";
  public static final String KEY_JVM_TOTAL_MEMORY = "JvmTotalMemory";
  public static final String KEY_JVM_FREE_MEMORY = "JvmFreeMemory";
  public static final String KEY_JVM_USED_MEMORY = "JvmUsedMemory";
  public static final String KEY_JVM_MAX_MEMORY = "JvmMaxMemory";
  public static final String KEY_DIRECT_MEMORY = "DirectMemBufferPool";
  public static final String KEY_NON_HEAP_MEMORY = "NonHeapMemory";
  public static final String KEY_VM_MAX_DIRECT_MEMORY = "MaxDirectMemory";
  public static final String KEY_ENDPOINTS = "Endpoints";
  public static final String KEY_SERVICE_IMPLEMENTATIONS = "ServiceImplementations";

  @Autowired
  private CamelContext camelContext;

  @Autowired
  TakCacheService takService;

  @Autowired
  BuildProperties buildProperties;

  Map<String, String> implementationVersions;

  @Autowired
  public GetStatusProcessor(List<AgpServiceConfiguration> serviceConfigurations) {
    if (serviceConfigurations != null) {
      implementationVersions = new HashMap<>();

      for (AgpServiceConfiguration sc : serviceConfigurations) {
        final String implementationTitle = sc.getClass().getPackage().getImplementationTitle();
        final String implementationVersion = sc.getClass().getPackage().getImplementationVersion();
        log.info("ServiceConfiguration class: {}\npackage: {}\ntitle: {}\nversion: {}",
            sc.getClass().getName(),
            sc.getClass().getPackage().getName(),
            implementationTitle,
            implementationVersion);
        implementationVersions.put(implementationTitle, implementationVersion);
      }
    }
  }

  @Override
  public void process(Exchange exchange) {
    boolean showMemory = exchange.getIn().getHeaders().containsKey("memory");
    Map<String, Object> map = registerInfo(showMemory);

    String json = null;
    try {
      ObjectMapper mapper = new ObjectMapper();
      DefaultPrettyPrinter p = new DefaultPrettyPrinter();
      p.indentArraysWith(new DefaultIndenter().withLinefeed(System.lineSeparator()));
      mapper.setDefaultPrettyPrinter(p);
      json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
    } catch (JsonProcessingException e) {
      log.error("Error parsing Map to Json in GetStatusProcessor. Sending orinary string.");
      json = map.toString();
    }
    exchange.getIn().setBody(json.replace("\\/", "/"));
    exchange.getIn().getHeaders().put(AgpHeaders.HEADER_CONTENT_TYPE, "application/json");
  }

  private Map<String, Object> registerInfo(boolean showMemory) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();

    map.put(KEY_APP_NAME, buildProperties.getName());
    map.put(KEY_APP_VERSION, buildProperties.getVersion());
    map.put(KEY_APP_BUILD_TIME, parseTimeValues(buildProperties.getTime()));

    ServiceStatus serviceStatus = camelContext.getStatus();
    map.put(KEY_SERVICE_STATUS, "" + serviceStatus);
    map.put(KEY_UPTIME, camelContext.getUptime());
    map.put(KEY_MANAGEMENT_NAME, camelContext.getManagementName());
    map.put(KEY_JAVA_VERSION, (String) System.getProperties().get("java.version"));
    map.put(KEY_CAMEL_VERSION, camelContext.getVersion());

    map.put(KEY_TAK_CACHE_INITIALIZED, "" + takService.isInitalized());
    map.put(KEY_TAK_CACHE_RESET_INFO, getTakRefreshInfo());

    Runtime instance = Runtime.getRuntime();
    map.put(KEY_JVM_TOTAL_MEMORY, "" + MemoryUtil.bytesReadable(instance.totalMemory()));
    map.put(KEY_JVM_FREE_MEMORY, "" + MemoryUtil.bytesReadable(instance.freeMemory()));
    map.put(KEY_JVM_USED_MEMORY, "" + MemoryUtil.bytesReadable((instance.totalMemory() - instance.freeMemory())));
    map.put(KEY_JVM_MAX_MEMORY, "" + MemoryUtil.bytesReadable(instance.maxMemory()));
    if (showMemory) {
      map.put(KEY_DIRECT_MEMORY, "" + getDirectMemoryString());
      map.put(KEY_VM_MAX_DIRECT_MEMORY, "" + MemoryUtil.getVMMaxMemory());
      map.put(KEY_NON_HEAP_MEMORY, "" + getNonHeapMemory());
    }
    map.put(KEY_ENDPOINTS, getEndpointInfo());

    if (implementationVersions != null) {
      map.put(KEY_SERVICE_IMPLEMENTATIONS, implementationVersions);
    }
    return map;
  }

  private String parseTimeValues(Instant time) {
    Date date = new Date(time.toEpochMilli());
    return getFormattedDate(date);
  }


  private String getNonHeapMemory() {
    MemoryUsage nonHeapMemoryUsage = MemoryUtil.getNonHeapMemoryUsage();

    return String.format("Init: %s Used: %s, Commited: %s, Max: %s",
        MemoryUtil.bytesReadable(nonHeapMemoryUsage.getInit()),
        MemoryUtil.bytesReadable(nonHeapMemoryUsage.getUsed()),
        MemoryUtil.bytesReadable(nonHeapMemoryUsage.getCommitted()),
        MemoryUtil.bytesReadable(nonHeapMemoryUsage.getMax()));
  }

  private String getDirectMemoryString() {
    return String.format("Used: %s, Count: %d, Max Capacity: %s",
        MemoryUtil.getMemoryUsed(),
        MemoryUtil.getCount(),
        MemoryUtil.getTotalCapacity());
  }

  private List getEndpointInfo() {
    List<String> endPoints = new ArrayList<>();
    List<Route> routes = camelContext.getRoutes();
    for (Route route : routes) {
      String endpoint = route.getEndpoint().getEndpointKey();
      if (endpoint.contains("http://") && ((DefaultRoute) route).getStatus() == ServiceStatus.Started) {
        String key = route.getEndpoint().getEndpointKey();
        if (key.indexOf('?') > -1) {
          key = key.substring(0, key.indexOf('?'));
        }
        endPoints.add(key);
      }
    }
    return endPoints;
  }

  public String getTakRefreshInfo() {
    TakCacheLog takCacheLog = takService.getLastRefreshLog();
    if (takCacheLog == null) {
      return "Not initialized";
    }

    return String.format("Date:%s Status:%s vagval:%d behorigheter:%d",
        getFormattedDate(takService.getLastResetDate()),
        takCacheLog.getRefreshStatus(),
        takCacheLog.getNumberVagval(),
        takCacheLog.getNumberBehorigheter());
  }

  private String getFormattedDate(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    return date == null ? "" : dateFormat.format(date);
  }

}

