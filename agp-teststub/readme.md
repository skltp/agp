# agp-teststub
agp-teststub är ett basprojekt som kan agera producent för alla typer av tjänster genom att man tar in detta projet som ett maven dependency.
Projektet är också producent för:
* EI - findContent
* TAK - behörigheter

**Hur används denna**

Kan användas i en teststub för en specifik tjänst eller fristående som producent för findContent och SokVagvalInfo.
För att använda denna som producent måste man skapa en Spring konfigurering utifrån klassen TestProducerConfiguration samt skapa en specifik TestData generator för tjänsten.
Se agp-test-service/GetLaboratoryOrderOutcome-teststub för ett exempel.
 
  
**TAK test data**

Addressen för producent tjänsten konfigureras i parameter "teststub.sokVagValInfoAddress"

Följande behörigheter genereras:

|Logisk Address|Target Namespace| Sender Id |
|:----------:|:-------------:| :-----:|
| HSA-ID-1  | * |sample-sender-id|
| HSA-ID-2  | * |sample-sender-id|
| HSA-ID-3  | * |sample-sender-id|
| HSA-ID-4  | * |sample-sender-id|
| HSA-ID-5  | * |sample-sender-id|
| HSA-ID-6  | * |sample-sender-id|
| HSA-ID-1  | * |sample-original-consumer-hsaid|
| HSA-ID-1  | * |sample-original-consumer-hsaid|
| HSA-ID-77 | * |sample-original-consumer-hsaid|
| HSA-ID-11 | * |sample-original-consumer-hsaid|
| HSA-ID-12 | * |sample-original-consumer-hsaid|
| HSA-ID-31 | * |sample-original-consumer-hsaid|
| HSA-ID-32 | * |sample-original-consumer-hsaid|
| HSA-ID-FEL | random num | TK_HSA-ID-FEL |
| HSA-ID-FEL | random num | TK_HSA-ID-FEL |



**EI - findContent**

Addressen för producent tjänsten konfigureras i parameter "teststub.findContentAddress"

Följande engagemang genereras:

|Logisk Address|Patient| businessObjectId | Test fall |
|:----------:|:-------------:| :-----:|:-----: |
| HSA-ID-4  | 121212121212 | 1002 | TC1 |
| HSA-ID-5  | 121212121212 | 1003 | TC1 |
| HSA-ID-6  | 121212121212 | 1004 | TC1 |
| HSA-ID-1  | 194911172296 | 1001 | TC3 |
| HSA-ID-2  | 194911172296 | 1001 | TC3 |
| HSA-ID-1  | 198611062384 | 1002 | TC4 |
| HSA-ID-2  | 198611062384 | 1003 | TC4 |
| HSA-ID-2  | 198611062384 | 1004 | TC4 |
| HSA-ID-3  | 198611062384 | 1004 | TC4 |
| HSA-ID-1  | 192011189228 | 5001 | TC5 |
| HSA-ID-7  | 194804032094 | 6001 | TC6 |
| HSA-ID-7  | 194808069887 | 7001 | TC7 |

**TC1** Hämta data från bakomliggande producenter (3 engagemang) 

**TC2**  Testperson saknar engagemang 

**TC3** Inget engagemang från ena system, engagemang i andra system 

**TC4** Inget svar från ett system, engagemang i flera system, timeout från ett system 

**TC5** Error från system (exception) 

**TC6** Anropsbehörighet saknas för aktuell logiskadress 

**TC7** Hämta data från endast en producent
 
