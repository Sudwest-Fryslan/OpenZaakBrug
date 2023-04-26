**Replicatie**

Als je als gemeente wil over schakelen van huidige naar nieuwe wereld en wil je je risico&#39;s minimaliseren.

Dit door het volgende proces te doorlopen:

1. Plaatst de OpenZaakBrug tussen het bestaande zaaksysteem en de TSA's (taak specifieke applicaties), waarbij de proxy-urls moeten worden gebruikt.

   Waarom: Hiermee wordt getest of het berichtenverkeer blijft lopen, de OpenZaakBrug fungeert als een doorgeef luik (een reverse proxy).

2. Converteer de bestaande zaaktype-definities en laad deze in de ZTC van OpenZaak. Dit kan met bijvoorbeeld de INavigator-import-tool of met de hand. 

   Waarom: Met deze inrichting komen eventuele problemen met de bestaande zaaktype definities aan het licht, of deze inderdaad passen in de nieuwe structuur

3. In de volgende stap laten we alle communicatie tussen het bestaande zaaksysteem en de TSA's vervangen door OpenZaak, hierbij doen we nog een afschrift naar het legacy zaaksysteem . Hierdoor wordt er dus gewerkt op openzaak, waarbij er nog gerepliceerd wordt naar het legacy zaaksysteem Ook doet het systeem nog een bevraging op het "oude"-zaaksysteem wanneer een zaak of document niet wordt gevonden in OpenZaak. Op deze manier is het niet nodig om alle gegevens gemigreerd te hebben en toch al op OpenZaak te draaien. Voor de zekerheid wordt het bericht van de TSA ook nog doorgestuurd naar het "oude"-zaaksysteem, zodat deze nog steeds volledig meeloopt. Ook worden de id's nog door het "oude"-zaaksysteem aangemaakt. Hierbij moet de replicatie urls worden gebruikt
   Waarom: Hiermee wordt aangetoond dat de translatie naar OpenZaak werkt, zonder dat het oude zaaksysteem niet meer synchroon loopt.

4. Deze stap is de migratie van alle gegevens uit het oude-zaaksysteem naar OpenZaak, dit kan door alle zaken en documenten te bevragen op de OpenZaakBrug of via scripts.

5. Wanneer alle zaken en documenten in het zaaksysteem staan, dan moeten in de database de ZaakIdentificatieHuidige en DocumentIdentificatieHuidige aangepast worden naar de laatst uitgegevens nummers (en de betreffende prefix), waarna translate-urls gebruikt kunnen worden. De OpenZaakBrug draait daamee volledig op OpenZaak het het "oude"-zaaksysteem kan worden uitgezet.)

Op dit moment wordt AL het berichtenverkeer in de database gelogd met de eventuele fouten voor nadere inspectie, zodra de koppeling robuust werkt kan dit worden aangepast.

## Replicatie standen: ##

- Proxy : Stuur door naar het legacy zaaksysteem en logt berichtenverkeer en fouten naar de tabellen
- Replicate : Converteer de berichten naar OpenZaak logt berichtenverkeer en fouten naar de tabellen, daarnaast nog een afschrift naar het legacy zaaksysteem voor de zekerheid
- Translate : Converteer de berichten naar OpenZaak logt berichtenverkeer en fouten naar de tabellen

## urls ##

| modus| url | soapaction |
|------|-----|------------|
| proxy | http://localhost:8080/proxy/generic/zds/VrijBericht | http://www.egem.nl/StUF/sector/zkn/0310/genereerDocumentIdentificatie_Di02 |
| proxy | http://localhost:8080/proxy/generic/zds/VrijBericht | http://www.egem.nl/StUF/sector/zkn/0310/genereerZaakIdentificatie_Di02 |
| proxy | http://localhost:8080/proxy/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/actualiseerZaakstatus_Lk01 |
| proxy | http://localhost:8080/proxy/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/creeerZaak_Lk01 |
| proxy | http://localhost:8080/proxy/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/updateZaak_Lk01 |
| proxy | http://localhost:8080/proxy/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/voegZaakdocumentToe_Lk01 |
| proxy | http://localhost:8080/proxy/generic/zds/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/geefLijstZaakdocumenten_Lv01 |
| proxy | http://localhost:8080/proxy/generic/zds/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/geefZaakdetails_Lv01 |
| proxy | http://localhost:8080/proxy/generic/zds/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/geefZaakdocumentLezen_Lv01 |
| proxy | http://localhost:8080/proxy/generic/stufzkn/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/zakLv01 |
| replicate | http://localhost:8080/replicate/generic/zds/VrijBericht | http://www.egem.nl/StUF/sector/zkn/0310/genereerDocumentIdentificatie_Di02 |
| replicate | http://localhost:8080/replicate/generic/zds/VrijBericht | http://www.egem.nl/StUF/sector/zkn/0310/genereerZaakIdentificatie_Di02 |
| replicate | http://localhost:8080/replicate/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/actualiseerZaakstatus_Lk01 |
| replicate | http://localhost:8080/replicate/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/creeerZaak_Lk01 |
| replicate | http://localhost:8080/replicate/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/updateZaak_Lk01 |
| replicate | http://localhost:8080/replicate/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/voegZaakdocumentToe_Lk01 |
| replicate | http://localhost:8080/replicate/generic/zds/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/geefLijstZaakdocumenten_Lv01 |
| replicate | http://localhost:8080/replicate/generic/zds/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/geefZaakdetails_Lv01 |
| replicate | http://localhost:8080/replicate/generic/zds/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/geefZaakdocumentLezen_Lv01 |
| replicate | http://localhost:8080/replicate/generic/stufzkn/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/zakLv01 |
| translate | http://localhost:8080/translate/generic/zds/VrijBericht | http://www.egem.nl/StUF/sector/zkn/0310/genereerDocumentIdentificatie_Di02 |
| translate | http://localhost:8080/translate/generic/zds/VrijBericht | http://www.egem.nl/StUF/sector/zkn/0310/genereerZaakIdentificatie_Di02 |
| translate | http://localhost:8080/translate/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/actualiseerZaakstatus_Lk01 |
| translate | http://localhost:8080/translate/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/creeerZaak_Lk01 |
| translate | http://localhost:8080/translate/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/updateZaak_Lk01 |
| translate | http://localhost:8080/translate/generic/zds/OntvangAsynchroon | http://www.egem.nl/StUF/sector/zkn/0310/voegZaakdocumentToe_Lk01 |
| translate | http://localhost:8080/translate/generic/zds/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/geefLijstZaakdocumenten_Lv01 |
| translate | http://localhost:8080/translate/generic/zds/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/geefZaakdetails_Lv01 |
| translate | http://localhost:8080/translate/generic/zds/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/geefZaakdocumentLezen_Lv01 |
| translate | http://localhost:8080/translate/generic/stufzkn/BeantwoordVraag | http://www.egem.nl/StUF/sector/zkn/0310/zakLv01 |
