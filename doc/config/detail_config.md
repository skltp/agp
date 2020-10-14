# Detaljerad konfiguration

I det här dokumentet finns detaljerad information om hur man konfigurerar AGP Camel.
Längre ner finns också exempelfiler som visar hur AGP Camel kan konfigureras.
Huvudsidan för konfigurering finns här: [AGP Camel konfigurering]
 
### Hawtio

##### Ställa in autentisering för Hawtio
I application-custom.properties kan man sätta propertyn:
`hawtio.authentication.enabled=true` 
till true eller false. Default är den false. Men det rekommenderas att ha autentisering på eftersom man kan ändra funktionalitet i AGP via Hawtio. Att sätta på autentisering innebär att man behöver konfigurera en user och password enligt nedan. 
##### Konfigurera ny user och password 
För att ändra/lägga till användare/lösenord för Hawtio: Generera en md5-hash av det password ni valt, till exempel med (på Linux): `printf  '%s' "<password>" | md5sum`.
     Skapa en ny login-fil, t.ex. hawtio_users.properties. Den ska innehålla namn på user och hashat pw enligt:
     `<user>: MD5:<password hash>, user, admin`     
     Placera filen lämpligen tillsammans med övriga konfigurations-filer.
     I `application-custom.properties`: Lägg till sökväg till filen enligt:
     `hawtio.external.loginfile=<path>/hawtio_users.properties`
     Installera om VP och starta. Kontrollera vilken port AGP är konfigurerad att starta på. Det ska nu gå att surfa till http://\<server\>:\<port\>/actuator/hawtio/ och logga in med vald user och password. Kontrollera även log-filen för agp-camel för att se om installationen gått bra.
     
     
### Konfigurera loggning
Se anvisningar på sidan [Loggning konfigurering]

### Exempel på application-custom.properties
```
server.port=8881
agp.host=localhost

# Hawtio
management.endpoints.web.exposure.include=hawtio,jolokia
hawtio.authentication.enabled=false
#hawtio.external.loginfile=C:/Temp/hawtio_users.properties

# Allow to obtain basic information about camel routes (read only mode)
endpoints.camelroutes.enabled=true
endpoints.camelroutes.read-only=true

# Total timeout for aggregated calls to producers
aggregate.timeout=28000

# Outpoing parameters for calling services
vp.instanceId=dev_env
vp.defaultReceiveTimeout=27000
vp.defaultConnectTimeout=2000
vp.defaultServiceURL=http://localhost:8083/vp

# Outgoing parameters for calling EI-FindContent
ei.logicalAddress=556500000
ei.senderId=SENDER1
ei.findContentUrl=http://localhost:8082/findcontent
ei.connectTimeout=2000
ei.receiveTimeout=20000

# Other services URLs
reset.cache.url=http://${agp.host}:8091/resetcache
agp.status.url=http://${agp.host}:1080/status

#Takcache configuration
takcache.endpoint.address=http://localhost:8085/tak/teststub/SokVagvalsInfo/v2

# Max size of payload loggings
log.max.payload.size=49152
```

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

   [Loggning konfigurering]: <logging_configuration.md>
   [AGP Camel konfigurering]: <config.md>