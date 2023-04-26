# Common Ground Checklist#
Gegenereerd met: 
https://commonground-checklist.appsemble.appsemble.app/nl/item-checklist

## CG01 Moderne REST/json API-koppelvlakken

### Praktijk

Voor koppelingen tussen software(componenten) wordt gestandaardiseerd op het gebruik van moderne REST/json API-koppelvlakken. API's worden vormgegeven volgens de NL API Strategie, waar met partners aan wordt samengewerkt.

### SMART

1. Welke koppelvlakken worden gebruikt? (REST/json, SOAP, etc.)
2. In hoeverre wordt de NL API Strategie toegepast? Zie https://docs.geostandaarden.nl/api/API-Strategie/

### Normen

1. De software heeft REST/json API-koppelvlakken.
2. Alle services die de software aanbiedt op laag 2 zijn voorzien van REST/json API-koppelvlakken.
3. Alle koppelvlakken van de software zijn beschikbaar als REST/json API's.
4. De software maakt enkel gebruik van REST/json koppelvlakken.
5. De software maakt enkel gebruik van REST/json koppelvlakken en deze zijn ingericht volgens de NL API Strategie.

###### Score

2

###### Opmerking

De applicatie is bedoeld om de oude soap-stuf zds webservices te converteren naar de nieuwe rest/json zds koppelvlakken

## CG02 Data realtime gebruiken bij de bron

### Praktijk

Gegevens worden niet meer opgeslagen in procesapplicaties (laag 4 of 5), maar in basisregistraties, gemeentelijke kernregistraties en specifieke registers die via services worden benaderd. Voor elk type gegeven wordt een heldere keuzegemaakt over wat daarvoor het authentieke bronregister is (dat kan in specifieke uiteraard ook een applicatiespecifiek registercomponent zijn). Er worden geen lokale kopieën van registers gemaakt, en geen gegevens gedupliceerd in andere registers.

### SMART

1. Worden er gegevens in procesapplicaties opgeslagen?
2. Wat is voor elk type gegeven dat verwerkt wordt de authentieke bron?
3. Wordt er gebruik gemaakt van datadistributie?
4. Worden gegevens uit authentieke registers gedupliceerd in nieuwe registers?
5. Welke organisaties zijn verantwoordelijk voor welke authentieke registers?

### Normen

1. Gegevens worden niet in procesapplicaties opgeslagen.
2. Gegevens worden niet in procesapplicaties opgeslagen, en voor ieder type gegeven wordt opgeslagen in een authentiek bronregister dat past bij de gemeentelijke API-standaarden.
3. Gegevens worden niet in procesapplicaties opgeslagen, en voor ieder type gegeven wordt opgeslagen in een authentiek bronregister dat past bij de gemeentelijke API-standaarden. Authentieke registers worden realtime 'bij de bron' gebruikt (er vindt geen datadistributie plaats).
4. Gegevens worden niet in procesapplicaties opgeslagen, en voor ieder type gegeven wordt opgeslagen in een authentiek bronregister dat past bij de gemeentelijke API-standaarden. Authentieke registers worden realtime 'bij de bron' gebruikt (er vindt geen datadistributie plaats). Gegevens uit authentieke bronnen worden niet gekopieerd naar andere registers.
5. Gegevens worden niet in procesapplicaties opgeslagen, en voor ieder type gegeven wordt opgeslagen in een authentiek bronregister dat past bij de gemeentelijke API-standaarden. Authentieke registers worden realtime 'bij de bron' gebruikt (er vindt geen datadistributie plaats). Gegevens uit authentieke bronnen worden niet gekopieerd naar andere registers. Registers zijn belegd bij organisaties wiens taak de bijhouding van die registers omvat.

###### Score

4

###### Opmerking

Geen opmerking

## CG03 Continu een werkend en toonbaar (tussen)resultaat opleveren

### Praktijk

De actuele versie van de software wordt (vanaf het einde van iedere ontwikkelcyclus) beschikbaar gesteld om deze te kunnen testen. Zoveel mogelijk kwaliteitsaspecten van de software worden geautomatiseerd getest en de kwaliteit is inzichtelijk.

### SMART

1. Waar staat de demo-omgeving voor de software?
2. Waar staan testcode en het rapport van de geautomatiseerde tests van de software?
3. Welke zaken worden geautomatiseerd getest?

### Normen

1. Alle stakeholders kunnen de na afloop van iedere ontwikkelcyclus de laatste versie van de software op ieder moment testen in een demo-omgeving.
2. Alle stakeholders kunnen de na afloop van iedere ontwikkelcyclus de laatste versie van de software op ieder moment testen in een demo-omgeving, en er is een geautomatiseerd rapport beschikabar waaruit blijkt dat de werking van de software geautomatieerd wordt getest.
3. Er is een publieke demo-omgeving waar de laatste versie van de software te testen is, en iedereen kan het rapport inzien waaruit blijkt dat de werking van de software geautomatieerd wordt getest.
4. Er is een publieke demo-omgeving waar de laatste versie van de software te testen is, en iedereen kan het rapport inzien waaruit blijkt dat de werking van de software geautomatieerd wordt getest op tenminste toegankelijkheid, werking en code coverage.
5. Er is een publieke demo-omgeving waar de laatste versie van de software te testen is, en iedereen kan het rapport inzien waaruit blijkt dat de werking van de software geautomatieerd wordt getest op tenminste toegankelijkheid, werking, code coverage en code complexity.

###### Score

4

###### Opmerking

Geen demo omgeving, wel geautomatiseerde testen

## CG04 Hergebruik en samenwerken aan software(componenten) in de community

### Praktijk

Gemeenten en partners werken samen aan software die herbruikbaar is. Software is voor meerdere gemeenten bruikbaar en kan door meerdere leveranciers worden geleverd.

### SMART

1. (Hoe) kan de software door meerdere gemeenten worden gebruikt?
2. (Hoe) kan de software door meerdere leveranciers worden gebruikt?

### Normen

