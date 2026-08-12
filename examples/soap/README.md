# SoapUI-testprojecten

Deze map bevat de SoapUI-projecten waarmee je OpenZaakBrug handmatig kunt bevragen:

- `functional-tests-soapui-project.xml` — functionele testketens (zaak/document aanmaken, updaten, status
  wijzigen, etc.) tegen een draaiende OpenZaakBrug.
- `performance-tests-soapui-project.xml` — dezelfde soort scenario's, opgezet voor load-/performancetesten.
- `Haarlem Voorbeeld/`, `Swf Suites4SociaalDomein/`, `openzaak-export-catalogus-zaaktypes/` — losse
  voorbeeldberichten en dumps, niet volledige SoapUI-projecten.

Beide projectbestanden gebruiken de project-property `OpenZaakbugServer` (Project → Custom Properties) als
basis-URL voor alle endpoints — pas die aan als je tegen een andere OpenZaakBrug-instantie dan
`http://localhost:8080` wilt testen.

## Schema Compliance-assertie geeft NullPointerException

Een aantal teststappen heeft een "Schema Compliance"-assertie, die de respons tegen de officiële WSDL/XSD
valideert. Op een andere machine dan die van de originele auteur geeft dit:

```
com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion
java.lang.NullPointerException: Cannot invoke "...InterfaceDefinitionPart.getUrl()" because the return value
of "...DefinitionCache.getRootPart()" is null
```

Oorzaak: de betreffende interfaces (`ZdsBeantwoordVraag`, `ZdsOntvangAsynchroon`, `ZdsVrijeBerichten`,
`StufZknBeantwoordVraag`) wijzen naar een absoluut pad op de machine van de originele auteur
(`file:/D:/git/zds-stuf-to-zgw-api-translator/...`), dat op elke andere machine niet bestaat.

### Belangrijk: dit zijn 2 verschillende standaarden, niet 1

- `ZdsBeantwoordVraag`, `ZdsOntvangAsynchroon`, `ZdsVrijeBerichten` gebruiken het **ZDS-specifieke
  `zs-dms`-koppelvlak** — een smalle set operaties, alleen wat ZDS zelf nodig heeft.
- `StufZknBeantwoordVraag` gebruikt de **volledige, generieke StUF-ZKN 0310 sectormodel-WSDL**
  (`vraagAntwoord`-koppelvlak) — met veel meer operaties (bv. `sttLv07`, `bslLv01`, `zakLv05`, etc. — 122
  operaties in totaal, voor alle StUF-ZKN-entiteitstypes: besluit, contactpersoon, medewerker, status,
  statustype, verzoek, zaak, zaaktype...). Dit is een **apart gepubliceerde standaard**, los van ZDS.

Als je per ongeluk de `zs-dms`-WSDL aan `StufZknBeantwoordVraag` koppelt (of andersom), lijken opeens operaties
te ontbreken die er eerder wel waren — dat is het symptoom hiervan, geen echte regressie.

### Oplossing: officiële schema's downloaden en de interface-definitie bijwerken

De map `wsdl-schema/` (naast dit bestand) wordt **niet** in git bijgehouden — download de bundels hieronder
opnieuw wanneer je ze nodig hebt (nieuwe machine, na een `git clone`, etc.).

**Officiële bronnen:**
- ZDS (Zaak- en Documentservices): <https://standaarden.vng.nl/Zaken-en-documenten> →
  <https://vng-realisatie.github.io/Zaak-en-Documentservices/Documentatie>
- StUF-ZKN 0310 (sectormodel Zaken, los van ZDS): <https://vng-realisatie.github.io/StUF-ZKN/> →
  <https://vng-realisatie.github.io/StUF-ZKN/Documentatie>

**Downloaden en uitpakken:**

```bash
DEST="examples/soap/wsdl-schema"
mkdir -p "$DEST/ZDS-1.2" "$DEST/ZDS-1.1" "$DEST/StUF-ZKN-0310"

curl -L -o "$DEST/Zaak-_Documentservices_1_2.zip" \
  "https://vng-realisatie.github.io/Zaak-en-Documentservices/documenten/Zaak-_Documentservices_1_2.zip"
curl -L -o "$DEST/Zaak_DocumentServices_1_1_02.zip" \
  "https://vng-realisatie.github.io/Zaak-en-Documentservices/documenten/Zaak_DocumentServices_1_1_02.zip"
# check de Documentatie-pagina hierboven voor de actuele patch-bestandsnaam (verandert per release):
curl -L -o "$DEST/zkn0310_stuf-zkn-patch.zip" \
  "https://vng-realisatie.github.io/StUF-ZKN/documenten/<huidige-patch-bestandsnaam>.zip"

(cd "$DEST/ZDS-1.2" && unzip -o "../Zaak-_Documentservices_1_2.zip")
(cd "$DEST/ZDS-1.1" && unzip -o "../Zaak_DocumentServices_1_1_02.zip")
(cd "$DEST/StUF-ZKN-0310" && unzip -o "../zkn0310_stuf-zkn-patch.zip")
```

