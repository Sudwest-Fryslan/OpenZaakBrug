# [Open Zaakbrug (zds-to-zgw)](https://sudwest-fryslan.github.io/OpenZaakBrug/) #

De nieuwe standaard voor het zaakgewijs werken is de [zgw standaard](https://www.vngrealisatie.nl/producten/api-standaarden-zaakgericht-werken), dit is de opvolger van de Zaak- en Documentservices (ZDS) standaard.
Om de overstap te maken naar deze nieuwe standaard vinden wij het belangrijk om niet meer te investeren in de oude-zds-koppelingen, maar  zo snel mogelijk de nieuwe zgw-standaard te gebruiken. Om deze reden heeft het team Open Zaakbrug een oplossing om de bestaande applicaties, die de oude zds-standaard praten, toch met een zgw-zaaksysteem te laten praten.

Hiermee kan de gemeente de bestaande zaaksgewijswerken applicaties blijven gebruiken en toch overstappen op bijvoorbeeld [openzaak](https://openzaak.org/). Dit zodat alles blijft draaien en ondertussen de overstap kan worden gemaakt naar de nieuwe commonground wereld.

Door te beginnen met een zgw-zaaksysteem kunnen alle nieuwe domein-specifieke-applicaties via deze nieuwe standaard aansluiten en krijgen de leveranciers van de bestaande applicaties de tijd om de ZGW koppeling in te bouwen.

## Over OpenZaakBrug
De API-standaarden voor zaakgericht werken stellen gemeenten in staat om de informatievoorziening rondom zaakgericht werken in te richten volgens de informatiekundige visie Common Ground. Hiermee zetten ze een eerste stap in de richting van een modernisering van het ICT-landschap. Dit helpt bij het (ont)koppelen van processystemen en de opslagcomponenten waar documenten en overige informatie in zijn opgeslagen.

Wij willen niet meer investeren in de oude koppelingen van het zaaksgericht werken (ZDS standaard) en willen zo snel mogelijk de nieuwe Zaakgericht Werken-api&#39;s gebruiken. Daarom willen we zo snel mogelijk de nieuwe opslaginfrastructuur op basis van deze standaard aanbieden. Daarna kunnen alle nieuwe applicaties via deze nieuwe standaard aansluiten.

Omdat de bestaande applicaties niet de nieuwe standaard ondersteunen kunnen deze niet aangesloten worden op het nieuwe zaaksysteem. Door een component te ontwikkelen welke de communicatie omzet van de oude standaard (ZDS) naar de nieuwe (ZGW) hoeven de bestaande applicaties niet op hetzelfde moment te worden aangepast.

Het uiteindelijke doel is om alle applicaties op de nieuwe Zaakgericht Werken-api&#39;s te krijgen, met deze aanpak ontstaat er tijd om beheerst en onder regie dit uit te voeren.

Met dit vertaal component zorgen we ervoor dat de bestaande ZDS- applicaties kunnen aansluiten op de nieuwe zaak en document opslagcomponenten.

Hierbij hebben we de volgende uitgangspunten:

- Het doel is het aansluiten van de bestaande ZDS-applicaties
- De oplossing hoeft niet aan de volledige ZDS-standaard te voldoen (minimaal vereiste functionele ondersteuning)
- De programmatuur moet makkelijk herbruikbaar zijn
- De broncode moet overdraagbaar zijn en naar wens aanpasbaar zijn door gemeenten
- Het betreft een tijdelijke oplossing, de leveranciers moeten over naar ZGW

## Presentaties...

- Team Open Zaakbrug op [commonground.nl](https://commonground.nl/groups/view/c2df2f42-b3ea-405e-953f-fe808ab56ba0/team-openzaakbrug)
- 2021-02-05 [Fieldlab DOiT video](https://vimeo.com/512518040/2284537066)
- 2021-02-05 [Powerpoint Fieldlab DOiT](https://github.com/Sudwest-Fryslan/OpenZaakBrug/blob/master/docs/powerpoint/20210205-FieldLab2021-Eduard-Open%20Zaakbrug,%20commonground%20in%20de%20praktijk.pptx?raw=true)

## Continue implementatie / continue  levering

Om de kwaliteit te borgen en reproduceerbaar de programmatuur uit te leveren wordt er gebruik gemaakt van ci/cd

Aanpassen code en geautomatiseerd testen:

- Eén of meerdere issue's wordt aangemaakt waarin beschreven staat welke werkzaamheden worden uitgevoerd
- Voor deze werkzaamheden wordt een branch aangemaakt
- De werkzaamheden worden via commit's op de betreffende branch gedaan
- Na het doen van een commit worden er [testen](https://travis-ci.com/github/Sudwest-Fryslan/OpenZaakBrug/builds/) uitgevoerd op github om te kijken of alles werkt. 
  - Hierin wordt controle gedaan door voorbeeldberichten te verwerken, te controleren of Open Zaakbrug zich hetzelfde blijft gedragen.
  - Te kijken of er een docker image kan worden gemaakt en worden opgestart

Goedkeuren aanpassingen en klaarzetten:

- Een andere partij, dan degene die het pullrequest heeft gedaan, reviewed de code en controleert of de testen goed zijn doorlopen
- Wanneer dit akkoord is, wordt deze geaccepteerd en gemerged in de master branch
- De eerder aangemaakte branch wordt verwijderd
- Er wordt automatisch een nieuwe docker image geplaatst op [dockerhub](https://hub.docker.com/r/openzaakbrug/openzaakbrug/tags?)

Updaten in de eigen omgeving:

- Via docker pull openzaakbrug/openzaakbrug kan een versie van openzaakbrug worden geinstalleerd
- Daarbij moet éénmalig de configuratie worden aangemaakt, daarna kan met soort gelijke commando's snel en reproduceerbaar de omgeving worden geupdate.

Vinden van fouten/verbeteringen:

- Fouten en verbeteringen kunnen worden gemeld via een [issue](https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues) op github
- Voor verder communicatie gebruiken we ook [slack](https://samenorganiseren.slack.com/archives/C01FDA71Y4V)

## Common Ground
Met Common Ground willen gemeenten de informatievoorziening eenvoudiger, sneller en slimmer inrichten. In de kern gaat het bij Common Ground om een hervorming van de gemeentelijke informatievoorziening, door op een andere manier om te gaan met gegevens. Zo koppelen we data los van werkprocessen en applicaties
Wij van Open Zaakbrug ondersteunen de visie van common ground en ontwikkelen het product in mijn met deze visie.

Bekijk onze [checklist](commonground/checklist.md) om te zien hoe common ground wij zijn!


## Technische informatie

- Informatie over de installatie kijk in : [Installing Open Zaakbrug.md](docs/Installing%20Open%20Zaakbrug.md)  
- Informatie over het aanroepen van de service en het vertalen en repliceren kijk : [Workings of Replication.md](docs/Workings%20of%20Replication.md)
- Overzichtsplaat van de berichtenstroom door de applicatie in [Flow](docs/media/flow.png)

## Huidige ondersteuning applicaties

Op dit moment wordt er druk gewerkt om de Open Zaakbrug live te krijgen. Hiervoor testen de functioneelbeheerders van de verschillende backoffice applicaties het gedrag van de vertaler.  Hierbij wordt getest of de applicatie zich gedraagt zoals het voorheen werkte met het legacy (ZDS) zaaksysteem voor de nieuwe processen. Daarnaast wordt getest of de bestaande zaken uit het oude zaaksysteem goed gerepliceerd worden naar het nieuwe systeem, zodat er geen conversies hoeven te worden gedaan. (het is wel zaak dat de gegevenskwaliteit hiervoor goed in orde is!) 

| Applicatie                                                   | Proxy functionaliteit     | Translate functionaliteit | Replicatie functionaliteit | Status        |
| ------------------------------------------------------------ | ------------------------- | ------------------------- | -------------------------- | ------------- |
| Suites voor Sociaaldomein                                    | Getest door fb en akkoord | Getest door fb en akkoord | Getest door fb en akkoord  | in acceptatie |
| Gisvg (wabo/apv-vergunningen)                                | Getest door fb en akkoord |  **live**                 | **live**                   | **live**      |
| Midofficevuller met BZ (verhuizing/identieitskaart/rijbewijs) | Getest door fb en akkoord | **live**                 | nvt.                       | **live**      |
| [Sleeptool](https://github.com/Sudwest-Fryslan/DeSleepTool) (toevoegen documenten aan bestaande zaak) | Getest door fb en akkoord | **live** | nvt. | -           |
| Verint Kana                                                  | Getest door fb en akkoord | Getest door fb en akkoord | Getest door fb en akkoord  | in acceptatie |
| Gidso regiesysteem                                           | -                         | -                         | -                          | -             |
| Powerbrowser                                                 | -                         | -                         | -                          | -             |

De bovenstaande testen zijn uitgevoerd door de functioneelbeheerders van de betreffende taakspecifieke applicaties, dus door de personen die moeten zorgen voor de functionaliteiten en continuiteit van de betreffende applicaties.

- **Proxy** : Verstuurd de berichten door naar het oude, legacy zaaksysteem, Open Zaakbrug kan gebruikt worden om de berichten te inspecteren.
- **Translate** : Vertaald de ZDS berichten naar ZGW en geeft weer netjes een antwoord in ZDS. 
- **Replicate**:  Combinatie van de bovenste 2, de berichten worden vertaald EN doorgestuurd naar het nieuwe systeem. Wanneer er verwezen wordt naar een zaak of zaakdocument die niet bestaat in openzaak, dan wordt deze informatie on-the-fly uit het oude systeem gecopieerd naar openzaak en de betreffende applicatie uitgevoerd.

Omdat het een standaard is die wordt vertaald, zullen vervolg applicaties steeds "makkelijker" te koppelen zijn, omdat daarmee de functie-set beter is getest en daarmee volwassener zal worden. Op dit moment blijkt dat het implementeren van de volgende webservices voldoende is om goede ondersteuning te leveren:

| Webservice functie                         | Translate   | Replicate            |
| ------------------------------------------ | ----------- | -------------------- |
| ZDS 1.1 genereerZaakIdentificatie_Di02     | ondersteund | ondersteund          |
| ZDS 1.1 creeerZaak_Lk01                    | ondersteund | ondersteund          |
| ZDS 1.1 geefZaakdetails_Lv01               | ondersteund | ondersteund          |
| StufZkn 3.1 zakLv01                        | ondersteund | ondersteund          |
| ZDS 1.1 updateZaak_Lk01                    | ondersteund | ondersteund          |
| ZDS 1.1 actualiseerZaakstatus_Lk01         | ondersteund | ondersteund          |
| ZDS 1.1 geefLijstZaakdocumenten_Lv01       | ondersteund | ondersteund          |
| ZDS 1.1 genereerDocumentIdentificatie_Di02 | ondersteund | ondersteund          |
| ZDS 1.1 maakZaakdocument_Lk01              | ondersteund | ondersteund          |
| ZDS 1.1 voegZaakdocumentToe_Lk01           | ondersteund | ondersteund          |
| ZDS 1.1 geefZaakdocumentLezen_Lv01         | ondersteund | ondersteund          |
| ZDS 1.1 geefZaakdocumentbewerken_Di02      | in test     | niet geïmplementeerd |
| ZDS 1.1 updateZaakdocument_Di02            | in test     | niet geïmplementeerd |
| ZDS 1.1 cancelCheckout_Di02                | in test     | niet geïmplementeerd |

## Roadmap

- [PublicCode](https://publiccode.net/) : duidelijke checklist van de kwaliteit, de processen van het product en hoe opensource we zijn.
- [HaalCentraal](https://commonground.nl/groups/view/8b2f6ab7-09c6-4164-9eb0-c98868c1cc8d/open-personen) (oid) ondersteuning voor het niet meer opslaan van burgergegevens in het zaaksysteem, maar het verwijzen
- [Haven](https://haven.commonground.nl/) Het installeren van de applicatie met een klik op een knop

## Partijen

- [Gemeente Súdwest-Fryslân](https://sudwestfryslan.nl/), Jacco Hovinga,  Santoshi Bouma en Eduard Witteveen (productowner)
- [Gemeente Haarlem](https://www.haarlem.nl/),  David van Hussel
- [Gemeente Utrecht](https://www.utrecht.nl/), Lazo Bozarov
- [WeAreFrank!](https://blog.wearefrank.nl/borging-openzaakbrug-integratie-vng), Jaco de Groot 

Om ons te bereiken kunt u een email versturen naar e.witteveen@sudwestfryslan.nl of stel gewoon een vraag in onze [slack](https://samenorganiseren.slack.com/archives/C01FDA71Y4V) 