1. De software kan door meerdere gemeenten worden gebruikt.
2. De software kan door meerdere gemeenten worden gebruikt en de software kan door meerdere leveranciers worden gebruikt.
3. De software kan door meerdere gemeenten worden gebruikt en de software kan door meerdere leveranciers worden gebruikt. De software wordt opgeleverd in de vorm van docker images en documentatie hoe de software gebruikt kan worden.
4. De software kan door meerdere gemeenten worden gebruikt en de software kan door meerdere leveranciers worden gebruikt. De software wordt opgeleverd in de vorm van docker images en documentatie hoe de software gebruikt kan worden. Meerdere leveranciers werken samen aan de software.
5. De software kan door meerdere gemeenten worden gebruikt en de software kan door meerdere leveranciers worden gebruikt. De software wordt opgeleverd in de vorm van docker images en documentatie hoe de software gebruikt kan worden. Meerdere leveranciers werken samen aan de software. Meerdere leveranciers kunnen de software leveren aan gemeenten.

###### Score

3

###### Opmerking

Geen opmerking

## CG05 Beheer van de software is onderdeel van ontwikkeling

### Praktijk

Het beheer van software gebeurt vanaf het begin van de ontwikkeling door middel van devops, zodat bruikbaarheid, kwaliteit en veiligheid vanaf het begin geregeld zijn.

### SMART

