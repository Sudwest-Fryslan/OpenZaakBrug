# [Open Zaakbrug (zds-to-zgw)](https://sudwest-fryslan.github.io/OpenZaakBrug/) #

### Introductie
De ZGW-standaard is de opvolger van de ZDS-standaard voor zaakgewijs werken. Wij investeren niet meer in de oude ZDS-koppelingen en schakelen over op de ZGW-standaard. Open Zaakbrug maakt het mogelijk voor bestaande ZDS-applicaties om te communiceren met een ZGW-zaaksysteem, wat de overgang naar [OpenZaak](https://openzaak.org/) en de Common Ground-visie vergemakkelijkt.

### Status en Nieuws
- **2019:** Gestart door Súdwest-Fryslân, Utrecht en Haarlem, met een innovatiesubsidie van de [VNG](https://vng.nl).
- **Midden 2021:** Súdwest-Fryslân in productie met OpenZaak en Open Zaakbrug. [Meer informatie](https://commonground.nl/blog/view/6a946c44-851a-4a2b-bfaf-8368d886aff7/sudwest-fryslan-live-met-openzaak-en-open-zaakbrug).
- **2023:** Haarlem in productie met Open Zaakbrug.
- **Begin 2024:** Den Haag in productie met Zaakbrug. [Meer informatie](https://www.centric.eu/nl/wat-we-doen/centric-leefomgeving-en-openzaak-naadloos-van-zds-naar-zgw/).
- **2024:** Open Zaakbrug behaalt Goud-status in het Common Ground portfolio. [Meer informatie](https://app.powerbi.com/view?r=eyJrIjoiOWU4MjlmYTktNjE2MS00OGRhLTgwMjYtZWZhNTFhZmRhZjI2IiwidCI6IjZlZjAyOWFiLTNmZDctNGQ5OC05YjBlLWQxZjVmZWRlYTZkMS).
- **2024:** Verbeterde snelheid door de expand-functionaliteit en experimentele functies van OpenZaak. [Meer informatie](https://github.com/VNG-Realisatie/gemma-zaken/issues/2443).

### Over Open Zaakbrug
De [ZGW API-standaarden](https://www.vngrealisatie.nl/producten/api-standaarden-zaakgericht-werken) stellen gemeenten in staat zaakgericht werken volgens de Common Ground-visie in te richten. Dit bevordert het (ont)koppelen van processystemen en opslagcomponenten. Open Zaakbrug zet communicatie om van ZDS naar ZGW, waardoor bestaande applicaties niet direct hoeven te worden aangepast.

### Uitgangspunten
- Het doel is het aansluiten van de bestaande ZDS-applicaties.
- De oplossing hoeft niet aan de volledige ZDS-standaard te voldoen; minimale functionele ondersteuning is voldoende.
- De programmatuur moet makkelijk herbruikbaar zijn.
- De broncode moet overdraagbaar en aanpasbaar zijn door gemeenten.
- Het betreft een tijdelijke oplossing; leveranciers moeten over naar ZGW.
- Open Zaakbrug ondersteunt de transitie zodat je niet in één keer hoeft over te stappen van het ene zaaksysteem naar een ZGW-registratie.
- Het helpt bij het achterhalen van de inhoud van de berichten en het overhalen van reeds bestaande zaken uit het oude zaaksysteem naar de nieuwe ZGW-zaakregistratie.

### Technische Informatie
- Docker Containers : Beschikbaar op [Docker Hub](https://hub.docker.com/r/openzaakbrug/openzaakbrug/tags) voor eenvoudige installatie van Open Zaakbrug.
- Installatie voor Ontwikkelaars : Zie de installatie-instructies in de documentatie: [Installing Open Zaakbrug.md](https://sudwest-fryslan.github.io/OpenZaakBrug/docs/Installing%20Open%20Zaakbrug.md).
- Transitie Ondersteuning en Data Migratie : Ondersteunt de overgang van oude zaaksystemen naar ZGW-zaakregistratie. [Meer details](https://sudwest-fryslan.github.io/OpenZaakBrug/docs/Workings%20of%20Replication.md).
- Berichtenstroom Overzicht:  [Flow](https://sudwest-fryslan.github.io/OpenZaakBrug/docs/media/flow.png).
- Beschikbaar in de repository: [Open Zaakbrug Docs](https://sudwest-fryslan.github.io/OpenZaakBrug/docs/).

### Ondersteunde Applicaties
De functioneel beheerders van de verschillende backoffice applicaties hebben de vertaler getest en goedgekeurd. De bestaande zaken uit het oude zaaksysteem worden goed gerepliceerd naar het nieuwe systeem, mits de gegevenskwaliteit goed is.

| Applicatie                                      | Status         |
|-------------------------------------------------|----------------|
| Suites voor Sociaaldomein                       | live           |
| Gisvg (wabo/apv-vergunningen)                   | live           |
| Midofficevuller (verhuizing/identiteitskaart/rijbewijs) | live |
| Sleeptool (toevoegen documenten aan bestaande zaak) | live |
| Verint Kana                                     | in acceptatie  |
| Gidso regiesysteem                              | live           |
| Powerbrowser                                    | live           |
| Centric Leefomgeving                            | in acceptatie  |
| Signicat CityPermit                             | in acceptatie  |

### Webservice functies

| Webservice functie                              | Proxy      | Translate  | Replicate  |
|-------------------------------------------------|------------|------------|------------|
| ZDS 1.1 genereerZaakIdentificatie_Di02          | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 creeerZaak_Lk01                         | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 geefZaakdetails_Lv01                    | ondersteund| ondersteund| ondersteund|
| StufZkn 3.1 zakLv01                             | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 updateZaak_Lk01                         | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 actualiseerZaakstatus_Lk01              | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 geefLijstZaakdocumenten_Lv01            | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 genereerDocumentIdentificatie_Di02      | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 maakZaakdocument_Lk01                   | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 voegZaakdocumentToe_Lk01                | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 geefZaakdocumentLezen_Lv01              | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 geefZaakdocumentbewerken_Di02           | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 updateZaakdocument_Di02                 | ondersteund| ondersteund| ondersteund|
| ZDS 1.1 cancelCheckout_Di02                     | ondersteund| ondersteund| ondersteund|

### Zaakbrug
Zaakbrug is de beoogde opvolger van Open Zaakbrug en fungeert als een vertaler die bestaande applicaties, die de oude ZDS-standaard gebruiken, laat werken met een ZGW-zaaksysteem. Het is gebaseerd op een open-source integratie-framework. 
Zaakbrug biedt geen ondersteuning voor een gefaseerde implementatie zonder een 'big bang' aanpak (proxy, replicatie). 
[Meer informatie](https://www.zaakbrug.nl).

De naamswijziging is doorgevoerd om misverstanden te voorkomen. Het werd ten onrechte verondersteld dat de (Open) Zaakbrug exclusief met OpenZaak zou functioneren. Zowel Open Zaakbrug als Zaakbrug zijn compatibel met correct geïmplementeerde ZGW-API's.

### Verschillen tussen Open Zaakbrug en Zaakbrug

| Kenmerk                              | Open Zaakbrug                         | Zaakbrug                                      |
|--------------------------------------|---------------------------------------|-----------------------------------------------|
| Onderhoud en fixes                   | Onderhoud en bugfixes                 | Onderhoud en bugfixes                         |
| Nieuwe features                      | Door andere gemeenten beschikbaar     | Door andere gemeenten beschikbaar             |
| Gebruikte platform                   | Spring Boot javacode                  | FrankFramework configuratie                    |
| Zoeken en herstellen fouten          | Cognos rapportages                    | E2E Ladybug advanced                          |
| Nieuwe expand (snelheid)             | In acceptatie                         | Beschikbaar incl. etags caching               |
| Werken met losse URLs (dms)          | In acceptatie                         | In productie                                  |
| Mutaties op zaken                    | Stopt bij foutmelding                 | Probeert te herstellen                        |
| Overige functionaliteiten            | - Proxy: analyse van het bestaande berichten verkeer   |                                               |
|                                      | - Replicatie: migreren data tijdens gebruik, zonder bigbang in productie |                                               |
| Berichtenverkeer testen              | Testcases werken goed                 | Testcases werken goed. Testsuite E2E en unit testen |
| Voordeel                             | In productie met meer vakapplicaties  | Configuratie vertaling kan worden aangepast per zaaktype/generiek door beheerder zelf |
| Toekomst                             | - Draait stabiel                      | Wordt actief doorontwikkeld, veel flexibiliteit in features |
|                                      | - Bijhouden van ZGW-standaard aanpassingen, |                                               |
|                                      | - Bugs fixen bij bestaande of nieuwe applicaties |                                               |
| Integratie andere systemen           | Via PostgreSQL                        | Splunk, Elastic search, Secure logging, etc.  |



### Overige
- **PublicCode:** Kwaliteit en open-source processen. [PublicCode](https://publiccode.net).
- **HaalCentraal:** Verwijzen naar burgergegevens zonder opslag in zaaksystemen. [HaalCentraal](https://commonground.nl).
- **Haven:** Eenvoudige installatie met één klik. [Haven](https://haven.commonground.nl).

### Partijen
- Gemeente Súdwest-Fryslân, Eduard Witteveen (productowner)
- Gemeente Haarlem, David van Hussel
- Gemeente Den Haag, Geert-Jan Pes
- WeAreFrank!, Jaco de Groot

Voor vragen of meer informatie kunt u een email sturen naar [e.witteveen@sudwestfryslan.nl](mailto:e.witteveen@sudwestfryslan.nl) of een bericht achterlaten in onze [Slack](https://samenorganiseren.slack.com).
