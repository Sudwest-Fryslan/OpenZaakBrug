# Zaaktypen aanmaken/wijzigen in de Open Zaak admin — praktische valkuilen

Deze notitie verzamelt gotcha's die zijn tegengekomen bij het (opnieuw) opbouwen van een zaaktype in de
Open Zaak admin (`/admin/catalogi/...`), naar aanleiding van het handmatig nabouwen van zaaktype B1291
("Bezwaarschrift gemeentelijke belastingen") in de Gemeente Súdwest-Fryslân-catalogus op `test.openzaak.nl`.
Zie ook [[bewaartermijnen-bepalen]] voor de inhoudelijke kant (selectielijst/archivering) van resultaattypen.

## 1. Geldigheidsdatums bij een nieuwe versie: "einde" is exclusief

Bij "Nieuwe versie toevoegen" op een gepubliceerd zaaktype moet je vóór het (opnieuw) publiceren de
`Datum einde geldigheid` van de **oude** versie zetten — anders weigert Open Zaak te publiceren met:

> "Zaaktype versies (dezelfde omschrijving) mogen geen overlappende geldigheid hebben."

Cruciaal: `Datum einde geldigheid` werkt **exclusief** — de versie is **niet meer geldig vanaf 00:00 op die
datum**, niet meer geldig **op** die datum. Zet de oude versie se einddatum dus **gelijk aan** de begindatum
van de nieuwe versie (niet één dag ervoor!), anders ontstaat er een gat van precies één dag waarop géén enkele
versie geldig is:

```
Oude versie:  begin=10-11-2022   einde=01-08-2026   → geldig t/m 31-07-2026
Nieuwe versie: begin=01-08-2026   (geen einde)        → geldig vanaf 01-08-2026
```

Dit gat leidde in de praktijk tot: `nl.haarlem...ConverterException: Zaaktype met code:B1291 could not be
found`, met in de OpenZaakBrug-log de duidelijke aanwijzingen:

```
[processing warning] zaaktype met identificatie: 'B1291' heeft een versie die nog moet beginnen: ...
[processing warning] zaaktype met identificatie: 'B1291' heeft een versie die al beeindigd is: ...
```

Zodra beide meldingen tegelijk verschijnen voor dezelfde identificatie, is dit vrijwel zeker de oorzaak: zoek
de datums van de omliggende versies op en dicht het gat.

### Browser/server-tijdzoneverschil maakt dit extra verraderlijk

De admin toont een waarschuwing "Let op: u ligt 2 uur voor ten opzichte van de servertijd." De "Vandaag"-knop
en de automatisch ingevulde versiedatum bij "Nieuwe versie toevoegen" kunnen daardoor, afhankelijk van het
tijdstip, een dag vóór of ná de kalenderdatum uitkomen die je zelf verwacht. Vertrouw daarom niet blind op de
automatisch ingevulde datum — lees na het aanmaken van een nieuwe versie de daadwerkelijke `Datum begin
geldigheid` af en stem de oude versie se `Datum einde geldigheid` daar expliciet op af, in plaats van zelf een
datum te "berekenen".

## 2. Resultaattype toevoegen aan een **nieuw** zaaktype: procestype-filter werkt pas na opslaan

De losse pagina `/admin/catalogi/resultaattype/add/?zaaktype=<id>` (of de "Voeg een Resultaattype toe"-link
vanaf de zaaktype-pagina) bepaalt de getoonde `Selectielijstklasse`-radio-opties aan de hand van het
`selectielijst_procestype` van het zaaktype. Bij het aanmaken van een **gloednieuw** zaaktype blijkt dit **niet**
correct te resolven via de `?zaaktype=`-querystring-prefill — het formulier valt terug op procestype 1
("Instellen en inrichten organisatie"), ook als het zaaktype zelf een ander procestype heeft (bijv. 13,
"Geschillen behandelen"). Gevolg: de selectielijstklasse die je eigenlijk nodig hebt, staat niet in de lijst en
opslaan geeft `Selecteer een geldige keuze. ... is geen beschikbare keuze.`