1. Wie is de eigenaar van de software?
2. Hoe is beheer van de software geregeld? (denk aan ondersteuning, security-patches, softwareupdates en de reactietijd in SLA's)
3. Welke onderdelen van beheer zijn geautomatiseerd?
4. In welke mate wordt de Standard for Public Code toegepast voor beheer van de softwarecode? Zie https://standard.publiccode.net/

### Normen

1. De eigenaar van de software is bekend en de manier waarop software beheerd wordt is gedocumenteerd.
2. De eigenaar van de software is bekend en er is duidelijk beschreven hoe ondersteuning, security-patches, en softwareupdates geregeld zijn.
3. De eigenaar van de software is bekend en er is duidelijk beschreven hoe ondersteuning, security-patches, en softwareupdates geregeld zijn. Het is duidelijk hoeveel ondersteuning er mogelijk is en wat daarvoor reactietijden/SLA's zijn.
4. De eigenaar van de software is bekend en er is duidelijk beschreven hoe ondersteuning, security-patches, en softwareupdates geregeld zijn. Het is duidelijk hoeveel ondersteuning er mogelijk is en wat daarvoor reactietijden/SLA's zijn. Security-patches en software-updates kunnen geautomatiseerd plaatsvinden.
5. De eigenaar van de software is bekend en er is duidelijk beschreven hoe ondersteuning, security-patches, en softwareupdates geregeld zijn. Het is duidelijk hoeveel ondersteuning er mogelijk is en wat daarvoor reactietijden/SLA's zijn. Security-patches en software-updates kunnen geautomatiseerd plaatsvinden. De broncode van de software wordt beheerd volgens de Standard for Public Code.

###### Score

4

###### Opmerking

Geen opmerking

## CG06 Planning en voortgang delen in de community

### Praktijk

Gemeenten en partners maken hun planning, voortgang en werkwijze actief bekend in de Common Ground community.

### SMART

1. Waar staan doelstellingen van de software?
2. Waar staat de informatie over planning, voortgang en samenwerking?
3. Waar staat informatie over de componenten waar de software uit bestaat?

### Normen

1. Informatie over planning, voortgang en samenwerking is alleen voor directe stakeholders beschikbaar.
2. Informatie over planning, voortgang en samenwerking is publiek beschikbaar op de eigen website.
3. Informatie over planning, voortgang en samenwerking is beschikbaar op commonground.nl.
4. Informatie over planning, voortgang en samenwerking is beschikbaar in de Common Ground Componentencatalogus.
5. Informatie over planning, voortgang en samenwerking is beschikbaar in de Common Ground Componentencatalogus en daarbij is voor ieder component inzichtelijk wanneer het beschikbaar komt.

###### Score

5

###### Opmerking

Geen opmerking

## CG07 Kennis en informatie delen in de community

### Praktijk

Kennis, leerpunten en valkuilen tijdens de ontwikkeling van de software wordt actief gedeeld met andere leden van de community, bijvoorbeeld via blogs, chatgroepen en evenementen. Documentatie inclusief doelstelling van de softare, installatieprocedure, configuratiemogelijkheden en gebruikershandleiding zijn gedocumenteerd en beschikbaar.

### SMART

1. Waar staat de documentatie over de configutatie en het gebruik van de software?
2. Waar staat de documentatie over de werking van de software?
3. Is het mogelijk de software uit te proberen in een testversie, en zo ja hoe/waar?
4. Waar worden leerpunten en valkuilen gedeeld?

### Normen

1. Er is documentatie op een publieke website beschikbaar.
2. Heldere documentatie over architectuur, procesmodellen, algoritmen en gegevensverwerkingen van de software is op een publieke website beschikbaar.
3. Heldere documentatie over architectuur, procesmodellen, algoritmen en gegevensverwerkingen van de software is op een publieke website beschikbaar. Leerpunten en valkuilen worden gedeeld via commonground.nl.
4. Heldere documentatie over architectuur, procesmodellen, algoritmen en gegevensverwerkingen van de software is op een publieke website beschikbaar. Leerpunten en valkuilen worden gedeeld via commonground.nl, en de software kan in een testversie worden uitgeprobeerd.
5. Heldere documentatie over architectuur, procesmodellen, algoritmen en gegevensverwerkingen van de software is op een publieke website beschikbaar. Leerpunten en valkuilen worden gedeeld via commonground.nl, en de software kan in een testversie worden uitgeprobeerd. Het is in de community duidelijk hoe het mogelijk is om in contact te komen met het team dat aan de software werkt.

###### Score

5

###### Opmerking

Geen opmerking

## CG08 Gebruik van containertechnologie en Haven-compliancy

### Praktijk

Om software uit kleinere componenten te kunnen samenstellen, beter te kunnen schalen, en wendbaarder te zijn, wordt containertechnologie (Docker) en de Haven-compliant infrastructuur gebruikt. Zo kunnen stukken software als bouwblokken worden opgeleverd en makkelijk worden gecombineerd, en op elke moderne infrastructuur worden gebruikt.

### SMART

1. Wordt de software uitgevoerd met behulp van containertechnologie?
2. Waar worden docker container images gepubliceerd?
3. Wat is de gemiddelde bestandsomvang van de containers die onderdeel zijn van de software?
4. Wat is het gemiddelde geheugengebruik van de containers die onderdeel zijn van de software?
5. Worden installatiescripts meegeleverd om de software op Haven-compliant infrastructuur te kunnen uitvoeren?

### Normen

1. De software maakt geen gebruik van functionaliteit die enkel op één specifieke infrastructuur werkt.
2. De software wordt uitgevoerd met behulp van containertechnologie (Docker)
3. De software kan worden uitgevoerd door middel van het starten van de benodigde containers (`docker run <image>` werkt).
4. Er worden installatiescripts meegeleverd om de software gemakkelijk op iedere Haven-compliant infrastructuur te kunnen uitvoeren.
5. De containers die nodig zijn om de software te gebruiken om een businessprobleem op te lossen, kunnen vanuit de Common Ground Componentencatalogus gemakkelijk worden geïnstalleerd op iedere Haven-compliant infrastructuur.

###### Score

3

###### Opmerking

Geen opmerking

## CG09 Geautomatiseerd bouwen, testen versies en releasen CI/CD

### Praktijk

Softwarecode wordt bijgehouden in versiebeheersystemen, en heeft een duidelijk systeem voor het uitbrengen van nieuwe versies. Nieuwe versies kunnen zoveel mogelijk geautomatiseerd worden gereleased en getest. Tijdens het releasen worden kwaliteitsaspecten van software geautomatiseerd getest, zodat met een gerust gevoel kan worden geupdate naar de volgende versie. Zo kan de doorlooptijd voor het toevoegen van nieuwe functionaliteit en het oplossen van problemen omlaag.

### SMART

1. Wordt er gebruik gemaakt van versiebeheer voor code en releases? Zo ja van welke systemen?
2. Hoe vaak per week kan er gereleased worden?
3. Wordt gebruik gemaakt van een geautomatiseerde CI/CD-straat? (bijv functionaliteit/regressie, toegankelijkheid, code complexity en test coverage)
4. Welke zaken worden bij het maken van een nieuwe release geautomatiseerd getest? (denk aan functionaliteit/regressie, toegankelijkheid, code complexity, code coverage)
5. Wat is de doorlooptijd van het deployen van een nieuwe versie van de software naar producti met een eenvoudige (bijvoorbeeld enkel tekstuele) wijziging?

### Normen

1. Codeversies en softwareversies worden duidelijk bijgehouden volgens een versiebeheersysteem.
2. Codeversies en softwareversies worden duidelijk bijgehouden volgens een versiebeheersysteem, en het is mogelijk om minimaal wekelijks een nieuw versie van de software te releasen.
3. Het releasen van nieuwe versies van de software kan volledig geautomatiseerd.
4. Het releasen van nieuwe versies van de software kan volledig geautomatiseerd en daarbij worden nieuwe versies geautomatiseerd getest op functionaliteit/regressie, toegankelijkheid, code complexity en test coverage.
5. Het deployen van nieuwe versies van de software naar productie kan volledig geautomatiseerd en daarbij worden nieuwe versies geautomatiseerd getest op functionaliteit/regressie, toegankelijkheid en code complexity en test coverage. De doorlooptijd voor het doorvoeren van een kleine softwareupdate naar productie is korter dan een uur.

###### Score

5

###### Opmerking

Geen opmerking

## CG10 Software is bruikbaar in moderne browsers en op moderne besturingssystemen

### Praktijk

Software stelt geen bijzondere eisen aan de systemen van de gebruikers van de software, maar werkt juist op alle moderne browsers en besturingssystemen. Dat zorgt er voor dat software platformonafhankelijk kan werken en enkel moderne en veilige (up to date) platforms hoeven worden ondersteund.

### SMART

1. In welke browsers en/of op welke besturingssystemen is de software te geruiken?
2. Zijn er bijzondere plugins of andere runtime-vereisten bij het gebruiken van de software?

### Normen

1. De software vereist geen verouderde browser, besturingssysteem of plugin.
2. De software werkt in meerdere browsers of besturingssystemen.
3. De software werkt in meerdere moderne browsers of besturingssystemen en vereist geen specifieke plugins of versies van browsers of besturingssystemen.
4. De software is te gebruiken op alle up-to-date moderne browsers (Chrome, Firefox, Opera, Edge, Internet Explorer, Safari) of besturingssystemen (Windows, Linux, MacOS)
5. De software is te gebruiken op alle up-to-date moderne besturingssystemen (Windows, Linux, MacOS) en (indien van toepassing) in elke moderne browser (Chrome, Firefox, Opera, Edge, Internet Explorer, Safari).

###### Score

5

###### Opmerking

Geen opmerking

## CG11 Open source software

### Praktijk

Softwarecode wordt (tenzij er een goed onderbouwde reden is om dat niet te doen) als open source software vrijgegeven onder een EUPL-licentie (of gelijkwaardig). Het (her)gebruik van open source software wordt expliciet gemaakt en er wordt bijgedragen aan bestaande open source software.

### SMART

1. Welke bestaande open source software wordt hergebruikt?
2. Is de broncode van de software publiek beschikbaar?
3. Onder welke licentie is de broncode beschikbaar en waar staat deze licentie?
4. Zijn er een CODE_OF_CONDUCT.md en een CONTRIBUTING.md aanwezig, waarin beschreven staat hoe andere partijen kunnen bijdragen aan de software?
5. Is er een opengov.yaml metadatabestand aanwezig?

### Normen

1. In de software wordt bestaande open source software wordt hergebruikt (en welke dat is), en dit hergebruik is gedocumenteerd.
2. De broncode van de software is op verzoek beschikbaar.
3. De broncode van de software is publiek beschikbaar op een gangbare Git-omgeving (bijvoorbeeld Gitlab of Github)
4. De broncode van de software is publiek beschikbaar en voorzien van een EUPL licentie of minimaal gelijkwaardig (MIT, BSD, Apache 2.0).
5. De broncode van de software is publiek beschikbaar en voorzien van een EUPL licentie of minimaal gelijkwaardig (MIT, BSD, Apache 2.0), en er zijn een CODE_OF_CONDUCT.md en een CONTRIBUTING.md opgenomen in de repository, waarin beschreven staat hoe andere partijen kunnen bijdragen aan de software.

###### Score

4

###### Opmerking

Geen opmerking

## CG12 Componenten hebben een afgebakende functie binnen de vijflagen-architectuur

### Praktijk

Software wordt opgedeeld in kleinere zelfstandig bruikbare componenten die een duidelijk afgebakende functie binnen de vijflagen-architectuur hebben. Afhankelijkheden tussen componenten zijn in beeld.

### SMART

1. Waar zijn de functies van componenten gedocumenteerd?
2. Waar is gedocumenteerd welke component zich op welke laag van het architectuurmodel bevindt?
3. Waar zijn de afhankelijkheden tussen componenten gedocumenteerd?

### Normen

1. Voor alle componenten is helder gedocumenteerd welke functies deze vervullen.
2. De documentatie van de software bevat een architectuurplaat waarin ieder component geplot is op de vijflagen-architectuur.
3. De documentatie van de software bevat een architectuurplaat waarin ieder component geplot is op de vijflagen-architectuur, en daarin zijn componenten die data-services afnemen (procesapplicaties) duidelijk gescheiden van componenten die data-services aanbieden (dataregistercomponenten).
4. De documentatie van de software bevat een architectuurplaat waarin ieder component geplot is op de vijflagen-architectuur, en daarin zijn componenten die data-services afnemen (procesapplicaties) duidelijk gescheiden van componenten die data-services aanbieden (dataregistercomponenten). Bovendien zijn user interfaces gescheiden van procesimplementaties, en dataservices van data-opslag.
5. De documentatie van de software bevat een architectuurplaat waarin ieder component geplot is op de vijflagen-architectuur, en daarin zijn componenten die data-services afnemen (procesapplicaties) duidelijk gescheiden van componenten die data-services aanbieden (dataregistercomponenten). Bovendien zijn user interfaces gescheiden van procesimplementaties, en dataservices van data-opslag. Componenten op onderliggende lagen zijn nooit afhankelijk van bovenliggende lagen.

###### Score

1

###### Opmerking

Geen opmerking

## CG13 Laag 5 Wendbare maar herkenbare user interfaces

### Praktijk

User interfaces zijn kleine en zelfstandig bruikbare componenten die wendbaar zijn en makkelijk vervangen kunnen worden. Ze zijn gericht op het uitvoeren van specifieke processen door specifieke gebruikers.

### SMART

1. In welke componenten zijn user interfaces opgedeeld?
2. Zijn thema's voor verschillende organisaties beschikbaar? (bijvoorbeeld thema's voor alle afnemende gemeenten)

