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

 ## Dokumentation referenser
 - [Konfigurering]
 
 [//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)
 
 
   [Konfigurering]: <doc/config/config.md>

