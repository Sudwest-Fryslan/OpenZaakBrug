# Bewaartermijnen en archiefactiedatum bepalen (Open Zaak)

Deze notitie beschrijft de werkwijze en de bronnen die zijn gebruikt om voor een zaaktype/resultaattype in
Open Zaak de juiste selectielijstklasse, afleidingswijze en (proces)termijn te bepalen. Aanleiding: het
resultaattype "Uitgereikt" van zaaktype "Aanvraag rijbewijs" (B0208, catalogus Gemeente Súdwest-Fryslân) op
`test.openzaak.nl` had afleidingswijze "Hoofdzaak" staan, terwijl dit zaaktype nooit als deelzaak/hoofdzaak
wordt gebruikt — vandaar de 400 `archiefactiedatum-error` bij het zetten van de eindstatus.

## Bronnen

- **Selectielijst gemeenten en intergemeentelijke organen 2020** (geldig sinds 1 januari 2020) — officiële
  informatiepagina bij het Nationaal Archief, incl. downloadlink naar de PDF:
  <https://www.nationaalarchief.nl/archiveren/kennisbank/selectielijst-gemeenten-en-intergemeentelijke-organen-2020>
  Directe PDF: <https://www.nationaalarchief.nl/sites/default/files/field-file/Selectielijst_20200214.pdf>
- **Formele vaststelling** in de Staatscourant ("Vaststelling Selectielijst gemeenten en intergemeentelijke
  organen 2020", Stcrt. 2020, 11143, ondertekend 18-02-2020, gepubliceerd 26-02-2020; de selectielijst 2017
  is per 31-12-2019 komen te vervallen):
  <https://zoek.officielebekendmakingen.nl/stcrt-2020-11143.html>
- **VNG-publicatie** van dezelfde selectielijst-PDF: <https://vng.nl/sites/default/files/2020-02/selectielijst_20200214.pdf>
- **Open Zaak — archiveringslogica** (hoe `archiefactiedatum` technisch wordt berekend per afleidingswijze):
  <https://open-zaak.readthedocs.io/en/stable/manual/archiving.html>

Check bij gebruik altijd of de Nationaal Archief-pagina de selectielijst nog als *actueel* vermeldt — dit kan
na 2020 zijn opgevolgd door een nieuwere versie.

## Rekenregel in Open Zaak

```
archiefactiedatum = startdatumBewaartermijn + archiefactietermijn
```

`startdatumBewaartermijn` wordt bepaald door de gekozen **afleidingswijze** op het resultaattype:

| Afleidingswijze | startdatumBewaartermijn |
|---|---|
| Afgehandeld | `Zaak.einddatum` |
| Termijn | `Zaak.einddatum + ResultaatType.procestermijn` |
| Hoofdzaak | einddatum van de **hoofdzaak** (vereist dat de zaak een deelzaak is!) |
| Eigenschap | waarde van de ZaakEigenschap die overeenkomt met het ingestelde datumkenmerk |
| Zaakobject / Ingangsdatum besluit / Vervaldatum besluit | datum uit het gekoppelde object/besluit |

Belangrijk: archiefparameters worden herberekend bij wijzigingen, **behalve** bij afleidingswijze
`ander_datumkenmerk` en `termijn` — en het verwijderen van een resultaat zet deze datums terug op `null`.

Open Zaak valideert bovendien dat de afleidingswijze **niet** "Afgehandeld" mag zijn als de gekozen
selectielijstklasse een `procestermijn` van het type `vast_te_leggen_datum` heeft (dat is precies de fout die
we tegenkwamen bij "Uitgereikt" / 5.1.6).

## Werkwijze: bewaartermijn per resultaattype bepalen

De bewaartermijn wordt **niet** één keer per zaaktype bepaald, maar per resultaattype — verschillende
resultaten van hetzelfde zaaktype (verleend/geweigerd/ingetrokken/buiten behandeling) kunnen een andere
selectielijstklasse en dus een andere termijn hebben.

1. **Bepaal het proces**, niet alleen de naam van het zaaktype: wat is de aanleiding, welke handeling voert de
   gemeente uit, wat is het procesobject, wat levert het proces op? Zoek in de selectielijst het procestype
   dat hier inhoudelijk het beste bij past (bijv. procestype 5 "Producten en diensten leveren").