### Normen

1. User interface-componenten zijn zelfstandig bruikbaar (deploybaar als docker containers) en zijn niet opgenomen in een ander component.
2. User interfaces voor verschillende doelgroepen of duidelijk verschillende processen zijn zelfstandig bruikbaar (deploybaar als docker containers, 'micro-frontends').
3. User interfaces voor verschillende doelgroepen of duidelijk verschillende processen zijn zelfstandig bruikbaar (deploybaar als docker containers, 'micro-frontends'), maar de visuele elementen en user experience zijn voor gebruikers consistent omdat gebruik wordt gemaakt van een design system.
4. User interfaces voor verschillende doelgroepen of duidelijk verschillende processen zijn zelfstandig bruikbaar (deploybaar als docker containers, 'micro-frontends'), maar visuele elementen zijn voor gebruikers consistent omdat gebruik wordt gemaakt van een gedeeld design system met thema's voor alle afnemende gemeenten.
5. User interfaces voor verschillende doelgroepen of duidelijk verschillende processen zijn zelfstandig bruikbaar (deploybaar als docker containers, 'micro-frontends'), maar visuele elementen zijn voor gebruikers consistent omdat gebruik wordt gemaakt van het NL Design System, met thema's voor alle afnemende organisaties.

###### Score

1

###### Opmerking

Geen opmerking

## CG14 Laag 4 Procesimplementatie in afgebakende componenten

### Praktijk

Processen worden geimplementeerd in afzonderlijke procescomponenten die een helder afgebakende scope hebben ("do one thing and do it well"). Processtappen zoals het ontvangen van invoer uit user interfaces, het uitvoeren van businesslogica, of het doorvoeren van data-acties (lezen/schrijven) zijn helder gedocumenteerd. Er wordt gewerkt aan gestandaardiseerde basisprocessen.

### SMART

1. Welke componenten implementeren processen, en welke processen?
2. Waar zijn procesimplementaties beschreven/gedocumenteerd? (bijvoorbeeld mbv BPMN)
3. Wordt gebruik gemaakt van process engines?
4. Worden er basisprocessen geimplementeerd?

### Normen

1. Er is helder gedocumenteerd welke processen in de software geimplementeerd worden.
2. Er is helder gedocumenteerd welke processen in de software geimplementeerd worden en het is duidelijk welk proces in welke component geimplementeerd wordt.
3. Er is helder gedocumenteerd welke processen in de software geimplementeerd worden en het is duidelijk welk proces in welke component geimplementeerd wordt. Voor ieder proces zijn afzonderlijke processtappen gedocumenteerd (bijvooreeld mbv BPMN).
4. Er is helder gedocumenteerd welke processen in de software geimplementeerd worden en het is duidelijk welk proces in welke component geimplementeerd wordt. Voor ieder proces zijn afzonderlijke processtappen gedocumenteerd (bijvooreeld mbv BPMN). Processen zijn zelfstandig uitvoerbaar en kennen geen dependencies op andere processen (geen spaghetti/subprocessen).
5. Er is helder gedocumenteerd welke processen in de software geimplementeerd worden en het is duidelijk welk proces in welke component geimplementeerd wordt. Voor ieder proces zijn afzonderlijke processtappen gedocumenteerd (bijvooreeld mbv BPMN). Elk proces is in een eigen afgebakend component geïmplementeerd.

