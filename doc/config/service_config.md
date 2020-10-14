# Konfiguration av aggregerande tjänst

Varje aggregerande tjänst har en egen implementation av AgpServiceConfiguration som en spring-boot @ConfigurationProperties.
Klassen innehåller default konfigureringen för tjänsten som kan överlagras enligt spring-boot. exempelvis i application.properties.
Varje tjänst har unikt @ConfigurationProperties prefix för sin konfigurering, exempelvis "getaggregatedlaboratoryorderoutcome.v4" 

Parameterar:

|Nyckel|Exempelvärde|Beskrivning|
|----|------------------|---------|
| \{prefix}.inboundServiceURL | http://0.0.0.0:9024/GetAggregatedLaboratoryOrderOutcome/service/v4  | URL som tjänsten lyssnar på. **Denna behöver sannolikt överlagras** |
| \{prefix}.outboundServiceWsdl | http://$\{vp}/vp/clinicalprocess/healthcond/actoutcome/GetLaboratoryOrderOutcome/4/rivtabp21  | URL mot tjänsten i VP. Default används konfigureringen vp.defaultServiceURL  |
| \{prefix}.receiveTimeout | 10000 | Receive timeout ms mot producenten. Default används konfigureringen vp.defaultReceiveTimeout  |
| \{prefix}.serviceName | GetLaboratoryOrderOutcome.V4  | Namn/identifierare av tjänsten tjänsten. Används exempelvis för att göra tjänsten sökbar i loggar |
| \{prefix}.targetNamespace | urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcome:4:rivtabp21 | Tjänstekontraktets target namespace  |
| \{prefix}.inboundServiceWsdl | /schemas/clinicalprocess-healthcond-actoutcome/interactions/ GetLaboratoryOrderOutcomeInteraction/GetLaboratoryOrderOutcomeInteraction_4.0_RIVTABP21.wsdl | Sökväg till wsdl  |
| \{prefix}.inboundServiceClass | riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcome.v4.rivtabp21.GetLaboratoryOrderOutcomeResponderInterface  | Tjänstens genererade service klass, inkommande anrop |
| \{prefix}.inboundPortName | {urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcome :4:rivtabp21}GetLaboratoryOrderOutcomeResponderPort | Tjänstekontraktets "port name" |
| \{prefix}.outboundServiceWsdl |  | Sökväg till wsdl, utgående anrop  |
| \{prefix}.outboundServiceClass |  | Tjänstens genererade service klass, utgående anrop |
| \{prefix}.outboundPortName |  | Tjänstekontraktets "port name" |
| \{prefix}.takContract | urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcomeResponder:4 | Tjänstekontraktets namn i TAK  |
| \{prefix}.eiServiceDomain |riv:clinicalprocess:healthcond:actoutcome  | EI domain för tjänsten  |
| \{prefix}.eiCategorization | und-kkm-ure | EI categorization för tjänsten. Denna kan konfigureras som en enskild kategori eller som en kommaseparerad lista. Konfigureras den som en lista hämtas alla kategorier från EI och sedan filtreras svaret mot listan. |
| \{prefix}.serviceFactoryClass | se.skltp.aggregatingservices.riv.clinicalprocess.healthcond.actoutcome.GLOOAgpServiceFactoryImpl | Tjänstekontraktets target namespace  |
| \{prefix}.connectTimeout | -1 | Connect timeout ms mot producent. Default används konfigureringen vp.defaultConnectTimeout   |
| \{prefix}.enableSchemaValidation | false | Strikt schemavalidering. Default:false|

