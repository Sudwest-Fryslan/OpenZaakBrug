### Zaaksgewijs Werken Standaarden

Zaaksgewijs werken is een methode waarbij alle informatie en documenten die bij een specifieke zaak horen, centraal beheerd worden. Dit concept ontstond in de jaren 90 en is verder ontwikkeld met de introductie van digitale systemen. Zaaksgewijs werken stelt gemeenten in staat om klantgericht en transparant te werken, waardoor processen efficiënter worden en de dienstverlening aan burgers en bedrijven verbetert.

#### Voordelen van Zaaksgewijs Werken
- **Centrale Registratie**: Alle gegevens en documenten van een zaak worden op één plek beheerd, wat zorgt voor overzicht en efficiëntie.
- **Klantgericht Werken**: Gemeenten kunnen burgers en bedrijven beter informeren over de status van hun zaak.
- **Transparantie**: Het biedt inzicht in de voortgang van zaken, wat de transparantie verhoogt.
- **Efficiëntie**: Door centrale registratie en betere toegang tot informatie kunnen processen sneller worden afgehandeld.
- **Goede Dossieropbouw**: Het draagt bij aan een gestructureerde en complete opbouw van dossiers.
- **Vernietiging**: Documenten kunnen conform wettelijke voorschriften op de juiste manier worden vernietigd.

### Standaarden en Hun Synoniemen
- **STUF-ZKN (Standaard UitwisselingsFormaat - Zaken)**
   - **Synoniemen**: STUF-Zaken
   - **Versies**: Diverse versies sinds 2004, integratie met ZDS
   - **Status**: Ondersteund door VNG
   - **Beschrijving**: Standaard voor de uitwisseling van gegevens tussen verschillende systemen binnen de publieke sector.

- **ZDS (Zaak- en Documentservices)**
   - **Synoniemen**: ZS-DMS, ZSDMS
   - **Versies**: 1.1 (sinds 2016), 1.2 (sinds 2017)
   - **Status**: Ondersteund door VNG
   - **Beschrijving**: Standaard voor het beheren van zaken en documenten binnen gemeentelijke systemen.

- **ZGW API's (Zaakgericht Werken API's)**
   - **Synoniemen**: Zaakgericht werken API's
   - **Versies**: 1.0 en hoger (sinds 2020)
   - **Status**: Opvolger van ZDS
   - **Beschrijving**: Moderne, RESTful API's voor zaakgericht werken, gebaseerd op nieuwe standaarden en verbeterde integratie en schaalbaarheid.

### Ontwikkelingen
De Zaakgericht Werken API's (ZGW API's) zijn de opvolger van ZDS en bieden moderne, RESTful API's voor zaakgericht werken. Deze API's zijn gebaseerd op nieuwe standaarden en bieden verbeterde mogelijkheden voor integratie en schaalbaarheid binnen gemeentelijke systemen.

### Over ZDS
**ZDS (Zaak- en Documentservices)** biedt een gestandaardiseerde manier om zaken en documenten binnen gemeentelijke systemen te beheren.

#### Versies en Functies
- **ZDS 1.1**:
  - **Geef Zaakstatus**: Status van een zaak opvragen.
  - **Geef Zaakdetails**: Details van een zaak opvragen.
  - **Actualiseer Zaakstatus**: Status van een zaak bijwerken.
  - **Creëer Zaak**: Nieuwe zaak aanmaken.
  - **Update Zaak**: Zaakgegevens bijwerken.
  - **Genereer Zaakidentificatie**: Unieke identificatie voor een zaak genereren.
  - **Geef lijst Zaakdocumenten**: Lijst van documenten voor een zaak opvragen.
  - **Geef Zaakdocument lezen**: Specifiek document van een zaak lezen.
  - **Voeg Zaakdocument toe**: Document aan een zaak toevoegen.
  - **Maak Zaakdocument**: Nieuw document voor een zaak aanmaken.
  - **Update Zaakdocument**: Documentgegevens bijwerken.
  - **Genereer Documentidentificatie**: Unieke identificatie voor een document genereren.
  - **Cancel CheckOut**: Document ontgrendelen.

- **ZDS 1.2**:
  - Alle functies van ZDS 1.1.
  - **Koppel Zaakdocument aan Zaak**: Document aan een zaak koppelen.
  - **Ontkoppel Zaakdocument**: Document van een zaak ontkoppelen.

### Componenten van een Zaaksysteem
Een Zaaksysteem (ZS) bestaat uit verschillende componenten die samenwerken om zaken en documenten efficiënt te beheren. De belangrijkste componenten zijn:

- **Zaakservices Component (ZSC)**: Ondersteunt specifieke diensten en functionaliteiten gerelateerd aan zaakbeheer, zoals het verwerken van zaakstatussen en details.
- **Document Services Component (DSC)**: Beheert documentgerelateerde diensten, inclusief het opslaan, ophalen, bijwerken en verwijderen van documenten.

Een ZS integreert zowel de ZSC als de DSC om een volledige set aan zaak- en documentbeheerdiensten te bieden. 
Vakapplicaties communiceren met het ZS via gestandaardiseerde interfaces zoals STUF-ZKN en ZDS om gegevens uit te wisselen en processen te stroomlijnen.

### Extensies binnen ZDS
Extensies binnen ZDS bieden aanvullende functionaliteiten bovenop de basisstandaard. Ze zijn optioneel en kunnen naar behoefte worden geïmplementeerd.

#### Beschikbare Extensies
- **DSC (Document Services Component) verwijdert document (via STUF-ZKN en CMIS)**
   - **Doel**: Synchroniseren van documentverwijdering tussen DSC (Document Services Component) en ZS (Zaaksysteem).
- **Document ontkoppelen van zaak (via STUF-ZKN en CMIS)**
   - **Doel**: Documenten ontkoppelen van zaken en deze wijziging doorgeven aan betrokken systemen.
- **ZS (Zaaksysteem) informeert ZSC (Zaakservices Component)/DSC (Document Services Component) over mutatie met betrekking tot zaak of document**
   - **Doel**: Informeren van ZSC (Zaakservices Component) en DSC (Document Services Component) over wijzigingen in zaken of documenten met behulp van STUF-ZKN berichten.

### Disclaimer
Deze informatie is samengesteld op basis van gegevens die beschikbaar zijn op verschillende bronnen:
- [VNG Realisatie](https://vngrealisatie.nl)
- [GEMMA Softwarecatalogus](https://www.softwarecatalogus.nl)
- [GEMMA Online](https://www.gemmaonline.nl)

Voor de meest actuele en gedetailleerde informatie, raadpleeg de officiële documentatie van de genoemde bronnen.
