# Logging konfigurering

Denna sida handlar om loggning. För allmän information om konfiguration av AGP Camel, se [AGP Camel konfigurering].
### Allmänt
Som logg-ramverk används Log4j2, se [log4j2s dokumentation] för mer information.
Ett exempel på grundkonfigurering som loggar till konsolen finns längre ned på denna sida.

### Extern konfiguration
För att använda en extern log4j2.xml konfigurationsfil kan man starta applikationen med parametern `-Dlog4j.configurationFile`, 
till exempel:
 `java -jar -Xms256m -Xmx1024m -Dlog4j.configurationFile=file:///opt/agp/config/log4j2.xml agp-application.jar"`
### Ändring av loggnivåer i runtime
Här beskivs två sätt att ändra loggnivåer i runtime.

 1. Om en extern konfiguration används räcker det att ändra i konfigureringsfilen förutsatt att den är grundkonfigurerad att upptäcka förändringar i runtime. Kontrollera att parametern `monitorInterval="30"` är satt.
 2. Ändra loggnivåer med Hawtio (eller på annat sätt via jmx)
 ### Rekommenderade loggers
Vissa loggers kan vara av extra intresse för att följa AGPs uppstart och flöden. Se beskrivning nedan:

*log4j2.xml*
```
<Configuration status="WARN" monitorInterval="30">

  <Properties>
    <Property name="LOG_PATTERN">
      %d %-5p [%t] %-30c - %X{corr.id} %m%n
    </Property>
  </Properties>

  <Appenders>
    <Console name="Console" target="SYSTEM_OUT">
      <PatternLayout pattern="${LOG_PATTERN}"/>
    </Console>
  </Appenders>

  <Loggers>
    <AsyncLogger name="se.skltp.aggregatingservices" level="WARN"/>

    <!--Level INFO will log the init/reset och TAK cache-->
    <AsyncLogger name="se.skltp.takcache" level="INFO"/>
    <AsyncLogger name="se.skl.tp.vp.vagval.ResetTakCacheProcessor" level="INFO"/>


    <!--Level INFO will log startup for spring boot application-->
    <AsyncLogger name="se.skltp.aggregatingservices.AgpApplication" level="INFO"/>

    <!--Apache camel-->
    <AsyncLogger name="org.apache.camel" level="INFO"/>

    <!-- Message logging -->
    <AsyncLogger name="se.skltp.aggregatingservices.logging" level="INFO"/>
    <AsyncLogger name="se.skltp.aggregatingservices.logging.FindContentResponderInterface" level="DEBUG"/>

    <!-- Message logging per service -->
    <Logger name="se.skltp.aggregatingservices.logging.GetLaboratoryOrderOutcomeResponderInterface level="INFO"/>


    <AsyncRoot level="WARN">
      <!--<AppenderRef ref="RollingRandomAccessFile"/>-->
      <AppenderRef ref="Console"/>
    </AsyncRoot>

  </Loggers>
</Configuration>
```
### Meddelande-loggning
Meddelanden loggas av en Cxf interceptor med följande grund logger:
 *se.skltp.aggregatingservices.logging*

   - INFO nivå - innebär att meddelanden loggas utan payload.
   - DEBUG nivå - innebär att meddelanden loggas med payload. 

 Det går också att konfigurera en egen logger per tjänstekontrakt enligt:
 *se.skltp.aggregatingservices.logging.{servicecontractresponder}*
 
 Exempel:
 - *se.skltp.aggregatingservices.logging.GetLaboratoryOrderOutcomeResponderInterface*
 - *se.skltp.aggregatingservices.logging.FindContentResponderInterface"*


[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)


   [log4j2s dokumentation]: <https://logging.apache.org/log4j/2.x/>
   [AGP Camel konfigurering]: <config.md>
