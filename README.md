# AGP - Aggregeringsplattform
 Projektet är byggd med Spring-boot och Apache Camel.<br/>
 Aggregeringsplattformen är en körbar applikation som är plugin baserad där varje plugin exponerar en ingång för ett specifikt tjänstekontrakt.
 
 Vid uppstart läser aggregeringsplattformen in alla pluginer i sin classpath och exponerar en ingång för pluginens tjänstekontrakt.  
 När en konsument anroper tjänsten som exponeras av pluginen sker:
 1.  Aggregeringsplattformen tar ut person Id ur inkommande anrop.
 2. EI anropas för att hämta alla producenter där personen har engagemang.
 3. Alla producenter anropas och svar inväntas.
 4. Svaren aggregeras till ett hopsatt svar som returneras till konsumenten.
 
    
 ## Moduler
### agp-application
Innehåller en körbar spring-boot applikation

### agp-core
Innehåller de interface som varje plugin måste implementera.

### agp-init
Används för att skapa en init-container, för användning tillsammans med Helm chart.

### agp-schemas
Innehåller wsdl scheman för anrop mot EI

### agp-test-core
Innehåller hjälpklasser för unittester till pluginer

### agp-test-service
En test implementation av en plugin. Denna används i integrationstester av plattformen.

### agp-teststub
Innehåller en mock plattform för:
- EI
- TAK

Innehåller även en grund för att skapa en mock plattform för varje plugin

## Bygga och köra AGP i lokal utvecklingsmiljö
För att bygga hela projektet och köra alla tester (i rotkatalogen):
```
mvn clean install
```
För att köra applikationen måste först teststub-komponenten startas:
```
cd agp-teststub
mvn spring-boot:run
```
Därefter kan själva applikationen startas (i en annan terminal):
```
cd agp-application
mvn spring-boot:run
```
Ovanstående kommandon kommer dock starta AGP utan några aggregerande tjänster (plugins).
För att starta AGP med aggregerande tjänster behöver man köra den byggda jar-filen enligt följande exempel:
```
cd agp-application/target
java -Dloader.path=/tmp/services/ -jar agp-application-4.1.0-SNAPSHOT-exec.jar
```
Parametern loader.path sätts alltså till en katalog där man lagt de aggregerande tjänster som skall startas.
Notera att det är artefakterna som slutar på _-all.jar_ som skall användas, t.ex. GetAggregatedRequestActivities-v2-main-2.0.1-all.jar

 ## Helm
Källkoden innehåller även Helm chart som kan användas som grund för konfiguration i Kubernetes.

 ## Dokumentation referenser
 - [Konfigurering]
 
 [//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)
 
 
   [Konfigurering]: <doc/config/config.md>
