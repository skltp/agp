# Helm Values Reference

This page documents every configurable value in `helm/values.yaml` for the AGP (Aggregeringsplattform) Helm chart.

---

## repository

| Key          | Description                                                                                                                                                                           |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `repository` | Container image registry prefix (e.g. `docker.drift.inera.se/ntjp/`). Prepended to the image name when constructing the full image reference. **Must be overridden per environment.** |

---

## deployment

General deployment settings for the AGP pod.

| Key                                        | Description                                                                                                                                                             |
|--------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `deployment.replicaCount`                  | Number of pod replicas to run.                                                                                                                                          |
| `deployment.imagePullPolicy`               | Kubernetes image pull policy (`Always`, `IfNotPresent`, `Never`).                                                                                                       |
| `deployment.elasticGrokFilter`             | Value injected as a label/annotation for Elastic log pipeline grok-filter matching.                                                                                     |
| `deployment.javaOpts`                      | JVM options passed to the container entry-point (e.g. `-XX:MaxRAMPercentage=65`).                                                                                       |
| `deployment.resources`                     | Kubernetes resource requests and limits (`cpu`, `memory`) for the main AGP container. Set to `{}` to omit. Structure follows the standard `requests`/`limits` format.   |
| `deployment.initResources`                 | Kubernetes resource requests and limits for the init container that downloads plugin artifacts.                                                                         |
| `deployment.initResources.limits.cpu`      | CPU limit for the init container.                                                                                                                                       |
| `deployment.initResources.limits.memory`   | Memory limit for the init container.                                                                                                                                    |
| `deployment.initResources.requests.cpu`    | CPU request for scheduling the init container.                                                                                                                          |
| `deployment.initResources.requests.memory` | Memory request for scheduling the init container.                                                                                                                       |
| `deployment.topologySpreadConstraints`     | List of Kubernetes [topology spread constraints](https://kubernetes.io/docs/concepts/scheduling-eviction/topology-spread-constraints/) to distribute pods across nodes. |

---

## skltp

| Key                | Description                                                                                                               |
|--------------------|---------------------------------------------------------------------------------------------------------------------------|
| `skltp.instanceId` | Unique identifier for this SKLTP instance. Used for correlation and instance identification. Maps to `SKLTP_INSTANCE_ID`. |

---

## vip

Backwards-compatible Kubernetes Service for environments that reference AGP by a legacy service name.

| Key        | Description                                                   |
|------------|---------------------------------------------------------------|
| `vip.name` | Name of the backwards-compatible Kubernetes Service resource. |

---

## aggServices

Maven repository, artifact list, and port definitions for the aggregating service plugins loaded at runtime.

| Key                      | Description                                                                                                                                                            |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `aggServices.repository` | URL of the Maven repository from which plugin artifacts are downloaded by the init container.                                                                          |
| `aggServices.artifacts`  | List of plugin artifacts to download. Each entry is a map with `groupId`, `artifactId`, and `version`. Set to `{}` to deploy no plugins. **Override per environment.** |
| `aggServices.ports`      | List of container ports exposed for inbound aggregating-service SOAP endpoints. Typically `[9001]`.                                                                    |

---

## server

| Key           | Description                                                                      |
|---------------|----------------------------------------------------------------------------------|
| `server.port` | Internal server port for the AGP Spring Boot application. Maps to `SERVER_PORT`. |

---

## paths

File-system paths inside the AGP container.

| Key          | Description                                               |
|--------------|-----------------------------------------------------------|
| `paths.base` | Base installation directory for AGP inside the container. |

---

## logging

| Key                      | Description                                                                                         |
|--------------------------|-----------------------------------------------------------------------------------------------------|
| `logging.config`         | Path to the Log4j2 configuration file inside the container.                                         |
| `logging.maxPayloadSize` | Maximum payload size (bytes) logged in message logging interceptors. Larger payloads are truncated. |

---

## management

Spring Boot Actuator / management endpoint configuration.

| Key                                      | Description                                                                                 |
|------------------------------------------|---------------------------------------------------------------------------------------------|
| `management.endpointsWebExposureInclude` | List of Actuator endpoint IDs to expose over HTTP (e.g. `health`, `metrics`, `prometheus`). |
| `management.endpointPrometheusEnabled`   | Enable the Prometheus metrics scrape endpoint (`true`/`false`).                             |

---

## camel

Apache Camel Spring Boot configurations.

| Key                    | Description                                                                               |
|------------------------|-------------------------------------------------------------------------------------------|
| `camel.messageHistory` | Enable or disable Camel message history (`true`/`false`). Disable for better performance. |
| `camel.tracing`        | Enable or disable Camel route tracing (`true`/`false`). Useful only for debugging.        |

---

## aggregate

| Key                 | Description                                                                                                 |
|---------------------|-------------------------------------------------------------------------------------------------------------|
| `aggregate.timeout` | Total timeout in milliseconds for the aggregated fan-out to all producers. Requests exceeding this are cut. |

---

## vp

Outgoing parameters for calling services via VP.

| Key                        | Description                                                                                                |
|----------------------------|------------------------------------------------------------------------------------------------------------|
| `vp.defaultReceiveTimeout` | Default HTTP read timeout in milliseconds for outbound producer calls via VP.                              |
| `vp.defaultConnectTimeout` | Default TCP connect timeout in milliseconds for outbound producer calls via VP.                            |
| `vp.defaultServiceUrl`     | Default base URL of the Virtuell Plattform endpoint used for producer calls. **Override per environment.** |
| `vp.useAynchHttpConduit`   | Use asynchronous CXF HTTP conduit for outbound VP calls (`true`/`false`).                                  |

---

## ei

Outgoing parameters for calling EI (Engagemangsindex) FindContent.

| Key                      | Description                                                                   |
|--------------------------|-------------------------------------------------------------------------------|
| `ei.logicalAddress`      | Logical address (HSA-ID) used in the SOAP header when calling EI FindContent. |
| `ei.findContentUrl`      | URL of the EI FindContent SOAP endpoint. **Override per environment.**        |
| `ei.connectTimeout`      | TCP connect timeout in milliseconds for the EI FindContent call.              |
| `ei.receiveTimeout`      | HTTP read timeout in milliseconds for the EI FindContent call.                |
| `ei.useAynchHttpConduit` | Use asynchronous CXF HTTP conduit for the EI call (`true`/`false`).           |

---

## aggServiceUrls

Inbound listener URLs for each aggregating service. Each key maps a service contract (name + version) to a local listener address. These URLs define where the CXF SOAP endpoints are exposed inside the container.

| Key                                                       | Description                                                |
|-----------------------------------------------------------|------------------------------------------------------------|
| `aggServiceUrls.getAggregatedActivities_v1`               | Listener URL for GetAggregatedActivities v1.               |
| `aggServiceUrls.getAggregatedAlertInformation_v2`         | Listener URL for GetAggregatedAlertInformation v2.         |
| `aggServiceUrls.getAggregatedAlertInformation_v3`         | Listener URL for GetAggregatedAlertInformation v3.         |
| `aggServiceUrls.getAggregatedCareContacts_v2`             | Listener URL for GetAggregatedCareContacts v2.             |
| `aggServiceUrls.getAggregatedCareContacts_v3`             | Listener URL for GetAggregatedCareContacts v3.             |
| `aggServiceUrls.getAggregatedCareDocumentation_v2`        | Listener URL for GetAggregatedCareDocumentation v2.        |
| `aggServiceUrls.getAggregatedCareDocumentation_v3`        | Listener URL for GetAggregatedCareDocumentation v3.        |
| `aggServiceUrls.getAggregatedCarePlans_v2`                | Listener URL for GetAggregatedCarePlans v2.                |
| `aggServiceUrls.getAggregatedDiagnosis_v2`                | Listener URL for GetAggregatedDiagnosis v2.                |
| `aggServiceUrls.getAggregatedECGOutcome_v1`               | Listener URL for GetAggregatedECGOutcome v1.               |
| `aggServiceUrls.getAggregatedFunctionalStatus_v2`         | Listener URL for GetAggregatedFunctionalStatus v2.         |
| `aggServiceUrls.getAggregatedImagingOutcome_v1`           | Listener URL for GetAggregatedImagingOutcome v1.           |
| `aggServiceUrls.getAggregatedLaboratoryOrderOutcome_v3`   | Listener URL for GetAggregatedLaboratoryOrderOutcome v3.   |
| `aggServiceUrls.getAggregatedLaboratoryOrderOutcome_v4`   | Listener URL for GetAggregatedLaboratoryOrderOutcome v4.   |
| `aggServiceUrls.getAggregatedMaternityMedicalHistory_v2`  | Listener URL for GetAggregatedMaternityMedicalHistory v2.  |
| `aggServiceUrls.getAggregatedMedicationHistory_v2`        | Listener URL for GetAggregatedMedicationHistory v2.        |
| `aggServiceUrls.getAggregatedObservations_v1`             | Listener URL for GetAggregatedObservations v1.             |
| `aggServiceUrls.getAggregatedReferralOutcome_v3`          | Listener URL for GetAggregatedReferralOutcome v3.          |
| `aggServiceUrls.getAggregatedRequestActivities_v1`        | Listener URL for GetAggregatedRequestActivities v1.        |
| `aggServiceUrls.getAggregatedRequestActivities_v2`        | Listener URL for GetAggregatedRequestActivities v2.        |
| `aggServiceUrls.getAggregatedRequestExemptionStatuses_v1` | Listener URL for GetAggregatedRequestExemptionStatuses v1. |
| `aggServiceUrls.getAggregatedSubjectOfCareSchedule_v1`    | Listener URL for GetAggregatedSubjectOfCareSchedule v1.    |
| `aggServiceUrls.getAggregatedVaccinationHistory_v1`       | Listener URL for GetAggregatedVaccinationHistory v1.       |
| `aggServiceUrls.getAggregatedVaccinationHistory_v2`       | Listener URL for GetAggregatedVaccinationHistory v2.       |

---

## otherServiceUrls

Internal management and status endpoints.

| Key                           | Description                                       |
|-------------------------------|---------------------------------------------------|
| `otherServiceUrls.resetCache` | Listener URL for the TAK/EI cache reset endpoint. |
| `otherServiceUrls.agpStatus`  | Listener URL for the AGP health/status endpoint.  |

---

## takcache

Connection and caching settings for the TAK (Tjänsteadresseringskatalogen) cache.

| Key                           | Description                                                                                               |
|-------------------------------|-----------------------------------------------------------------------------------------------------------|
| `takcache.useBehorighetCache` | Enable caching of *behörighet* (authorization) entries from TAK (`true`/`false`).                         |
| `takcache.useVagvalCache`     | Enable caching of *vägval* (routing) entries from TAK (`true`/`false`).                                   |
| `takcache.persistentFileName` | File path for the persisted local TAK cache. Used as fallback when the remote TAK service is unreachable. |
| `takcache.endpointAddress`    | URL of the TAK web service used to populate the local cache. **Override per environment.**                |

---

## serviceOverrides

Per-service timeout and configuration overrides. Each key is a service name (matching `aggServiceUrls` naming) and contains override properties.

| Key                                             | Description                                                              |
|-------------------------------------------------|--------------------------------------------------------------------------|
| `serviceOverrides.<serviceName>.receiveTimeout` | Override the VP receive (read) timeout in milliseconds for this service. |

Example:
```yaml
serviceOverrides:
  getAggregatedRequestActivities_v1:
    receiveTimeout: "15000"
```

---

## environment

ConfigMap and Secret references injected into the container as environment variables.

| Key                                          | Description                                                                                         |
|----------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `environment.variables._default_config_maps` | List of ConfigMap names whose keys are injected as environment variables by default.                |
| `environment.variables.config_maps`          | Additional ConfigMap names to inject. Override per environment.                                     |
| `environment.variables.secrets`              | Kubernetes Secret names whose keys are injected as environment variables. Override per environment. |

---

## log4j

Log4j2 logger configuration rendered into a ConfigMap-based `log4j2.xml`.

| Key                     | Description                                                                                                                               |
|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `log4j.loggers`         | List of logger entries. Each entry has a `name` (logger name / package) and a `level` (`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `OFF`). |
| `log4j.rootLoggerLevel` | Log level for the root logger. All loggers not explicitly listed inherit this level.                                                      |

---

## probes

Kubernetes health probes for the AGP container.

### probes.startupProbe

| Key                                       | Description                                                                                                             |
|-------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| `probes.startupProbe.httpGet.path`        | HTTP path to probe (Actuator readiness endpoint).                                                                       |
| `probes.startupProbe.httpGet.port`        | Named or numeric port to probe.                                                                                         |
| `probes.startupProbe.httpGet.scheme`      | Protocol scheme (`HTTP` or `HTTPS`).                                                                                    |
| `probes.startupProbe.initialDelaySeconds` | Seconds to wait before the first probe after container start.                                                           |
| `probes.startupProbe.periodSeconds`       | Seconds between probe attempts. Together with `failureThreshold`, defines max startup time (e.g. 30s + 18 × 5s = 120s). |
| `probes.startupProbe.timeoutSeconds`      | Seconds before a single probe attempt times out.                                                                        |
| `probes.startupProbe.successThreshold`    | Number of consecutive successes required to mark the container as started.                                              |
| `probes.startupProbe.failureThreshold`    | Number of consecutive failures before the container is restarted.                                                       |

### probes.livenessProbe

| Key                                        | Description                                                                                                         |
|--------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `probes.livenessProbe.httpGet.path`        | HTTP path to probe (Actuator liveness endpoint).                                                                    |
| `probes.livenessProbe.httpGet.port`        | Named or numeric port to probe.                                                                                     |
| `probes.livenessProbe.httpGet.scheme`      | Protocol scheme (`HTTP` or `HTTPS`).                                                                                |
| `probes.livenessProbe.initialDelaySeconds` | Seconds to wait before the first liveness probe.                                                                    |
| `probes.livenessProbe.periodSeconds`       | Seconds between liveness probes.                                                                                    |
| `probes.livenessProbe.timeoutSeconds`      | Seconds before a probe attempt times out.                                                                           |
| `probes.livenessProbe.failureThreshold`    | Consecutive failures before the container is killed and restarted (e.g. 4 × 10s + 5s timeout ≈ 45s non-responsive). |
| `probes.livenessProbe.successThreshold`    | Consecutive successes to clear a failed state.                                                                      |

### probes.readinessProbe

| Key                                         | Description                                                                                                |
|---------------------------------------------|------------------------------------------------------------------------------------------------------------|
| `probes.readinessProbe.httpGet.path`        | HTTP path to probe (Actuator readiness endpoint).                                                          |
| `probes.readinessProbe.httpGet.port`        | Named or numeric port to probe.                                                                            |
| `probes.readinessProbe.httpGet.scheme`      | Protocol scheme (`HTTP` or `HTTPS`).                                                                       |
| `probes.readinessProbe.initialDelaySeconds` | Seconds to wait before the first readiness probe.                                                          |
| `probes.readinessProbe.periodSeconds`       | Seconds between readiness probes.                                                                          |
| `probes.readinessProbe.timeoutSeconds`      | Seconds before a probe attempt times out.                                                                  |
| `probes.readinessProbe.failureThreshold`    | Consecutive failures before the pod is removed from service endpoints (e.g. 1 × 10s + 5s ≈ 15s until cut). |
| `probes.readinessProbe.successThreshold`    | Consecutive successes to mark the pod as ready.                                                            |

---

## See Also

- [Configuration](config.md) — Application-level property documentation.
- [Detailed Configuration](detail_config.md) — Extended configuration reference.