###### Score

5

###### Opmerking

Geen opmerking

## CG15 Laag 3 Gegevens raadplegen bij andere organisaties mbv NLX

### Praktijk

NLX wordt gebruikt als landelijk integratiemechanisme tussen procesimplementaties en registers. NLX maakt het mogelijk om gegevens die in registers bij andere organisaties (of andere organisatieonderdelen) staan, te bevragen alsof ze lokaal beschikbaar zijn. Daarmee wordt het maken van kopieën voorkomen en kunnen gevens op een goede manier worden gebruikt. Organisaties die NLX gebruiken zijn zelf verantwoordelijk dat ze dit op de juiste manier doen.

### SMART

1. Kan de software data aanbieden of afnemen via NLX? Om welke registers gaat het dan?
2. Wordt er een voorbeeldconfiguratie meegeleverd om de software icm NLX te gebruiken? Waar staat dit voorbeeld?
3. Vindt gegevensuitwisseling tussen organisaties in de software altijd plaats met gebruik van NLX of ook buiten NLX om? Om welke gegevensuitwisselingen gaat dit?
4. Is het mogelijk de software
5. Worden vanuit de ontwikkelaars van de software bijgedragen aan de doorontwikkeling van NLX? (bijvoorbeeld door bugs en gewenste features te melden of op te lossen voor de hele community).

### Normen

1. Software is geschikt om gegevensuitwisseling via NLX uit te voeren.
2. Er wordt een bruikbare voorbeeldconfiguratie voor gebruik van de software icm NLX meegeleverd.
3. De software gebruikt enkel NLX voor gegevensuitwisseling tussen organisaties, maar ten behoeve van transitie wordt nog niet van de juiste bron gebruik gemaakt (bijv een lokale versie van een registratie ipv een landelijk unieke bron of vice versa).
4. De software gebruikt enkel NLX voor gegevensuitwisseling tussen organisaties, en gegevens worden bij de juiste bron geraadpleegd.
5. De software gebruikt enkel NLX voor gegevensuitwisseling tussen organisaties, en gegevens worden bij de juiste bron geraadpleegd. Door de ontwikkelaar van de software wordt bijgedragen aan de doorontwikkeling van NLX.

###### Score

1

###### Opmerking

Geen opmerking

## CG16 Laag 2 Data-services zijn onafhankelijke componenten

### Praktijk

Dataservices zijn onafhankelijke componenten die geen businesslogica bevatten (structurele datavalidatie of filtering op basis van autorisaties wordt niet beschouwd als proceslogica, maar als een technische data-service-functie).

### SMART

1. Welke data-services (laag 2) worden door de software aangeboden (welke gegevens worden met welk koppelvlak aangeboden)?
2. Hoe verhouden data-services zich tot elkaar?
3. Zijn data-services onafhankelijk van elkaar en andere componenten bruikbaar (deploybaar), of zijn er onderling afhankelijkheden?
4. Is er proceslogica aanwezig in data-services? Zo ja, welke?

### Normen

1. Er is duidelijk gedocumenteerd welke data-services door de software worden aangeboden.
2. Alle gegevens in de software worden ontsloten via data-services en deze zijn helder gedocumenteerd.
3. Alle gegevens in de software worden ontsloten via in afzonderlijke servicecomponenten gerealiseerde data-services en de indeling daarvan is helder gedocumenteerd.
4. Alle gegevens in de software worden ontsloten via in afzonderlijke servicecomponenten gerealiseerde data-services en de indeling daarvan is gedocumenteerd. Servicecomponenten bevatten geen proceslogica.
5. Alle gegevens in de software worden ontsloten via in afzonderlijke servicecomponenten gerealiseerde data-services en de indeling daarvan is gedocumenteerd. Servicecomponenten bevatten geen proceslogica. Er zijn geen harde relaties tussen meerdere registers onderling (combineren gebeurt op procesniveau bijv mbv linked data).

###### Score

3

###### Opmerking

Geen opmerking

## CG17 Laag 1 Opslag van gegevens is een commodity

### Praktijk

Het is duidelijk waar en hoe gegevens worden opgeslagen. Opslag van gegevens gebeurt zo eenvoudig mogelijk in gangbare systemen, zodat gegevens gemakkelijk door services ontsloten kunnen worden. Componenten voor gegevensopslag bevatten geen businesslogica.

### SMART

1. Welke gegevens worden door de software in welke componenten opgeslagen? Welke typen gegevensopslag worden vereist, aangeraden of ondersteund?
2. Welke businesslogica is aanwezig in componenten voor gegevensopslag?
3. Worden verschillende gegevenssets in afzonderlijke databases opgeslagen?

### Normen

1. Er is helder gedocumenteerd hoe en waar gegevens worden opgeslagen, en welke typen gegevenopslag ondersteund worden.
2. Er is helder gedocumenteerd hoe en waar gegevens worden opgeslagen, en welke typen gegevenopslag ondersteund worden. Componenten voor gegevensopslag zijn gescheiden van andere componenten.
3. Er is helder gedocumenteerd hoe en waar gegevens worden opgeslagen, en welke typen gegevenopslag ondersteund worden. Componenten voor gegevensopslag zijn gescheiden van andere componenten, waarbij gebruik wordt gemaakt van gangbare moderne databasesystemen.
4. Er is helder gedocumenteerd hoe en waar gegevens worden opgeslagen, en welke typen gegevenopslag ondersteund worden. Componenten voor gegevensopslag zijn gescheiden van andere componenten, waarbij gebruik wordt gemaakt van gangbare moderne databasesystemen. Componenten voor gegevensopslag bevatten geen businesslogica (zoals bijvoorbeeld stored procedures).
5. Er is helder gedocumenteerd hoe en waar gegevens worden opgeslagen, en welke typen gegevenopslag ondersteund worden. Componenten voor gegevensopslag zijn gescheiden van andere componenten, waarbij gebruik wordt gemaakt van gangbare moderne databasesystemen. Componenten voor gegevensopslag bevatten geen businesslogica (zoals bijvoorbeeld stored procedures). Verschillende gegevens worden in onafhankelijke databases opgeslagen.