2. **Bepaal het concrete resultaat** van het lokale resultaattype en vergelijk de betekenis (niet alleen de
   naam) met de omschrijving in de selectielijst, tot je het juiste resultaatnummer hebt
   (bijv. 5.1.6 — Geleverd — Rijbewijs).
3. **Lees de volledige selectielijstregel**: resultaatnummer, waardering (bewaren/vernietigen), procestermijn
   (en het type ervan — nihil / bestaansduur procesobject / vast te leggen datum / …), bewaartermijn,
   toelichting, herkomst.
4. **Bepaal de brondatum**: vanaf welk moment moet de bewaartermijn gaan lopen? Bijvoorbeeld de einddatum van
   de zaak, de einddatum + vaste procestermijn, de werkelijke vervaldatum van een verstrekt document/product,
   of een datum uit een zaakeigenschap.
5. **Kies de bijpassende afleidingswijze** in Open Zaak (zie tabel hierboven) en vul de bijbehorende velden in
   (Procestermijn bij "Termijn", Datumkenmerk bij "Eigenschap", enz.).
6. **Leg de onderbouwing vast**: zaaktype, resultaattype, selectielijstnummer + omschrijving, waardering,
   procestermijn(soort), afleidingswijze, archiefactietermijn, bron (URL + eventueel paginanummer), wie het
   heeft vastgesteld en wanneer.
7. **Test met een voorbeeldzaak**: reken vooraf de verwachte archiefactiedatum uit en controleer na het
   afsluiten van een testzaak of Open Zaak dezelfde datum berekent.
8. **Controleer uitzonderingen op vernietiging** (hotspots, bijzonder/precedentwerkend karakter, samenhang met
   blijvend te bewaren stukken) voordat dossiers na het verstrijken van de termijn daadwerkelijk vernietigd
   worden — deze uitzonderingen staan in de toelichting bij de officiële selectielijst.

## Voorbeeld: "Aanvraag rijbewijs" — resultaat "Uitgereikt"

Selectielijst 5.1.6 (pagina ±28 van 113 in de PDF; zoek op "5.1.6" of "Rijbewijs"):

| Veld | Waarde |
|---|---|
| Resultaatnummer | 5.1.6 — Geleverd — Rijbewijs |
| Herkomst | Risicoanalyse |
| Waardering | Vernietigen |
| Procestermijnsoort | Vast te leggen datum — "de tijdens het proces vast te leggen datum waarop de geldigheid van het procesobject vervalt" |
| Procestermijn | 5 of 10 jaar (= geldigheidsduur van het afgegeven rijbewijs) |
| Bewaartermijn (archiefactietermijn) | 1 jaar (P1Y) |

Omdat de procestermijn hier "vast te leggen datum" is (de échte geldigheidsduur van het concrete rijbewijs,
niet een vaste waarde), is de zuiverste inrichting eigenlijk om de werkelijke vervaldatum van het rijbewijs
als zaakeigenschap vast te leggen en afleidingswijze "Eigenschap" te gebruiken. Wanneer dat (nog) niet
beschikbaar is, is een pragmatische tussenoplossing afleidingswijze "Termijn" met een vaste procestermijn van
P5Y of P10Y (afhankelijk van de geldigheidsduur die dit rijbewijzenproces hanteert), gecombineerd met
archiefactietermijn P1Y — resulterend in vernietiging 6 respectievelijk 11 jaar na de einddatum van de zaak.
Gemeenten gebruiken hiervoor in de praktijk vaak twee aparte lokale resultaattypen (één voor de 6-jaar- en één
voor de 11-jaar-variant), omdat Open Zaak per resultaattype maar één vaste procestermijn ondersteunt.

## Checklist bij het inrichten van een resultaattype

| Onderdeel | Vast te leggen |
|---|---|
| Zaaktype / resultaattype | naam + identificatie |
| Selectielijstresultaat | nummer + volledige omschrijving |
| Waardering | bewaren / vernietigen |
| Procestermijnsoort | nihil / vast te leggen datum / bestaansduur procesobject / … |
| Procestermijn | indien van toepassing |
| Afleidingswijze | afgehandeld / termijn / eigenschap / hoofdzaak / … |
| Datumkenmerk | indien van toepassing (bij "Eigenschap") |
| Archiefactietermijn | bijv. P1Y |
| Onderbouwing | waarom deze selectielijstklasse past |
| Bron | URL (+ paginanummer) |
| Vastgesteld door | functioneel beheer / informatiebeheer |
| Datum controle | wanneer de inrichting is gecontroleerd |
