# Installatie

### Minimale configuratie OpenZaakBrug

Voor de minimale inrichting zijn er de volgende randvoorwaarden aan het systeem:

- Java 11, getest met OpenJDK11
- Maven
- Git
- Internet verbinding voor git en maven 

Ophalen en klaarzetten van de applicatie:

```
$ git clone https://github.com/Sudwest-Fryslan/OpenZaakBrug.git
```

```
$ cd OpenZaakBrug
```

```
$ mv src/main/resources/application.properties_example src/main/resources/application.properties
```

```
$ mv src/main/resources/config.json_example src/main/resources/config.json
```

### application.properties

Pas het openzaak endpoint aan:

- openzaak.baseUrl = https://openzaak.local
- openzaak.jwt.secret=test
- openzaak.jwt.issuer=test

Wanneer nodig, pas de database aan naar bijvoorbeeld postgresql:

- spring.datasource.driverClassName=org.postgresql.Driver
- spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
- spring.datasource.url= jdbc:postgresql://localhost/openzaakbrug
- spring.datasource.username=openzaakbrug
- spring.datasource.password=openzaakbrug

Bekijk de configuratie documentatie [Configuratie.md](Configuratie.md)

De default h2 database kan benaderd worden op [http://localhost:8080/h2-console/login.jsp](http://localhost:8080/h2-console/login.jsp) (zie ook het onderstaande scherm)

![openzaakbrug-h2](media/openzaakbrug-h2.png)

### config.json

Instellen van de applicatie, config.json

- Voor een werkende replication / proxy alle waarden aan voor "legacyservice" in het document naar het je bestaande legacy zaaksysteem
- Voor werkende replication alle waarden aan voor "url" in het document naar het je bestaande legacy zaaksysteem

Bekijk de configuratie documentatie [Configuratie.md](Configuratie.md)

## Running the service

Draaien van de service

```
$ cd OpenZaakBrug
$ mvn -Dhttps.protocols=TLSv1.2 spring-boot:run
```

Update en draaien van de applicatie

```
$ cd OpenZaakBrug
$ git pull
$ mvn spring-boot:run
```

Na het draaien van de commando&#39;s zijn de translate services bereikbaar op:

- http://localhost:8080/translate/generic/zds/VrijBericht
- http://localhost:8080/translate/generic/zds/OntvangAsynchroon
- http://localhost:8080/translate/generic/zds/BeantwoordVraag

Voor meer informatie, zie ook in Workings of Replication.md voor de overige urls

### Gebruik vanuit een backoffice applicatie

Hierna is het van belang om de client-applicatie aan te passen, in bijvoorbeeld Suite4SociaalDomein kan de functioneelbeheerder dit zelf doen in het volgende scherm:

Voor de basisurl zal dan http://%linuxserver%:8080/ gebruikt moeten worden

### Gebruik vanuit SoapUI

Binnen SoapUi kunnen dan de berichten verstuurd worden naar de volgende endpoints van de OpenZaakBrug. Een voorbeeld project valt te vinden in: examples\soap\Open-Zaakbrug-soapui-project.xml

### Bekijken berichten verkeer

Om het berichten verkeer te bekijken zijn er 2 mogelijkheden:

- Via de database, waarin al het berichtenverkeer wordt gelogd (kan worden) naar de tabellen, hierbij representeert het veld: referentienummer één vraag
  - - request_response_cycle : de vragen en antwoorden op de Open Zaakbrug service
    - zds_request_response_cycle : de vragen die gesteld worden aan het legacy zaaksysteem, met de bijbehorende antwoorden
    - zgw_request_response_cycle : de vragen die gesteld worden aan openzaak, met de bijbehorende antwoorden

- Via de ladybug omgeving, hiermee een bepaalde sequence van berichten kan worden opgenomen en later worden afgespeeld. Deze is bereikbaar via de url: http://localhost:8080/debug/ .  Meer informatie over ladybug valt te vinden op: https://frank-manual.readthedocs.io/en/latest/testing/ladybug/capture/capture.html

# Installatie met Spring Tool Suite
Onderstaand stappenplan kan gebruikt worden voor een schone installatie met Spring Tool Suite op Windows. De Spring Tool Suite bevat de juiste versie van Java en heeft ingebouwde support voor Git en Maven. 

1. Download de Spring Tool Suite (Spring Tools 4 for Eclipse) van https://spring.io/tools
2. Installeer de Spring Tool Suite door te dubbelklikken op de download
3. Download Lombok van https://projectlombok.org/download
4. Installeer Lombok door te dubbelklikken op de download
5. Selecteer de Spring Tool Suite in de Lombok-installer
6. Start de Spring Tool Suite
7. Clone en importeer de repo in de Spring Tool Suite
8. Kopieer/hernoem/bewerk de application.properties_example en config.json_example zoals hierboven beschreven
9. Doe een clean+build van de workspace
10. Doe een `Run As` -> `Maven build` met `clean install` als goals
11. Start de applicatie vanuit het `Boot Dashboard`
12. Open http://localhost:8080/debug
13. Indien alles werkt wordt nu de Ladybug getoond

## Troubleshooting Spring Tool Suite installatie
### Self extracting van de Spring Tool Suite installatie werkt niet
Als de self extracting installatie van de Spring Tool Suite niet werkt, kan de download ook met [7-Zip](https://www.7-zip.org/) geopend worden en de contents.zip die in de .jar zit kan uitgepakt worden op een locatie naar keuze. Winzip kan de contents.zip NIET uitpakken!p

## Lombok installer start niet
Als de Lombok-installer niet lijkt te starten, probeer deze dan te starten met `java.exe -jar <pad/naar/lombok.jar>`

## Lombok-installer kan Spring Tool Suite niet vinden
Als de Spring Tool Suite niet gevonden kan worden door de Lombok-installer, kan de juiste locatie opgegeven worden via "Specify location…".

## Foutmeldingen dat methodes niet gevonden kunnen worden
Als er foutmeldingen zijn dat methodes niet gevonden kunnen worden, is Lombok waarschijnlijk niet correct geïnstalleerd. Voer de installatie van Lombok opnieuw uit en herstart daarna de Spring Tool Suite. Voer een clean+build en een nieuwe Maven build uit zodra de Spring Tool Suite weer gestart is.

# rest hieronder moet gecontroleerd worden



### JWT enpoint

The project contains an enpoint that emits a valid JWT based on the secret provided in the properties file.
This can be used when using the ZGW API in tools like Postman. Only use this in test environments.
The enpoint is dissabled by default and can be enabled by setting:

```
nl.haarlem.translations.zdstozgw.enableJWTEntpoint = true
```

### Prerequisites

### Java

Java 11 or higher is needed to run the project. The project is tested with OpenJDK 11

### Lombok 

This project uses [Lombok](https://projectlombok.org/). No setup is required to run the application after building with Maven.
If compilation in an IDE is needed, setup may me applicable. 
Lombok plugins are available for all major Java IDE's ([examples](https://www.baeldung.com/lombok-ide))

### Installation ###

Clone the repo and build and run the service with Maven

```
$ git clone https://github.com/Haarlem/zds-stuf-to-zgw-api-translator/
$ cd zds-stuf-to-zgw-api-translator 
```

Use configuration from examples or provide local settings

```
$ mv src/main/resources/application.properties_example src/main/resources/application.properties
$ mv src/main/resources/config.json_example src/main/resources/config.json
```

Start the service

```
$ mvn spring-boot:run
```

The service is now available on port 8080 or the port provided in application.properties

#### Setup ZRC

This services depends on an implementation of [API standaard zaakgericht werken](https://www.vngrealisatie.nl/producten/api-standaarden-zaakgericht-werken)
The current version of this service is (partially) tested with [OpenZaak](https://github.com/open-zaak/open-zaak) as backend.

### Configuration

The application uses two configuration files, located in the resources folder. Examples are provided.

| File                   | Usage                                                  |
| ---------------------- | ------------------------------------------------------ |
| application.properties | Server settings like portnumbers and ZGW api url       |
| config.json            | Runtime configuration like zaaktypes, organisaties etc |

### Using the service

The service can receive and translate StUF ZDS 1.2 SOAP messages.
A list of supported operations and endpoints can be retrieved by accessing the url: http://localhost:8080/
The database can accessed by the following url: http://localhost:8080/h2-console/

Example SOAP messages are provided in the /examples/soap folder
