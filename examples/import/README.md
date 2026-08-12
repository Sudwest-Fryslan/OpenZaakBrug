# Zaaksysteem

Dit script is bedoeld om zaak-informatie van een locatie te importeren in een zaaksysteem. 
Van een locatie wordt de betreffende informatie geladen, waarna deze via zgw wordt aangeboden aan het zaaksysteem.

Dit script kan men aanroepen met de volgende parameters:

> `python ZgwImportData.py https://github.com/Sudwest-Fryslan/OpenZaakBrug/raw/master/examples/openzaak-export-catalogus-zaaktypes.zip`

In het bestand Config.py staat de configuraties van de locatie van het zaaksysteem, met de credentials

`examples/openzaak-export-catalogus-zaaktypes.zip` is een catalogus-export (via Open Zaak admin →
catalogus → "Exporteren") van de zaaktypen-catalogus op `test.openzaak.nl` die voor de geautomatiseerde
regressietests (`examples/soap/functional-tests-soapui-project.xml` en `performance-tests-soapui-project.xml`)
wordt gebruikt. Alle zaaktypen hierin zijn dus daadwerkelijk tegen die testen geverifieerd — dit bestand
opnieuw exporteren en verversen nadat een zaaktype op `test.openzaak.nl` is aangepast/toegevoegd voor de
testen, zodat dit bestand een nieuwe omgeving in één keer kan voorzien van exact dezelfde, geteste zaaktypen.

### 0.1:
- Initiele versie

Source: https://git.sudwestfryslan.nl:1900/Monitoring_Scripts/Nagios_Zaaksysteem