###### Score

5

###### Opmerking

Geen opmerking

## CG18 Transparante werking van software

### SMART

1. Waar staat de werking van de software gedocumenteerd?
2. Zijn er BPMN-modellen beschikbaar van geimplementeerde processen?
3. Worden er besluiten gecommuniceerd aan burgers of bedrijven zonder dat de precieze totstandkoming van die besluiten gecommuniceerd wordt?

### Normen

1. Documentatie van de werking van de software (procesbeschrijvingen) is publiek beschikbaar.
2. Documentatie van de werking van de software (procesbeschrijvingen) is publiek beschikbaar en processen zijn gemodelleerd in BPMN.
3. Documentatie van de werking van de software (procesbeschrijvingen) is publiek beschikbaar en processen zijn gemodelleerd in BPMN, waarbij duidelijk is aangegeven welke actoren (burgers, bedrijven, medewerkers, andere partijen) welke processtappen uitvoeren.
4. Documentatie van de werking van de software (procesbeschrijvingen) is publiek beschikbaar en processen zijn gemodelleerd in BPMN, waarbij duidelijk is aangegeven welke actoren (burgers, bedrijven, medewerkers, andere partijen) welke processtappen uitvoeren. Wanneer besluiten aan eindgebruikers worden gecommuniceerd, wordt daarbij gecommuniceerd hoe het besluit precies tot stand is gekomen.
5. Documentatie van de werking van de software (procesbeschrijvingen) is publiek beschikbaar en processen zijn gemodelleerd in BPMN, waarbij duidelijk is aangegeven welke actoren (burgers, bedrijven, medewerkers, andere partijen) welke processtappen uitvoeren. Wanneer besluiten aan eindgebruikers worden gecommuniceerd, wordt daarbij gecommuniceerd hoe het besluit precies tot stand is gekomen. De werking van de software is volledig inzichtelijk omdat de broncode publiek beschikbaar is.

###### Score

1

###### Opmerking

Geen opmerking

## CG19 Open ontwikkeling van nieuwe features

### Praktijk

Het is transparant inzichtelijk welke nieuwe features onderdeel kunnen worden van de software. Binnen de community is bekendgemaakt welke features te verwachten zijn, en iedereen kan nieuwe features voorstellen.

### SMART

1. Waar staat de prioriteit van nieuwe features?
2. Is het mogelijk om features toe te voegen aan een publieke backlog?

### Normen

1. Er is een roadmap gepubliceerd van aankomende nieuwe features.
2. De backlog met features (user stories) is inzichtelijk voor de opdrachtgevers.
3. De backlog van features (user stories) is inzichtelijk voor de Common Ground community.
4. De backlog van features (user stories) is publiek beschikbaar.
5. De backlog van features (user stories) is publiek beschikbaar. Iedereen kan features toevoegen/voorstellen aan de backlog van features.

###### Score

Niet compatibel

###### Opmerking

Geen opmerking

## CG20 Multilevel authenticatie en autorisatie

### Praktijk

Er wordt multi-level authenticatie en autorisatie gebruikt; gebruikersauthenticatie en autorisatie (van bijvoorbeeld burgers of medewerkers) vindt plaats op het niveau van natuurlijke personen en binnen de context van processapplicaties. Authenticatie en autorisatie bij gegevensuitwisseling mbv data-services vindt plaats op organisatieniveau (bijvoorbeeld een gemeente die gegevens raadpleegt verkrijgt toegang tot een data-service bij een ketenpartner).

### SMART

1. Op welke manier vindt authenticatie en autorisatie van gebruikers (natuurlijke personen) plaats?
2. Op welke manier vindt authenticatie en autorisatie van organisaties bij gegevensuitwisseling plaats?

### Normen

1. De authenticatie van gebruikers bij procesapplicaties is adequaat geregeld.
2. De authenticatie van gebruikers bij procesapplicaties is adequaat geregeld en processen kunnen alleen worden uitgevoerd door gebruikers die de juiste autorisaties hebben.
3. De authenticatie van gebruikers bij procesapplicaties is adequaat geregeld en processen kunnen alleen worden uitgevoerd door gebruikers die de juiste autorisaties hebben. Er is ondersteuning om rollen en rechten van gebruikers uit een afzonderlijk identity provider component op te halen (bijvoorbeeld een interne active directory of openid connect IDP).
4. De authenticatie van gebruikers bij procesapplicaties is adequaat geregeld en processen kunnen alleen worden uitgevoerd door gebruikers die de juiste autorisaties hebben. Er is ondersteuning om rollen en rechten van gebruikers uit een afzonderlijk identity provider component op te halen (bijvoorbeeld een interne active directory of openid connect IDP). Gegevensuitwisseling met andere organisaties vindt enkel plaats op basis van autorisaties.
5. De authenticatie van gebruikers bij procesapplicaties is adequaat geregeld en processen kunnen alleen worden uitgevoerd door gebruikers die de juiste autorisaties hebben. Er is ondersteuning om rollen en rechten van gebruikers uit een afzonderlijk identity provider component op te halen (bijvoorbeeld een interne active directory of openid connect IDP). Gegevensuitwisseling met andere organisaties vindt enkel plaats op basis van autorisaties, waarbij autorisatie plaatsvindt op het niveau van de afnemende organisatie.

###### Score

Niet compatibel

###### Opmerking

Geen opmerking

## CG21 Inzicht in en invloed op gegevensverwerking

### Praktijk

Gemeenten en andere organisaties zijn 'in control' als het gaat om verwerking van (persoons)gegevens. Er wordt bijgehouden welke gegevens verwerkt worden en met welk doel, en burgers kunnen van hun rechten mbt gegevensverwerking gebruik maken, inclusief het recht op inzage.

### SMART

