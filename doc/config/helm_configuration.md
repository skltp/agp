# App-of-Apps Helm Configuration Guide

This guide explains how to deploy **AGP (Aggregeringsplattform)** using the **app-of-apps** pattern with ArgoCD. It is aimed at operators setting up a new environment from scratch.

For per-key documentation of all AGP Helm values, see [Helm Values Reference].

---

## 1. The App-of-Apps Pattern

The SKLTP platform uses the [ArgoCD App-of-Apps](https://argo-cd.readthedocs.io/en/stable/operator-manual/cluster-bootstrapping/#app-of-apps-pattern) approach to manage multiple applications from a single Git repository.

### How It Works

You create a **central repository** (the "app-of-apps" repo) containing a Helm chart. When rendered, this chart produces an ArgoCD **ApplicationSet** resource that drives the deployment of all platform services — including AGP.

The **ApplicationSet** uses a **list generator** that iterates over an `applications[]` list in `values.yaml`. For each entry it:

1. Reads `valuefiles/common-values.yaml` (shared settings: image registry, ingress hostnames, SKLTP instance ID).
2. Reads `valuefiles/<name>-values.yaml` (application-specific overrides).
3. Merges both into the `helm.values` field of the generated ArgoCD Application.
4. Points ArgoCD at the application's own Git repository + `helm/` path + pinned tag.

ArgoCD then renders each application's Helm chart (e.g. `agp/helm/`) with the merged values and syncs the resulting Kubernetes resources to the target cluster.

### Value Precedence (highest → lowest)

1. `valuefiles/agp-values.yaml` (environment-specific overrides)
2. `valuefiles/common-values.yaml` (shared across all apps)
3. `agp/helm/values.yaml` (chart defaults in the AGP repository)

---

## 2. Setting Up Your App-of-Apps Repository

Create a new Git repository with the following structure:

```
my-platform-apps/
├── Chart.yaml
├── values.yaml
├── valuefiles/
│   ├── common-values.yaml
│   └── agp-values.yaml
└── templates/
    ├── applicationset.yaml
    ├── configmaps/
    │   ├── common-configmap.yaml
    │   └── agp-configmap.yaml
    └── secrets/
        └── (SealedSecrets or references)
```

### 2.1 `Chart.yaml`

```yaml
apiVersion: v2
name: my-platform-applicationset
description: App-of-apps chart for deploying SKLTP services
type: application
version: 0.1.0
appVersion: "0.0.1"
```

### 2.2 `values.yaml` — Cluster & Application List

This is the top-level values file for your app-of-apps chart. It defines the target cluster/namespace and lists which applications to deploy.

```yaml
destination:
  cluster: a                              # CHANGE: cluster identifier
  environment: myenv                      # CHANGE: environment name (dev, qa, prod, etc.)
  project: my-platform-project            # CHANGE: ArgoCD project name
  namespace: my-platform-myenv            # CHANGE: target Kubernetes namespace
  server: https://kubernetes.default.svc

repo:
  path: helm                              # Path within each app repo where Helm chart lives

applications:
- name: agp
  repourl: https://github.com/skltp/agp.git
  targetrevision: v4.1.0                  # CHANGE: pin to desired AGP release tag
```

### 2.3 `templates/applicationset.yaml` — The List Generator

This is the core template that generates one ArgoCD Application per entry in `applications[]`. It merges `common-values.yaml` and the per-app values file into the Helm values for each application.

```yaml
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: {{ .Chart.Name }}-{{ .Values.destination.environment }}
  namespace: argocd
spec:
  generators:
  - list:
      elements:
      {{- range .Values.applications }}
      - application: {{ .name }}-{{ $.Values.destination.environment }}
        repourl: {{ .repourl }}
        targetrevision: {{ .targetrevision }}
        app-values: | {{ $.Files.Get "valuefiles/common-values.yaml" | nindent 10 }}
          {{ $.Files.Get (printf "valuefiles/%s-values.yaml" .name) | nindent 10 }}
      {{- end }}
  template:
    metadata:
      name: '{{`{{application}}`}}'
    spec:
      destination:
        namespace: {{ .Values.destination.namespace }}
        server: {{ .Values.destination.server }}
      project: {{ .Values.destination.project }}
      source:
        repoURL: '{{`{{repourl}}`}}'
        path: {{ .Values.repo.path }}
        targetRevision: '{{`{{targetrevision}}`}}'
        helm:
          values: '{{`{{app-values}}`}}'
```

> **Key points about this template:**
> - It uses `$.Files.Get` to read the value files from your app-of-apps repo and inject them as inline Helm values.
> - The double-brace escaping (`{{` `` ` `` `{{...}}` `` ` `` `}}`) is required because the inner `{{application}}`, `{{repourl}}`, etc. are ArgoCD ApplicationSet template parameters — not Go template expressions.
> - Each application gets its own ArgoCD Application resource pointing at the application's own Git repo and Helm chart.

---

## 3. AGP Architecture — Init Container + Plugin Loading

Unlike simple single-container deployments, AGP uses a two-stage startup:

| Stage       | Container         | Role                                                                                                      |
|-------------|-------------------|-----------------------------------------------------------------------------------------------------------|
| **Init**    | `agp-init`        | Downloads aggregating-service plugin JARs from a Maven repository into an `emptyDir` volume.              |
| **Runtime** | `agp-application` | Spring Boot app that loads plugins from the shared volume via `-Dloader.path` and exposes SOAP endpoints. |

Both containers share an `emptyDir` volume (`services`) where the init container deposits plugin JARs. The runtime container picks them up at startup.

### Key Ports

| Port      | Purpose                                                       |
|-----------|---------------------------------------------------------------|
| 9001–90xx | Aggregating service SOAP endpoints (one per service contract) |
| 8080      | Status endpoint                                               |
| 8089      | Actuator / management (health, metrics, Prometheus)           |
| 8091      | Reset cache endpoint                                          |

---

## 4. Minimal AGP Deployment Configuration

### 4.1 `valuefiles/common-values.yaml` — Shared Values

Settings consumed by AGP and potentially other SKLTP services you deploy:

```yaml
repository: registry.example.com/skltp/        # CHANGE: your container registry prefix

ingressroute:
  esbHostName: esb.myenv.example.se            # CHANGE: public ESB hostname
  sjunetHostName: esb.myenv.sjunet.org         # CHANGE: Sjunet hostname
  bksVpHostName: esb.myenv.example.se          # CHANGE: Hostname if accessing the cluster directly

skltp:
  instanceId: MY-PLATFORM_ID                   # CHANGE: ID of this SKLTP instance
```

### 4.2 `valuefiles/agp-values.yaml` — AGP-Specific Overrides

Minimum overrides for AGP:

| Concern               | Keys to set                                                                       |
|-----------------------|-----------------------------------------------------------------------------------|
| Scaling               | `deployment.replicaCount`, `deployment.resources`                                 |
| Image                 | `container.image.tag`, `deployment.imagePullPolicy`                               |
| Environment variables | `environment.variables.config_maps`, `environment.variables.secrets`              |
| VIP service name      | `vip.name`                                                                        |
| Plugin artifacts      | `aggServices.repository`, `aggServices.artifacts[]`, `aggServices.ports[]`        |
| Startup probe tuning  | `probes.startupProbe.initialDelaySeconds`, `probes.startupProbe.failureThreshold` |
| Log levels            | `log4j.rootLoggerLevel`, `log4j.loggers[]`                                        |

See section 5 for the full example.

### 4.3 Kubernetes Resources (Created via `templates/`)

These must exist in the target namespace before (or alongside) the AGP deployment. Create them as additional templates in your app-of-apps chart:

| Resource                     | Purpose                                                                                 |
|------------------------------|-----------------------------------------------------------------------------------------|
| `ConfigMap/common-configmap` | Shared settings (e.g. TAK endpoint address) used across services.                       |
| `ConfigMap/agp-configmap`    | AGP-specific overrides: VP URL, EI FindContent URL, JAVA_OPTS, validation log settings. |
| `Secret/regcred`             | Image-pull credentials for the container registry.                                      |

> **Note:** Secrets should be provisioned via SealedSecrets, external-secrets-operator, or your organization's secret management solution. Never commit plaintext secrets to Git.

#### About `regcred` (Image-Pull Secret)

The `regcred` Secret is a Kubernetes `kubernetes.io/dockerconfigjson` secret that stores credentials for authenticating against the container image registry. Without it, the kubelet cannot pull the AGP container images and pods will fail with `ErrImagePull` / `ImagePullBackOff`.

The AGP Helm chart references this secret via `imagePullSecrets`:

```yaml
imagePullSecrets:
  - name: regcred
```

**Creating `regcred` manually** (for testing/bootstrapping):

```bash
kubectl create secret docker-registry regcred \
  --namespace=<your-namespace> \
  --docker-server=registry.example.com \
  --docker-username=<service-account> \
  --docker-password=<token-or-password>
```

**In production**, use SealedSecrets or an external-secrets-operator to manage this secret declaratively. The secret must exist in the same namespace as the AGP Deployment. If your cluster uses a shared image-pull secret at the ServiceAccount level, you can set `imagePullSecrets: []` in the values file to skip per-pod configuration.

---

## 5. Complete Minimal App-of-Apps Example

Below is a self-contained set of all files needed in your app-of-apps repository to deploy AGP. Each file is separated by `---` with a header comment.

> Replace placeholder values (marked with `# CHANGE`) with your environment-specific settings.

```yaml
##############################################################################
# FILE: Chart.yaml
##############################################################################
apiVersion: v2
name: my-platform-applicationset
description: App-of-apps chart for deploying SKLTP services
type: application
version: 0.1.0
appVersion: "0.0.1"
---
##############################################################################
# FILE: values.yaml — Cluster & application list
##############################################################################
destination:
  cluster: a                                    # CHANGE: cluster identifier
  environment: myenv                            # CHANGE: environment name
  project: my-platform-project                  # CHANGE: ArgoCD project
  namespace: my-platform-myenv                  # CHANGE: target namespace
  server: https://kubernetes.default.svc

repo:
  path: helm

applications:
- name: agp
  repourl: https://github.com/skltp/agp.git
  targetrevision: v4.1.0                       # CHANGE: desired AGP version
---
##############################################################################
# FILE: templates/applicationset.yaml — The list generator
##############################################################################
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: {{ .Chart.Name }}-{{ .Values.destination.environment }}
  namespace: argocd
spec:
  generators:
  - list:
      elements:
      {{- range .Values.applications }}
      - application: {{ .name }}-{{ $.Values.destination.environment }}
        repourl: {{ .repourl }}
        targetrevision: {{ .targetrevision }}
        app-values: | {{ $.Files.Get "valuefiles/common-values.yaml" | nindent 10 }}
          {{ $.Files.Get (printf "valuefiles/%s-values.yaml" .name) | nindent 10 }}
      {{- end }}
  template:
    metadata:
      name: '{{`{{application}}`}}'
    spec:
      destination:
        namespace: {{ .Values.destination.namespace }}
        server: {{ .Values.destination.server }}
      project: {{ .Values.destination.project }}
      source:
        repoURL: '{{`{{repourl}}`}}'
        path: {{ .Values.repo.path }}
        targetRevision: '{{`{{targetrevision}}`}}'
        helm:
          values: '{{`{{app-values}}`}}'
---
##############################################################################
# FILE: valuefiles/common-values.yaml — Shared values for all applications
##############################################################################
repository: registry.example.com/skltp/         # CHANGE: your registry prefix

ingressroute:
  esbHostName: esb.myenv.example.se            # CHANGE
  sjunetHostName: esb.myenv.sjunet.org         # CHANGE
  bksVpHostName: esb.myenv.example.se          # CHANGE

skltp:
  instanceId: MY-PLATFORM_ID                    # CHANGE: SKLTP instance identity
---
##############################################################################
# FILE: valuefiles/agp-values.yaml — AGP-specific overrides
##############################################################################
deployment:
  replicaCount: 2
  imagePullPolicy: IfNotPresent
  elasticGrokFilter: camel
  resources:
    limits:
      cpu: 750m
      memory: 2Gi
    requests:
      cpu: 500m
      memory: 2Gi
  topologySpreadConstraints:
    - labelSelector:
        matchLabels:
          app: agp
      maxSkew: 1
      topologyKey: kubernetes.io/hostname
      whenUnsatisfiable: ScheduleAnyway

container:
  image:
    tag:                                        # CHANGE: image tag (defaults to chart appVersion)

probes:
  startupProbe:
    initialDelaySeconds: 60
    failureThreshold: 24

environment:
  variables:
    config_maps:
      - common-configmap
      - agp-configmap
    secrets:

# Environment-specific backwards-compatible service name
vip:
  name: ind-myenv-agp-vip                       # CHANGE: legacy service name

# Aggregating services: maven repository, artifacts, and service ports
aggServices:
  repository: https://nexus.example.com/repository/maven-public/  # CHANGE
  artifacts:
    - groupId: se.skltp.aggregatingservices.riv.clinicalprocess.healthcond.description
      artifactId: GetAggregatedCareDocumentation-v3-main
      version: 2.0.1                            # CHANGE: plugin version
    - groupId: se.skltp.aggregatingservices.riv.clinicalprocess.logistics.logistics
      artifactId: GetAggregatedCareContacts-v3-main
      version: 2.0.1                            # CHANGE: plugin version
    # Add more plugin artifacts as needed...
  ports:
    - 9001
    - 9002
    # One port per aggregating service...

log4j:
  rootLoggerLevel: WARN
  loggers:
    - name: se.skltp.aggregatingservices
      level: WARN
    - name: se.skltp.takcache
      level: INFO
    - name: se.skltp.aggregatingservices.AgpApplication
      level: INFO
    - name: org.apache.camel
      level: INFO
    - name: se.skltp.aggregatingservices.logging
      level: INFO
    - name: se.skltp.aggregatingservices.logging.FindContentResponderInterface
      level: INFO
---
##############################################################################
# FILE: templates/configmaps/common-configmap.yaml
##############################################################################
apiVersion: v1
kind: ConfigMap
metadata:
  name: common-configmap
  namespace: {{ .Values.destination.namespace }}
data:
  TAKCACHE_ENDPOINT_ADDRESS: "http://tak-services-svc:8080/tak-services/SokVagvalsInfo/v2"
---
##############################################################################
# FILE: templates/configmaps/agp-configmap.yaml
##############################################################################
apiVersion: v1
kind: ConfigMap
metadata:
  name: agp-configmap
  namespace: {{ .Values.destination.namespace }}
data:
  # VP service URL (fully qualified in-cluster address)
  VP_DEFAULTSERVICEURL: "http://vp.my-platform-myenv.svc.cluster.local:8080/vp"  # CHANGE

  # EI FindContent service URL (fully qualified in-cluster address)
  EI_FINDCONTENTURL: "http://ei-backend.my-platform-myenv.svc.cluster.local:8082/skltp-ei/find-content-service/v1"  # CHANGE

  # JVM options: CXF child-element limit for large payloads, memory settings
  JAVA_OPTS: '-Dorg.apache.cxf.stax.maxChildElements=180000 -XX:MaxRAMPercentage=65 -Xmx1536m'

  # Schema validation logging (comma-separated list of services to validate, empty = none)
  VP_VALIDATIONLOG_SERVICES: ''
---
##############################################################################
# FILE: templates/secrets/regcred.yaml (placeholder — use SealedSecret)
##############################################################################
# apiVersion: bitnami.com/v1alpha1
# kind: SealedSecret
# metadata:
#   name: regcred
#   namespace: {{ .Values.destination.namespace }}
# spec:
#   encryptedData:
#     .dockerconfigjson: <sealed-value>
#   template:
#     type: kubernetes.io/dockerconfigjson
```

---

## 6. Deployment Workflow

1. **Create your app-of-apps repository** — Use the structure and files from section 5.
2. **Provision secrets** — Create SealedSecrets (or use your secrets operator) for image-pull credentials.
3. **Register in ArgoCD** — Create an ArgoCD Application that points at your app-of-apps repository (the "root" application). ArgoCD will render the chart, producing the ApplicationSet.
4. **Sync** — ArgoCD detects the ApplicationSet, generates one Application per entry in `applications[]`, renders each app's Helm chart with the merged values, and applies the resources to the cluster.
5. **Verify** — Check pod status, Actuator health (`/actuator/health`), TAK cache initialisation logs, and that the init container successfully downloaded all plugin artifacts.

### Registering the Root Application in ArgoCD

The "root application" is the single ArgoCD Application that bootstraps everything else. It tells ArgoCD where your app-of-apps repository lives and how to render it. Without this, ArgoCD has no knowledge of your chart.

You can create the root application declaratively or via the ArgoCD UI/CLI:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: my-platform-apps
  namespace: argocd
spec:
  project: my-platform-project                                       # CHANGE: must exist in ArgoCD
  source:
    repoURL: https://git.example.com/my-org/my-platform-apps.git     # CHANGE: your app-of-apps repo
    path: .                                                          # Chart.yaml is at the repo root
    targetRevision: main                                             # CHANGE: branch or tag to track
  destination:
    server: https://kubernetes.default.svc                           # The cluster ArgoCD runs on
    namespace: argocd                                                # ApplicationSet is created here
  syncPolicy:
    automated:
      prune: true       # Remove resources ArgoCD no longer manages
      selfHeal: true    # Revert manual drift automatically
```

Once this root Application is synced, ArgoCD renders your `Chart.yaml` + `values.yaml` + templates, producing the ApplicationSet which in turn creates the AGP Application (and any other applications you list).

---

## 7. Additional Override Examples

### 7.1 Resource Limits

```yaml
deployment:
  replicaCount: 6
  resources:
    limits:
      cpu: 750m
      memory: 2Gi
    requests:
      cpu: 500m
      memory: 2Gi
```

### 7.2 Log Levels

```yaml
log4j:
  rootLoggerLevel: WARN
  loggers:
    - name: se.skltp.aggregatingservices
      level: INFO
    - name: se.skltp.takcache
      level: INFO
    - name: se.skltp.aggregatingservices.logging
      level: INFO
    - name: se.skltp.aggregatingservices.logging.FindContentResponderInterface
      level: DEBUG
    - name: org.apache.camel
      level: WARN
```

### 7.3 Aggregating Service Plugin Artifacts

The `aggServices.artifacts` list defines which aggregating-service plugins the init container downloads. Each entry maps to a Maven artifact:

```yaml
aggServices:
  repository: https://nexus.example.com/repository/maven-public/
  artifacts:
    - groupId: se.skltp.aggregatingservices.riv.clinicalprocess.activity.actions
      artifactId: GetAggregatedActivities-v1-main
      version: 3.0.1
    - groupId: se.skltp.aggregatingservices.riv.clinicalprocess.healthcond.description
      artifactId: GetAggregatedCareDocumentation-v3-main
      version: 2.0.1
    - groupId: se.skltp.aggregatingservices.riv.clinicalprocess.logistics.logistics
      artifactId: GetAggregatedCareContacts-v3-main
      version: 2.0.1
```

Each plugin exposes a SOAP endpoint on a unique port. Add corresponding ports to `aggServices.ports`:

```yaml
  ports:
    - 9001
    - 9002
    - 9003
```

> **Important:** The number of ports must match the number of aggregating services. Each service binds to one port in order.

### 7.4 Per-Service Timeout Overrides

Some aggregating services may need shorter or longer timeouts than the global defaults:

```yaml
serviceOverrides:
  getAggregatedRequestActivities_v1:
    receiveTimeout: "15000"
  getAggregatedRequestActivities_v2:
    receiveTimeout: "15000"
  getAggregatedSubjectOfCareSchedule_v1:
    receiveTimeout: "15000"
```

These override the global `vp.defaultReceiveTimeout` for calls to specific service contracts.

### 7.5 Global Timeout Configuration

```yaml
# Total time allowed for the complete aggregation (EI lookup + all producer calls)
aggregate:
  timeout: "28000"

# Default timeouts for outbound calls to producers via VP
vp:
  defaultReceiveTimeout: "27000"
  defaultConnectTimeout: "2000"

# Timeouts for calling EI FindContent
ei:
  connectTimeout: "2000"
  receiveTimeout: "20000"
```

### 7.6 CXF Tuning via JAVA_OPTS

If large payloads are expected, increase the CXF child-element limit in the `agp-configmap`:

```yaml
# In templates/configmaps/agp-configmap.yaml:
data:
  JAVA_OPTS: '-Dorg.apache.cxf.stax.maxChildElements=180000 -XX:MaxRAMPercentage=65 -Xmx1536m'
```

### 7.7 Startup Probe Tuning

AGP loads many plugins at startup which can take time. Adjust the startup probe to allow sufficient boot time:

```yaml
probes:
  startupProbe:
    initialDelaySeconds: 60
    failureThreshold: 24    # 60s + 24 * 5s = up to 180s boot time
```

### 7.8 Topology Spread Constraints

Spread pods across nodes for high availability:

```yaml
deployment:
  topologySpreadConstraints:
    - labelSelector:
        matchLabels:
          app: agp
      maxSkew: 1
      topologyKey: kubernetes.io/hostname
      whenUnsatisfiable: ScheduleAnyway
```

---

## 8. AGP-Specific Operational Notes

### 8.1 Init Container & Plugin Downloads

The `agp-init` container uses a generated `pom.xml` (from the `aggServices.artifacts` list) and Maven `settings.xml` to download plugin JARs at pod startup. It caches downloads in a PersistentVolumeClaim (`agp-initrepo-pvc`) to speed up subsequent starts.

If a plugin download fails, the init container will exit non-zero and the pod will not start. Check init container logs:

```bash
kubectl logs <pod-name> -c agp-init --namespace=<namespace>
```

### 8.2 Reset Cache

AGP caches TAK (Tjänsteadressering och Katalog) data. To force a cache refresh without restarting:

- `POST http://<pod-ip>:8091/resetcache`

This endpoint is internal and not exposed via ingress.

### 8.3 ConfigMap Override Mechanism

The AGP Helm chart produces a built-in `agp-configmap-default` ConfigMap from chart values. Your external `agp-configmap` (defined in the app-of-apps repo) is loaded **after** the defaults, so any keys you set there will override the defaults.

This allows you to override VP URLs, EI URLs, JAVA_OPTS, and other settings without modifying the chart itself.

### 8.4 VP and EI Dependencies

AGP requires both VP and EI to be running and reachable:

- **VP** — Set `vp.defaultServiceURL` in the configmap to point at the VP service.
- **EI** — Set `ei.findContentUrl` in the configmap to point at the EI service.

Use fully qualified Kubernetes service names (e.g. `http://vp.<namespace>.svc.cluster.local:8080/vp`) for reliable in-cluster resolution.

---

## See Also

- [Helm Values Reference] — complete per-key documentation of `helm/values.yaml`.
- [Configuration Reference](config.md) — application-level properties documentation.
- [Service Configuration](service_config.md) — per-service plugin configuration details.
- [Logging Configuration](logging_configuration.md) — Log4j2 configuration and format.

[//]: # (Reference links)

[Helm Values Reference]: <helm_values.md>

