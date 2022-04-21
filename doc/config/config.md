# Konfiguration av AGP

AGP är en spring-boot applikation och kan konfigureras enligt de sätt spring-boot föreskriver

För mer information om hur användare och lösenord för Hawtio konfigureras samt exempelfiler, se [Detaljerad konfiguration].
Loggning och hur det går till och kan konfigureras kan man läsa om här: [Loggning konfigurering]

### Application.properties ###
Spring-boot property fil som ligger under resources i jaren. Inställningarna kan överlagras enligt de sätt som Spring-boot föreskriver. 

|Nyckel|Defaultvärde/Exempel|Beskrivning|
|----|------------------|---------|
| server.port | 8881 | Spring-boot server port |
| management.endpoints.web.exposure.include | hawtio,jolokia | Default aktivera övervakning via Hawwtio |
| hawtio.authentication.enabled | false | Aktivera autentiserng för HawtIO? |
| hawtio.external.loginfile |  | Extern fil med user/password till Hawtio |
| endpoints.camelroutes.enabled | true | Medger tillgång till information om de Camel-routes som finns |
| endpoints.camelroutes.read-only | true | Tillgång till endpoints bara i read-only mode |
| agp.logicalAddress | 5565594230 | Egen logisk address till vilken rekursiva anrop inte är tillåtna |
| aggregate.timeout | 28000 | Timeout ms för aggregering av alla producent anrop |
| validate.soapAction | false | Ska inkommande SOAPAction header valideras |
| vp.instanceId | dev_env | VP's instans-ID |
| vp.defaultReceiveTimeout | 27000 | Timout ms för producent anrop, denna kan överridas per tjänst |
| vp.defaultConnectTimeout | 2000 | Connect timeout ms för producent anrop, denna kan överridas per tjänst |
| vp.defaultServiceURL | http://localhost:8083/vp | URL till vp för producent anrop, denna kan överridas per tjänst |
| ei.logicalAddress | 556500000 | EI's logiska adress |
| ei.senderId | SENDER1 | Sender id för anrop till EI/findContent |
| ei.findContentUrl | http://localhost:8082/findcontent | URL till EI/findContent |
| ei.connectTimeout | 2000 | Connect timeout ms mot EI/findContent |
| ei.receiveTimeout | 20000 | receive timeout ms mot EI/findContent |
| reset.cache.url | http://localhost:8091/resetcache | URL för att ladda om TAK cache |
| agp.status.url | http://localhost:1080/status | URL till status funktionen i AGP |
| takcache.endpoint.address | http://localhost:8085/tak/teststub/SokVagvalsInfo/v2 | URL till TAK information |
| log.max.payload.size | 49152 | Max storlek i bytes som loggas av payloaden  |
| headers.request.filter | (?i)SoapAction/x-skltp-prt/Server/Host | (regexp) Headers att filtrera i anrop till producent  |
| headers.response.filter | (?i)x-vp.*/x-rivta-original-serviceconsumer-hsaid/x-skltp-prt/User-Agent/breadcrumbId/Host/Server | (regexp) Headers att filtrera i svar till konsument  |

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

   [Loggning konfigurering]: <logging_configuration.md>
   [Detaljerad konfiguration]: <detail_config.md>
   [Konfiguration av tjänst]: <service_config.md>