1. Welke gegevens worden verwerkt, en met welke wettelijke basis, grondslag en doelbinding?
2. Worden alle gegevensverwerkingen bijgehouden in een logging- en verwerkingsregister? Gebeurt dit in lijn met de beschrijving van 'Logging van verwerking van gegevens' (zie https://www.gemmaonline.nl/index.php/Gegevenslandschap)?
3. Op welke manier draagt de software bij aan inzicht voor burgers en bedrijven in verwerking van gegevens?
4. Hoe kunnen gebruikers incorrecte gegevens(verwerkingen) melden/herstellen?
5. Welke informatie kan door gebruikers worden geexporteerd teneinde een eigen dossier op te bouwen?

### Normen

1. De software registreert alle exacte verwerkingen van gegevens.
2. De software registreert alle exacte verwerkingen van gegevens, inclusief wettelijke grondslag en doelbinding.
3. De software registreert alle exacte verwerkingen van gegevens, inclusief wettelijke grondslag en doelbinding. De software draagt eraan bij dat burgers en bedrijven inzicht verkrijgen in deze logging.
4. De software registreert alle exacte verwerkingen van gegevens, inclusief wettelijke grondslag en doelbinding. De software draagt eraan bij dat burgers en bedrijven inzicht verkrijgen in deze logging, en dat zij op gemakkelijke wijze foutieve gegevens kunnen melden/herstellen.
5. De software registreert alle exacte verwerkingen van gegevens, inclusief wettelijke grondslag en doelbinding. De software draagt eraan bij dat burgers en bedrijven inzicht verkrijgen in deze logging, en dat zij op gemakkelijke wijze foutieve gegevens kunnen melden/herstellen. Burgers en bedrijven kunnen gegevens die verwerkt worden exporteren naar een open formaat zodat zij een eigen dossier op kunnen bouwen van gegevensverwerking door de overheid.

###### Score

1

###### Opmerking

Geen opmerking

## CG22 Informatiebeveiliging op orde

### Praktijk

Software voldoet aan alle wettelijke vereisten rondom informatiebeveiliging en privacybescherming. Er wordt maximaal gebruik gemaakt van security en privacy by design en by default. Gegevensverwerkingen worden in kaart gebracht dmv een DPIA, en bijgehouden dmv een register van logging en verwerking. Waar mogelijk worden (inzichten in beschikbare) security-updates geautomatiseerd.

### SMART

1. Waar staat de assesment/rapport over de toepassing van de Baseline Informatiebeveiliging Overheid?
2. Waar is de DPIA-rapporage te vinden?
3. Hoe zijn Privacy en Security by Design toegepast?
4. Welke gegevens worden voor welke doeleinden in de software verwerkt?
5. Waar worden gegevensverwerkingen geregistreerd tbv logging- en auditdoeleinden?

### Normen

1. Uit de BIO-assesment blijkt dat er geen onacceptabele beveiligingsrisico's aanwezig zijn.
2. Uit de BIO-assesment blijkt dat er geen onacceptabele beveiligingsrisico's aanwezig zijn, en uit het DPIA-rapport is duidelijk dat er geen grote risico's zijn voor de informatieveiligheid.
3. Uit de BIO-assesment blijkt dat er geen onacceptabele beveiligingsrisico's aanwezig zijn, en uit het DPIA-rapport is duidelijk dat er geen grote risico's zijn voor de informatieveiligheid, en dat privacy en security by design en default zijn toegepast.
4. Uit de BIO-assesment blijkt dat er geen onacceptabele beveiligingsrisico's aanwezig zijn, en uit het DPIA-rapport is duidelijk dat er geen grote risico's zijn voor de informatieveiligheid, en dat privacy en security by design en default zijn toegepast. Alle verwerkingen van gegevens worden bijgehouden in een logging- en verwerkingsregister.
5. Uit de BIO-assesment blijkt dat er geen onacceptabele beveiligingsrisico's aanwezig zijn, en uit het DPIA-rapport is duidelijk dat er geen grote risico's zijn voor de informatieveiligheid, en dat privacy en security by design en default zijn toegepast. Alle verwerkingen van gegevens worden bijgehouden in een logging- en verwerkingsregister. Er is een geautomatiseerd, actueel inzicht in beschikbare veiligheidspatches voor de software of dependencies van de software.

###### Score

Niet compatibel

###### Opmerking

Geen opmerking

## CG23 Permanent beta en de invloed van de opdrachtgever op productkeuzes

### Praktijk

Software krijgt gedurende de (door)ontwikkeling vorm en de focus voor doorontwikkeling is continu aanpasbaar. Het is voor de opdrachtgever mogelijk om invloed uit te oefenen op productkeuzes. Door middel van goed opdrachtgeverschap voeren gemeenten regie over de architectuur van software-oplossingen.

### SMART

1. Wie is de Product Owner?
2. Zijn er periodieke sprint reviews?
3. Wie voert regie over de architectuur van de oplossing?

### Normen

1. De software is ontwikkeld op basis van een functionele vraag van de opdrachtgever.
2. De opdrachtgever kan in periodieke sprint reviews input leveren op het productkeuzes in de software.
3. De software wordt iteratief ontwikkeld op basis van keuzes van de opdrachtgever.
4. De opdrachtgever vervult de rol van Product Owner in het ontwikkelteam.
5. De opdrachtgever voert de regie over de architectuur van de oplossing.

###### Score

5

###### Opmerking

Geen opmerking

## CG24 De duur van ontwikkelcycli.

### Praktijk

De (door)ontwikkeling van de software is kortcyclisch geregeld zodat wendbaar gereageerd kan worden op veranderende omstandigheden. Aan het eind van iedere cyclus wordt een werkend resultaat getoond en wordt de scope van de volgende ontwikkelcyclus bepaald op basis van gebruikersfeedback de prioriteit van nieuwe features.

### SMART

1. Hoe lang duurt iedere ontwikkelcyclus (sprint)?
2. Is er een vaste cadans van ontwikkelcycli?
3. Is er een werkend resultaat aan het einde van iedere ontwikkelcyclus?

### Normen

1. Software wordt niet kortcyclisch (door)ontwikkeld.
2. Software wordt niet kortcyclisch (door)ontwikkeld, maar de doorontwikkeling heeft een vaste einddatum binnen één jaar.
3. Software wordt kortcyclisch (door)ontwikkeld. De ontwikkelcyclus is korter dan drie maanden, maar er is geen vaste cadans van ontwikkelcycli.
4. Software wordt kortcyclisch (door)ontwikkeld. De ontwikkelcyclus is korter dan een maand, maar er is geen vaste cadans van ontwikkelcycli.
5. Software wordt kortcyclisch (door)ontwikkeld. De ontwikkelcyclus is korter dan een maand en er is een vaste cadans van ontwikkelcycli.

###### Score

4

###### Opmerking

Geen opmerking

## CG25 Open Standaarden

### Praktijk

Er wordt maximaal gebruik gemaakt van standaarden en bij voorkeur met gebruik van (internationale) open standaarden die zorgen voor een zo groot mogelijke compatibiliteit van software. Er wordt gedocumenteerd welke standaarden gebruikt worden en er deze sluiten aan bij de verplichte en aanbevolen standaarden van het Forum Standaardisatie.

### SMART

1. Waar is gedocumenteerd welke standaarden in de software worden toegepast.
2. Welke toegepaste standaarden zijn open of gesloten? Zijn er licentiekosten?
3. Welke verplichte en aanbevolen standaarden van het Forum Standaardisatie worden toegepast?

### Normen

1. Er is gedocumenteerd welke standaarden in de software worden toegepast.
2. Er is gedocumenteerd welke open en gesloten standaarden in de software worden toegepast en of en welke licentiekosten vereist zijn.
3. Er is gedocumenteerd welke open en gesloten standaarden in de software worden toegepast en of en welke licentiekosten vereist zijn. De software past alle relevante verplichte standaarden van het Forum Standaardisatie toe.
4. Er is gedocumenteerd welke open en gesloten standaarden in de software worden toegepast en of en welke licentiekosten vereist zijn. De software past alle relevante verplichte en aanbevolen standaarden van het Forum Standaardisatie toe.
5. Er is gedocumenteerd welke open en gesloten standaarden in de software worden toegepast en of en welke licentiekosten vereist zijn. De software past alle relevante verplichte standaarden van het Forum Standaardisatie toe. De software gebruikt enkel open standaarden waarvoor geen licentiekosten vereist zijn.

###### Score

2

###### Opmerking

Geen opmerking

## CG26 API-standaarden uit de Agenda van het Gegevenslandschap

### Praktijk

Voor gegevens en berichtenverkeer stelt VNG gemeentelijke API-standaarden op. Deze standaarden worden opgesteld tegelijk met het uitproberen ervan in de praktijk, zodat zeker is dat ze goed werken. API-specificaties die nog geen standaard zijn worden bij VNG aangemeld om bij bewezen werking en meerwaarde tot standaard te worden verklaard.

### SMART

1. Welke gemeentelijke API-standaarden voor gegevens en berichtenverkeer worden in de software toegepast? (Zie https://www.gemmaonline.nl/index.php/API-standaarden)
2. Welke van deze API-standaarden worden NIET geimplementeerd en waarom?
3. Welke API's in de software zouden tot een nieuwe API-standaard voor gemeenten kunnen leiden?

### Normen

1. Voor de API's in de software zijn OAS3 specificaties beschikbaar, maar wordt geen gebruik gemaakt van bestaande standaarden.
2. De software implementeert alle relevante gemeentelijke API-standaarden voor gegevens- en berichtenverkeer die verbindend zijn verklaard.
3. De software implementeert alle relevante gemeentelijke API-standaarden voor gegevens- en berichtenverkeer die 'verbindend zijn verklaard' of 'in gebruik' zijn.
4. De software implementeert alle relevante gemeentelijke API-standaarden voor gegevens- en berichtenverkeer die 'verbindend zijn verklaard' of 'in gebruik' zijn, en draagt bij aan de ontwikkeling van nieuwe gemeentelijke API-standaarden voor gegevens- en berichtenverkeer.
5. De software implementeert alle relevante gemeentelijke API-standaarden voor gegevens- en berichtenverkeer die 'verbindend zijn verklaard' of 'in gebruik' zijn, en draagt bij aan de ontwikkeling van nieuwe gemeentelijke API-standaarden voor gegevens- en berichtenverkeer. De software draagt een potentiële nieuwe gemeentelijke API-standaard voor gegevens- en berichtenverkeer bij aan de community.

###### Score

5

###### Opmerking

Geen opmerking

## CG27 Expliciet maken hoe transitie plaats kan vinden

### Praktijk

De status van bestaande en nieuwe componenten wordt expliciet gemaakt en erkend. Omschakeling naar Common Ground vergt een transitie, wat betekent dat bestaande en nieuwe componenten naast elkaar kunnen bestaan, en beide hun doelmatige meerwaarde kennen.

### SMART

1. Welke nieuwe componenten worden ontwikkeld, welke bestaande componenten zijn nodig?
2. Is de transitiestatus van componenten te vinden in de Componentencatalogus?
3. Waar is het transitieplan te vinden?

### Normen

1. Er is in beeld gebracht welke nieuwe en bestaande componenten nodig zijn.
2. Nieuwe en bestaande componenten zijn inzichtelijk in de Common Ground Componentencatalogus.
3. Nieuwe en bestaande componenten zijn inzichtelijk in de Common Ground Componentencatalogus, en er is een transitieplan beschreven.
4. Nieuwe en bestaande componenten zijn inzichtelijk in de Common Ground Componentencatalogus, en in een transitieplan staat per component beschreven hoe lang dat component nodig is en wanneer het herzien of uitgefaseerd kan worden.
5. Nieuwe en bestaande componenten zijn inzichtelijk in de Common Ground Componentencatalogus, en in een transitieplan staat per component beschreven hoe lang dat component nodig is en wanneer het herzien of uitgefaseerd kan worden. Bovendien is beschreven hoe de nieuwe componenten in de toekomst weer vervangen kunnen worden.

###### Score

5

###### Opmerking

Geen opmerking