**Welke bundel/bestand hoort bij welke interface:**

| SoapUI-interface         | Bron-bundel                     | Bestand (na uitpakken)                                                                        |
|----------------------------|----------------------------------|--------------------------------------------------------------------------------------------------|
| `ZdsBeantwoordVraag`       | ZDS 1.1.02                       | `ZDS-1.1/Zaak_DocumentServices_1_1_02/zkn0310/zs-dms/zkn0310_beantwoordVraag_zs-dms.wsdl`        |
| `ZdsOntvangAsynchroon`     | ZDS 1.1.02                       | `ZDS-1.1/Zaak_DocumentServices_1_1_02/zkn0310/zs-dms/zkn0310_ontvangAsynchroon_mutatie_zs-dms.wsdl` |
| `ZdsVrijeBerichten`        | ZDS 1.1.02                       | `ZDS-1.1/Zaak_DocumentServices_1_1_02/zkn0310/zs-dms/zkn0310_vrijeBerichten_zs-dms.wsdl`         |
| `StufZknBeantwoordVraag`   | StUF-ZKN 0310 (actuele patch)     | `StUF-ZKN-0310/zkn0310/vraagAntwoord/zkn0310_beantwoordVraag.wsdl`                               |

De originele, kapotte paden gebruikten allemaal de `zkn0310/zs-dms/`-koppelvlaknaming (die hoort specifiek bij
**ZDS 1.1.02** — in ZDS 1.2 is dit hernoemd naar `mutatie`/`vraagAntwoord` zonder de `zs-dms`-toevoeging). De
bestandsnamen in de ZDS 1.1.02-bundel komen **exact** overeen met wat de eerste 3 interfaces verwachten. Voor
`StufZknBeantwoordVraag` is de StUF-ZKN 0310-bundel nodig — de patch-versie op de site verandert regelmatig
(op moment van schrijven patch 34), dus check de Documentatie-pagina voor de huidige bestandsnaam.

De relatieve mapstructuur binnen elke uitgepakte bundel (`0301/`, `bg0310/`, `zkn0310/`, `gml-3.1.1.2/`, etc.
als broertjes van elkaar) is bewust ongewijzigd gelaten, omdat de WSDL's onderling naar elkaar verwijzen via
relatieve paden (bijv. `../../0301/stuf0301_types.wsdl`) — verplaats uitgepakte bestanden dus niet individueel.

De **ZDS 1.2**-bundel wordt ook gedownload omdat die op de officiële pagina naast 1.1 staat en relevant kan zijn
voor ander werk aan deze koppeling (dit project vertaalt uiteindelijk naar ZDS 1.2, zie `pom.xml`), maar is voor
déze specifieke schema-compliance-fix niet nodig.

**In SoapUI:** rechtermuisklik op elke interface in de projectboom (niet op een teststap) → **"Update
Definition..."** → wijs naar het bijbehorende bestand uit de tabel hierboven. Dit moet voor alle 4 de
interfaces gedaan worden, in **beide** SoapUI-projectbestanden.

### Corrupte whitespace in de projectbestanden (opgelost, maar let op bij toekomstige edits)

Beide projectbestanden zijn ooit door een pretty-printer/formatter gehaald, die op een aantal plekken een
regeleinde + inspringing midden in een tekstwaarde heeft gezet (bijv. `<con:sourceStep>naam\n\t\t- 2</con:sourceStep>`
in plaats van `<con:sourceStep>naam - 2</con:sourceStep>`, en hetzelfde bij een aantal `<con:endpoint>`-URL's).
SoapUI kan zo'n waarde dan niet meer matchen ("Resolve source property" faalt, of de endpoint-URL werkt niet).
Dit is voor `performance-tests-soapui-project.xml` gefixt (22 endpoints + 2 sourceSteps);
`functional-tests-soapui-project.xml` bleek al schoon.

Let op als je dit soort dingen zelf (opnieuw) moet fixen: **`git diff --stat` op deze bestanden is niet
betrouwbaar** — ze bestaan uit een paar extreem lange regels (honderden KB per regel), waardoor git's
regel-gebaseerde diff een piepklein tekstuele wijziging toont als "de hele regel vervangen" (tienduizenden
"gewijzigde" regels voor een paar bytes aan echte wijziging). Verifieer een wijziging daarom op bestandsgrootte
(`wc -c`) of met een gerichte Python-check op de specifieke tekstwaarde, niet op `git diff --stat`. Gebruik
geen brede pretty-printer/herformatter op deze bestanden — dat is waarschijnlijk hoe de corruptie hierboven is
ontstaan.