**Oplossing (2 stappen):**
1. Maak het resultaattype eerst aan met een willekeurige, wél beschikbare selectielijstklasse (bijv. de eerste
   in de lijst) — puur om de rest van het formulier (omschrijving, archiefnominatie, archiefactietermijn,
   afleidingswijze) correct op te slaan.
2. Open het zojuist opgeslagen resultaattype opnieuw via zijn eigen wijzig-pagina (`/resultaattype/<id>/change/`).
   Nu wél een **bestaand, aan de database gebonden** object, resolvt het formulier het procestype van het
   zaaktype correct en toont het de juiste selectielijstklasse-opties. Corrigeer daar de selectielijstklasse.

Dit is dezelfde reden waarom losstaand aanmaken van resultaattypen op een compleet nieuw zaaktype nooit in één
stap lukt — reken op twee bewerkrondes per resultaattype als het zaaktype zelf ook nieuw is.

## 3. `selectielijst_procestype` wijzigen op het zaaktype zelf: wacht op de AJAX-call

Het wijzigen van `Selectielijst procestype jaar` ververst via AJAX de opties van de `Selectielijst procestype`-
dropdown. Als je dit script-matig doet (bijv. met een browser-automatiseringstool), en je zet direct daarna ook
`Selectielijst procestype`, wordt die tweede waarde overschreven zodra de AJAX-respons binnenkomt en de opties
herlaadt — het formulier valt dan terug op de eerste optie in de nieuwe lijst. Bouw een korte wachttijd
(1–2 seconden) in tussen het zetten van het jaar en het zetten van het procestype zelf, en verifieer na afloop
dat de waarde daadwerkelijk is blijven staan vóórdat je opslaat.

## 4. Statustypevolgnummer: hoogste nummer = eindstatus, dus reken vooruit bij invoegen

`isEindstatus` wordt door Open Zaak afgeleid uit **het hoogste `statustypevolgnummer`** van het zaaktype — er is
geen apart veld om dit expliciet te zetten. Wil je een statustype vóóraan invoegen (bijv. een ontbrekende
"Ontvangen" als allereerste stap), dan moet je **alle bestaande volgnummers met 1 ophogen** voordat je het
nieuwe statustype als volgnummer 1 toevoegt — anders ontstaat een dubbel volgnummer. Werk daarbij van **hoog
naar laag** (begin bij het hoogste bestaande volgnummer), zodat je nooit tijdelijk twee statustypen met hetzelfde
nummer hebt.

## 5. Publiceren met nieuwe, nog-concept documenttypen: vink "Auto-publish related objects" aan

Als een zaaktype verwijst naar informatieobjecttypen die zelf nog CONCEPT zijn, laat de publiceer-pagina die
expliciet zien onder "Informatieobjecttypen". Vink daar **"Auto-publish related objects"** aan vóór het
bevestigen — anders blijft het zaaktype verwijzen naar niet-gepubliceerde documenttypen.

## 6. Een fix (nieuw statustype, nieuwe documentkoppeling, ...) geldt alleen voor de versie die je aanpast

Zodra er, door het patchen tijdens het testen, meerdere opeenvolgende versies met verschillende ingangsdatums
naast elkaar bestaan (bijv. één die "vandaag" dekt en één die "morgen" ingaat), geldt een fix die je op de
huidige/actieve versie toepast **niet automatisch** voor de latere versie(s) — die zijn al eerder als losse
kopie aangemaakt en missen de latere toevoeging. Regressietests die over meerdere dagen doorlopen, lopen dan
op een volgende dag alsnog tegen exact hetzelfde probleem aan.

**Vuistregel:** als je tijdens het testen een structurele fix doorvoert (statustype, resultaattype, roltype,
documentkoppeling), controleer of er nóg een gepubliceerde versie met een latere ingangsdatum bestaat, en
voer dezelfde fix daar ook door (via weer een "Nieuwe versie toevoegen" op díe versie) — anders herhaalt het
probleem zich zodra die latere versie actief wordt.
