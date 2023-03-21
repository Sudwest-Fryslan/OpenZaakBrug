/*
 * Copyright 2020-2021 The Open Zaakbrug Contributors
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the
 * European Commission - subsequent versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package nl.haarlem.translations.zdstozgw.translation.zds.services;

import java.lang.invoke.MethodHandles;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

import nl.haarlem.translations.zdstozgw.translation.zds.model.*;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.*;

import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.haarlem.translations.zdstozgw.config.ConfigService;
import nl.haarlem.translations.zdstozgw.config.ModelMapperConfig;
import nl.haarlem.translations.zdstozgw.config.model.Organisatie;
import nl.haarlem.translations.zdstozgw.config.model.ZgwRolOmschrijving;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.debug.Debugger;
import nl.haarlem.translations.zdstozgw.translation.BetrokkeneType;
import nl.haarlem.translations.zdstozgw.translation.zgw.client.ZGWClient;
import nl.haarlem.translations.zdstozgw.utils.ChangeDetector;
import nl.haarlem.translations.zdstozgw.utils.ChangeDetector.Change;

@Service
public class ZaakService {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Debugger debug = Debugger.getDebugger(MethodHandles.lookup().lookupClass());

    public final ZGWClient zgwClient;

    private final ModelMapper modelMapper;
    public final ConfigService configService;

    @Autowired
    public ZaakService(ZGWClient zgwClient, ModelMapper modelMapper, ConfigService configService) {
        this.zgwClient = zgwClient;
        this.modelMapper = modelMapper;
        this.configService = configService;
    }

    public String getRSIN(String gemeenteCode) {
        List<Organisatie> organisaties = this.configService.getConfiguration().getOrganisaties();
        for (Organisatie organisatie : organisaties) {
            if (organisatie.getGemeenteCode().equals(gemeenteCode)) {
                return organisatie.getRSIN();
            }
        }
        return "";
    }

    public ZgwZaak creeerZaak(String rsin, ZdsZaak zdsZaak) {
        zgwClient.caseCreationStatusOk = true;
        log.debug("creeerZaak:" + zdsZaak.identificatie);
        ZgwZaak zgwZaak = this.modelMapper.map(zdsZaak, ZgwZaak.class);

        var zaaktypecode = zdsZaak.isVan.gerelateerde.code;
        var zgwZaakType = this.zgwClient.getZgwZaakTypeByIdentificatie(zaaktypecode);
        if (zgwZaakType == null) {
            throw new ConverterException("Zaaktype met code:" + zaaktypecode + " could not be found");
        }
        zgwZaak.zaaktype = zgwZaakType.url;
        zgwZaak.bronorganisatie = rsin;
        zgwZaak.verantwoordelijkeOrganisatie = rsin;

        if (zdsZaak.getKenmerk() != null && !zdsZaak.getKenmerk().isEmpty()) {
            zgwZaak.kenmerk = new ArrayList<>();
            // TODO: controleren of werkt
            for (ZdsKenmerk kenmerk : zdsZaak.getKenmerk()) {
                zgwZaak.kenmerk.add(this.modelMapper.map(kenmerk, ZgwKenmerk.class));
            }
        }

        // alleen een verlenging meenemen als er echt waarden in staan
        if (zgwZaak.verlenging != null && (zgwZaak.verlenging.reden == null || zgwZaak.verlenging.reden.length() == 0)) {
            zgwZaak.verlenging = null;
        }

        zgwZaak = this.zgwClient.addZaak(zgwZaak);
        log.debug("Created a ZGW Zaak with UUID: " + zgwZaak.getUuid());

        // rollen
        ZgwRolOmschrijving zgwRolOmschrijving = this.configService.getConfiguration().getZgwRolOmschrijving();
        addRolToZgw(zgwZaak, zgwZaakType, zdsZaak.heeftBetrekkingOp, zgwRolOmschrijving.getHeeftBetrekkingOp());
        addRolToZgw(zgwZaak, zgwZaakType, zdsZaak.heeftAlsBelanghebbende, zgwRolOmschrijving.getHeeftAlsBelanghebbende());
        addRolToZgw(zgwZaak, zgwZaakType, zdsZaak.heeftAlsInitiator, zgwRolOmschrijving.getHeeftAlsInitiator());
        addRolToZgw(zgwZaak, zgwZaakType, zdsZaak.heeftAlsUitvoerende, zgwRolOmschrijving.getHeeftAlsUitvoerende());
        addRolToZgw(zgwZaak, zgwZaakType, zdsZaak.heeftAlsVerantwoordelijke, zgwRolOmschrijving.getHeeftAlsVerantwoordelijke());
        addRolToZgw(zgwZaak, zgwZaakType, zdsZaak.heeftAlsGemachtigde, zgwRolOmschrijving.getHeeftAlsGemachtigde());
        addRolToZgw(zgwZaak, zgwZaakType, zdsZaak.heeftAlsOverigBetrokkene, zgwRolOmschrijving.getHeeftAlsOverigBetrokkene());

        setResultaatAndStatus(zdsZaak, zgwZaak, zgwZaakType);

        if (!zgwClient.caseCreationStatusOk) {
            //Not all roltype and status data could be added to the new zgw Zaak, thus the zaak is deleted before replying with a SOAP fault
            zgwClient.deleteZaak(zgwZaak.getUuid());
            debugWarning("Niet alle rollen en/of statussen konden worden toegevoegd. Zaak: " + zgwZaak.getIdentificatie() + " is niet aangemaakt.");
            throw new ConverterException("Niet alle rollen en/of statussen konden worden toegevoegd. Zaak: " + zgwZaak.getIdentificatie() + " is niet aangemaakt.");
        }
        return zgwZaak;
    }

    public void updateZaak(ZdsZaak zdsWasZaak, ZdsZaak zdsWordtZaak) {
        log.debug("updateZaak:" + zdsWordtZaak.identificatie);
        ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(zdsWordtZaak.identificatie);
        if (zgwZaak == null) {
            throw new RuntimeException("Zaak with identification " + zdsWordtZaak.identificatie + " not found in ZGW");
        }
        ZgwZaakType zgwZaakType = this.zgwClient.getZaakTypeByZaak(zgwZaak);

        var changed = false;
        ChangeDetector changeDetector = new ChangeDetector();

        // check if the zdsWasZaak is equal to the one stored inside OpenZaak
        // this should be the case
        ZdsZaak zdsStored = this.modelMapper.map(zgwZaak, ZdsZaak.class);
        if (zdsWasZaak != null) {
            var storedVsWasChanges = changeDetector.detect(zdsStored, zdsWasZaak);
            var storedVsWasFieldsChanges = storedVsWasChanges.getAllChangesByDeclaringClassAndFilter(ZdsZaak.class, ZdsRol.class);
            if (storedVsWasFieldsChanges.size() > 0) {
                log.debug("Update of zaakid:" + zdsWasZaak.identificatie + " has # " + storedVsWasFieldsChanges.size() + " field changes between stored and was");
                for (Change change : storedVsWasFieldsChanges.keySet()) {
                    debugWarning("The field: " + change.getField().getName() + " does not match (" + change.getChangeType() + ") stored-value:'" + change.getCurrentValue() + "' , was-value:'" + change.getNewValue() + "'");
                }
                // ZgwZaakPut zgwWordtZaak = this.modelMapper.map(zdsWordtZaak, ZgwZaakPut.class);
                // ZgwZaakPut updatedZaak = ZgwZaakPut.merge(zgwZaak, zgwWordtZaak);
                // this.zgwClient.updateZaak(zgwZaak.uuid, updatedZaak);
                // changed = true;
            }

        } else {
            // when there was no "was" provided
            zdsWasZaak = zdsStored;
        }

        // attributen
        var wasVsWordtChanges = changeDetector.detect(zdsWasZaak, zdsWordtZaak);
        var wasVsWordtFieldChanges = wasVsWordtChanges.getAllChangesByDeclaringClassAndFilter(ZdsZaak.class, ZdsRol.class);
        if (wasVsWordtFieldChanges.size() > 0) {
            log.debug("Update of zaakid:" + zdsWasZaak.identificatie + " has # " + wasVsWordtFieldChanges.size() + " field changes");
            for (Change change : wasVsWordtFieldChanges.keySet()) {
                log.debug("\tchange:" + change.getField().getName());
            }
            ZgwZaakPut zgwWordtZaak = this.modelMapper.map(zdsWordtZaak, ZgwZaakPut.class);
            ZgwZaakPut updatedZaak = ZgwZaakPut.merge(zgwZaak, zgwWordtZaak);
            this.zgwClient.updateZaak(zgwZaak.uuid, updatedZaak);

            changed = true;
        }

        // rollen
        var wasVsWordtRolChanges = wasVsWordtChanges.getAllChangesByFieldType(ZdsRol.class);
        if (wasVsWordtRolChanges.size() > 0) {
            log.debug("Update of zaakid:" + zdsWasZaak.identificatie + " has # " + wasVsWordtRolChanges.size() + " rol changes:");

            changeDetector.filterChangesByType(wasVsWordtRolChanges, ChangeDetector.ChangeType.NEW)
                .forEach((change, changeType) -> {
                    var rolnaam = getRolOmschrijvingGeneriekByRolName(change.getField().getName());
                    log.debug("[CHANGE ROL] New Rol:" + rolnaam);
                    if (change.getField().getName().equals("heeftBetrekkingOp")) {
                        log.debug("ADD ZAAKOBKJECT");
                        addOrUpdateZaakObjectToZgw(zgwZaak, (ZdsRol) change.getNewValue());
                    } else {
                        addRolToZgw(zgwZaak, zgwZaakType, (ZdsRol) change.getNewValue(), rolnaam);
                    }
                });

            changeDetector.filterChangesByType(wasVsWordtRolChanges, ChangeDetector.ChangeType.DELETED)
                .forEach((change, changeType) -> {
                    var rolnaam = getRolOmschrijvingGeneriekByRolName(change.getField().getName());
                    if (rolnaam != null) {
                        log.debug("[CHANGE ROL] Deleted Rol:" + rolnaam);
                        if (change.getField().getName().equals("heeftBetrekkingOp")) {
                            log.debug("DELETE ZAAKOBKJECT");
                        } else {
                            deleteRolFromZgw(zgwZaak, zgwZaakType, rolnaam);
                        }
                    }
                });

            changeDetector.filterChangesByType(wasVsWordtRolChanges, ChangeDetector.ChangeType.CHANGED)
                .forEach((change, changeType) -> {
                    var rolnaam = getRolOmschrijvingGeneriekByRolName(change.getField().getName());
                    log.debug("[CHANGE ROL] Update Rol: " + rolnaam);
                    if (change.getField().getName().equals("heeftBetrekkingOp")) {
                        log.debug("UPDATE ZAAKOBJECT");
                        addOrUpdateZaakObjectToZgw(zgwZaak, (ZdsRol) change.getNewValue());
                    } else {
                        updateRolInZgw(zgwZaak, zgwZaakType, rolnaam, (ZdsRol) change.getNewValue());
                    }

                });
            changed = true;
        }

        boolean hasChanged = setResultaatAndStatus(zdsWordtZaak, zgwZaak, zgwZaakType);

        if (!changed && !hasChanged) {
            debugWarning("Update of zaakid:" + zdsWasZaak.identificatie + " without any changes");
        }
    }

    private boolean setResultaatAndStatus(ZdsZaak zdsZaak, ZgwZaak zgwZaak, ZgwZaakType zgwZaakType) {
        var changed = false;

        if (zdsZaak.resultaat != null && zdsZaak.resultaat.omschrijving != null && zdsZaak.resultaat.omschrijving.length() > 0) {
            var resultaatomschrijving = zdsZaak.resultaat.omschrijving;
            log.debug("Update of zaakid:" + zdsZaak.identificatie + " wants resultaat to be changed to:" + resultaatomschrijving);
            var zgwResultaatType = this.zgwClient.getResultaatTypeByZaakTypeAndOmschrijving(zgwZaakType, resultaatomschrijving);
            var resultaten = this.zgwClient.getResultatenByZaakUrl(zgwZaak.url);

            // remove any existing resultaten (we only want to have 1)
            for (ZgwResultaat resultaat : resultaten) {
                debugWarning("Zaak with identificatie:" + zdsZaak.identificatie + " already has resultaat #" + resultaten.indexOf(resultaat) + " met toelichting:" + resultaat.toelichting + "(" + resultaat.uuid + "), will be deleted");
                this.zgwClient.deleteZaakResultaat(resultaat.uuid);
            }
            ZgwResultaat zgwResultaat = new ZgwResultaat();
            zgwResultaat.zaak = zgwZaak.url;
            zgwResultaat.resultaattype = zgwResultaatType.url;
            zgwResultaat.toelichting = zdsZaak.resultaat.omschrijving;
            this.zgwClient.addZaakResultaat(zgwResultaat);
        }

        // if there is a status
        if (zdsZaak.heeft != null) {
            for (ZdsHeeft zdsHeeftIterator : zdsZaak.heeft) {
                ZdsGerelateerde zdsStatus = zdsHeeftIterator.gerelateerde;
                if (zdsStatus != null && zdsStatus.omschrijving != null && zdsStatus.omschrijving.length() > 0) {
                    log.debug("Update of zaakid:" + zdsZaak.identificatie + " wants status to be changed to:" + zdsStatus.omschrijving);
                    ZgwStatusType zgwStatusType = this.zgwClient.getStatusTypeByZaakTypeAndOmschrijving(zgwZaakType, zdsStatus.omschrijving, zdsStatus.volgnummer);
                    ZgwStatus zgwStatus = this.modelMapper.map(zdsHeeftIterator, ZgwStatus.class);
                    zgwStatus.zaak = zgwZaak.url;
                    zgwStatus.statustype = zgwStatusType.url;
                    zgwStatus.statustoelichting = zgwStatusType.omschrijving;

                    String zdsStatusDatum = zdsHeeftIterator.getDatumStatusGezet();
                    if ("true".equals(zgwStatusType.getIsEindstatus())) {
                        // Difference between ZDS --> ZGW the behaviour of ending a zaak has changed.
                        // (more info at: https://vng-realisatie.github.io/gemma-zaken/standaard/zaken/index#zrc-007 )
                        //
                        // in ZDS:
                        //	- object/einddatum contained the einddatum
                        //	- object/resultaat/omgeschrijving contained the resultaat-omschrijving
                        //
                        // in ZGW:
                        //	- resultaat an reference and status has to be set to the one with the highest volgnummer
                        zdsStatusDatum = zdsZaak.einddatum;
                    }


                    var formatter = new SimpleDateFormat("yyyyMMdd00000000");
                    var dagstart = formatter.format(new Date());
                    formatter = new SimpleDateFormat("yyyyMMddHHmmssSS");
                    if (zdsStatusDatum == null || zdsStatusDatum.length() == 0) {
                        debugWarning("no statusdatetime provided, using now()");
                        zdsStatusDatum = formatter.format(new Date());
                    } else if (zdsStatusDatum.length() < 16) {
                        // maken it length of 16
                        zdsStatusDatum = zdsStatusDatum + StringUtils.repeat("0", 16 - zdsStatusDatum.length());
                    }

                    if (dagstart.startsWith(zdsStatusDatum)) {
                        debugWarning("statusdatetime contains no time, using now() (DatumGezet, has to be unique)");
                        zdsStatusDatum = formatter.format(new Date());
                    } else if (zdsStatusDatum.endsWith("00000")) {
                        // TODO:
                        //		research if de dont want to make the zaaktype/datetime combination always unique
                        //		and maybe we want to have the client call on a higher lexical level
                        //
                        // The combination of zaak-uuid with datetime should be unique...
                        // when we still have a datetime
                        int index = this.zgwClient.getStatussenByZaakUrl(zgwZaak.url).size();
                        String ending = Integer.toString(index) + "00";
                        zdsStatusDatum = zdsStatusDatum.substring(0, zdsStatusDatum.length() - ending.length()) + ending;
                    }
                    var zgwStatusDatumTijd = (ModelMapperConfig.convertStufDateTimeToZgwDateTime(zdsStatusDatum));
                    zgwStatus.setDatumStatusGezet(zgwStatusDatumTijd);
                    this.zgwClient.addZaakStatus(zgwStatus);
                    changed = true;
                } else {
                    debugWarning("status has 'heeft' without 'gerelateerde' or  omschrijving");
                }
            }
        }
        return changed;
    }

    private void addZaakObjectToZGW(ZgwZaak zgwZaak, ZdsRol zdsRol) {
        if (zdsRol.getGerelateerde().getAdres() != null) {
            addZaakObjectAdresToZGW(zgwZaak, zdsRol);
        }
    }

    private void addZaakObjectAdresToZGW(ZgwZaak zgwZaak, ZdsRol zdsRol) {
        ZgwZaakObjectAdres zgwZaakObjectAdres = new ZgwZaakObjectAdres();
        zgwZaakObjectAdres.setObjectIdentificatie(modelMapper.map(zdsRol.getGerelateerde().getAdres(), ZgwZaakObjectObjectIdentificatieAdres.class));
        zgwZaakObjectAdres.setZaak(zgwZaak.getUrl());
        zgwZaakObjectAdres.setObjectType("adres");

        this.zgwClient.addZaakObject(zgwZaak, zgwZaakObjectAdres);
    }

    private void addOrUpdateZaakObjectToZgw(ZgwZaak zgwZaak, ZdsRol zdsRol) {
        if (zdsRol.getVerwerkingssoort().equalsIgnoreCase("T") && zdsRol.getGerelateerde().adres != null) {
            log.debug("Add addres");
            addZaakObjectToZGW(zgwZaak, zdsRol);
        }
        if (zdsRol.getVerwerkingssoort().equalsIgnoreCase("W") && zdsRol.getGerelateerde().adres != null) {
            log.debug("Update addres");
        }
        if (zdsRol.getVerwerkingssoort().equalsIgnoreCase("V") && zdsRol.getGerelateerde().adres != null) {
            log.debug("Remove addres");
        }
    }

    private void addRolToZgw(ZgwZaak createdZaak, ZgwZaakType zgwZaakType, ZdsRol zdsRol, String typeRolOmschrijving) {
        log.debug("addRolToZgw Rol: '" + typeRolOmschrijving + "'");
        if (zdsRol == null) {
            return;
        }
        if (zdsRol.gerelateerde == null) {
            // throw new ConverterException("Rol:" + typeRolOmschrijving + " zonder gerelateerde informatie");
            debugWarning("Rol:" + typeRolOmschrijving + " zonder gerelateerde informatie");
            return;
        }
        ZgwRol zgwRol = new ZgwRol();
        zgwRol.roltoelichting = typeRolOmschrijving + ": ";
        if (zdsRol.gerelateerde.medewerker != null) {
            zgwRol.betrokkeneIdentificatie = this.modelMapper.map(zdsRol.gerelateerde.medewerker, ZgwBetrokkeneIdentificatie.class);
            zgwRol.roltoelichting += zdsRol.gerelateerde.medewerker.achternaam != null ? zdsRol.gerelateerde.medewerker.achternaam : zdsRol.gerelateerde.medewerker.identificatie;
            zgwRol.betrokkeneType = BetrokkeneType.MEDEWERKER.getDescription();
        }
        if (zdsRol.gerelateerde.natuurlijkPersoon != null) {
            if (zgwRol.betrokkeneIdentificatie == null) {
                if (zgwRol.betrokkeneIdentificatie != null) {
                    throw new ConverterException("Rol: " + typeRolOmschrijving + " wordt al gebruikt voor medewerker");
                }
            }
            zgwRol.betrokkeneIdentificatie = this.modelMapper.map(zdsRol.gerelateerde.natuurlijkPersoon, ZgwBetrokkeneIdentificatie.class);
            // https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/118
            zgwRol.roltoelichting  += zdsRol.gerelateerde.natuurlijkPersoon.geslachtsnaam;
            if(zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres != null) {
                if(zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres != null) {
                    if(zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.identificatie == null || zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.identificatie.length() == 0) {
                        // https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/55
                        debugWarning("No aoaIdentificatie found for zaak with id: " + createdZaak.identificatie + " in rol: " + typeRolOmschrijving + " for natuurlijkPersoon");
                    }
                    else {
                        zgwRol.betrokkeneIdentificatie.verblijfsadres = this.modelMapper.map(zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres, ZgwAdres.class);
                        // https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
                        // 		Move code to the ModelMapperConfig.java

                        // Default behaviour uses straatnaam to fill the gorOpenbareRuimteNaam. Config allows for using openbareRuimteNaam instead for a specific zaaktype
                        var openbareRuimteNaam = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.straatnaam;
                        for(var translateVerblijfsadresForZaaktype : this.configService.getConfiguration().getTranslateVerblijfsadresForZaaktype()){
                            if(translateVerblijfsadresForZaaktype.getZaakType().equals(zgwZaakType.getIdentificatie())){
                                if(translateVerblijfsadresForZaaktype.getUseOpenbareRuimteNaam()){
                                    openbareRuimteNaam = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.openbareRuimteNaam;
                                }
                            }
                        }

                        zgwRol.betrokkeneIdentificatie.verblijfsadres = new ZgwAdres();
                        zgwRol.betrokkeneIdentificatie.verblijfsadres.aoaIdentificatie = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.identificatie;
                        zgwRol.betrokkeneIdentificatie.verblijfsadres.wplWoonplaatsNaam = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.woonplaatsnaam;
                        zgwRol.betrokkeneIdentificatie.verblijfsadres.gorOpenbareRuimteNaam = openbareRuimteNaam;
                        zgwRol.betrokkeneIdentificatie.verblijfsadres.aoaPostcode = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.postcode;
                        zgwRol.betrokkeneIdentificatie.verblijfsadres.aoaHuisnummer = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.huisnummer;
                        zgwRol.betrokkeneIdentificatie.verblijfsadres.aoaHuisletter = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.huisletter;
                        zgwRol.betrokkeneIdentificatie.verblijfsadres.aoaHuisnummertoevoeging = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.huisnummertoevoeging;
                        zgwRol.betrokkeneIdentificatie.verblijfsadres.inpLocatiebeschrijving  = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.locatiebeschrijving;
                    }
                }
            }
            zgwRol.betrokkeneType = BetrokkeneType.NATUURLIJK_PERSOON.getDescription();
        }
        if (zdsRol.gerelateerde.nietNatuurlijkPersoon != null) {
            if (zgwRol.betrokkeneIdentificatie == null) {
                if (zgwRol.betrokkeneIdentificatie != null) {
                    throw new ConverterException("Rol: " + typeRolOmschrijving + " wordt al gebruikt voor medewerker of natuurlijk persoon");
                }
            }
            zgwRol.betrokkeneIdentificatie = this.modelMapper.map(zdsRol.gerelateerde.nietNatuurlijkPersoon, ZgwBetrokkeneIdentificatie.class);
            // https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/118
            //zgwRol.betrokkeneIdentificatie.innNnpId = zdsRol.gerelateerde.nietNatuurlijkPersoon.annIdentificatie;
            //zgwRol.betrokkeneIdentificatie.annIdentificatie = zdsRol.gerelateerde.nietNatuurlijkPersoon.annIdentificatie;
            zgwRol.betrokkeneIdentificatie.statutaireNaam = zdsRol.gerelateerde.nietNatuurlijkPersoon.statutaireNaam;

            //zgwRol.betrokkeneIdentificatie.bezoekadres;
            zgwRol.roltoelichting  += zdsRol.gerelateerde.nietNatuurlijkPersoon.statutaireNaam;
            zgwRol.betrokkeneType = BetrokkeneType.NIET_NATUURLIJK_PERSOON.getDescription();

        }
        if (zdsRol.gerelateerde.vestiging != null) {
            if (zgwRol.betrokkeneIdentificatie == null) {
                if (zgwRol.betrokkeneIdentificatie != null) {
                    throw new ConverterException("Rol: " + typeRolOmschrijving + " wordt al gebruikt voor medewerker, natuurlijk persoon of niet natuurlijk persoon");
                }
            }
            zgwRol.betrokkeneIdentificatie = this.modelMapper.map(zdsRol.gerelateerde.vestiging, ZgwBetrokkeneIdentificatie.class);
            zgwRol.betrokkeneIdentificatie.vestigingsNummer = zdsRol.gerelateerde.vestiging.vestigingsNummer;
            zgwRol.betrokkeneIdentificatie.handelsnaam = new String[]{zdsRol.gerelateerde.vestiging.handelsnaam};

            //zgwRol.betrokkeneIdentificatie.bezoekadres;
            zgwRol.roltoelichting  += zdsRol.gerelateerde.vestiging.handelsnaam;
            zgwRol.betrokkeneType = BetrokkeneType.VESTIGING.getDescription();

        }
        if (zgwRol.betrokkeneIdentificatie == null) {
            //throw new ConverterException("Rol: " + typeRolOmschrijving + " zonder Natuurlijkpersoon or Medewerker");
            debugWarning("Rol: '" + typeRolOmschrijving + "' zonder (NIET) Natuurlijkpersoon or Medewerker");
            return;
        }
        var roltype = this.zgwClient.getRolTypeByZaaktypeAndOmschrijving(zgwZaakType, typeRolOmschrijving);
        if (roltype == null) {
            var zaaktype = this.zgwClient.getZaakTypeByUrl(createdZaak.zaaktype);
            throw new ConverterException(
                "Rol: '" + typeRolOmschrijving + "' niet gevonden bij Zaaktype: '" + zaaktype.identificatie + "'");
        }
        zgwRol.roltype = roltype.url;
        zgwRol.zaak = createdZaak.getUrl();
        this.zgwClient.addZgwRol(zgwRol);
    }

    public List<ZdsHeeftRelevant> geefLijstZaakdocumenten(String zaakidentificatie) {
        log.debug("geefLijstZaakdocumenten:" + zaakidentificatie);
        ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(zaakidentificatie);

        var relevanteDocumenten = new ArrayList<ZdsHeeftRelevant>();
        for (ZgwZaakInformatieObject zgwZaakInformatieObject : this.zgwClient
            .getZaakInformatieObjectenByZaak(zgwZaak.url)) {
            ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient
                .getZaakDocumentByUrl(zgwZaakInformatieObject.informatieobject);
            if (zgwEnkelvoudigInformatieObject == null) {
                throw new ConverterException("could not get the zaakdocument: "
                    + zgwZaakInformatieObject.informatieobject + " for zaak:" + zaakidentificatie);
            }
            ZgwInformatieObjectType documenttype = this.zgwClient
                .getZgwInformatieObjectTypeByUrl(zgwEnkelvoudigInformatieObject.informatieobjecttype);
            if (documenttype == null) {
                throw new ConverterException("getZgwInformatieObjectType #"
                    + zgwEnkelvoudigInformatieObject.informatieobjecttype + " could not be found");
            }
            /*
             * if(zgwEnkelvoudigInformatieObject == null) { throw new
             * ConverterException("ZgwEnkelvoudigInformatieObject #" + documentIdentificatie
             * + " could not be found"); } ZgwInformatieObjectType documenttype =
             * zgwClient.getZgwInformatieObjectTypeByÙrl(zgwEnkelvoudigInformatieObject.
             * informatieobjecttype); if(documenttype == null) { throw new
             * ConverterException("getZgwInformatieObjectType #" +
             * zgwEnkelvoudigInformatieObject.informatieobjecttype + " could not be found");
             * } var zgwZaakInformatieObject =
             * zgwClient.getZgwZaakInformatieObjectByEnkelvoudigInformatieObjectUrl(
             * zgwEnkelvoudigInformatieObject.getUrl()); if(zgwZaakInformatieObject == null)
             * { throw new ConverterException("getZgwZaakInformatieObjectByUrl #" +
             * zgwEnkelvoudigInformatieObject.getUrl() + " could not be found"); } var
             * zgwZaak = zgwClient.getZaakByUrl(zgwZaakInformatieObject.getZaak());
             * if(zgwZaak == null) { throw new ConverterException("getZaakByUrl #" +
             * zgwZaakInformatieObject.getZaak() + " could not be found"); } String inhoud =
             * zgwClient.getBas64Inhoud(zgwEnkelvoudigInformatieObject.getInhoud());
             * if(inhoud == null) { throw new ConverterException("getBas64Inhoud #" +
             * zgwEnkelvoudigInformatieObject.getInhoud() + " could not be found"); }
             *
             *
             * ZdsZaakDocumentInhoud result =
             * modelMapper.map(zgwEnkelvoudigInformatieObject, ZdsZaakDocumentInhoud.class);
             * result.inhoud = new ZdsInhoud(); var mimeType =
             * URLConnection.guessContentTypeFromName(zgwEnkelvoudigInformatieObject.
             * bestandsnaam); // documenttype result.omschrijving =
             * documenttype.omschrijving;
             *
             */
            ZdsZaakDocument zdsZaakDocument = this.modelMapper.map(zgwEnkelvoudigInformatieObject,
                ZdsZaakDocument.class);
            zdsZaakDocument.omschrijving = documenttype.omschrijving;
            ZdsHeeftRelevant heeftRelevant = this.modelMapper.map(zgwZaakInformatieObject, ZdsHeeftRelevant.class);
            heeftRelevant.gerelateerde = zdsZaakDocument;
            relevanteDocumenten.add(heeftRelevant);
        }
        return relevanteDocumenten;
    }

    public ZgwEnkelvoudigInformatieObject voegZaakDocumentToe(String rsin, ZdsZaakDocumentInhoud zdsInformatieObject) {
        log.debug("voegZaakDocumentToe:" + zdsInformatieObject.identificatie);

        var zaakIdentificatie = zdsInformatieObject.isRelevantVoor.gerelateerde.identificatie;
        ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(zaakIdentificatie);
        if (zgwZaak == null) {
            throw new RuntimeException("Zaak not found for identificatie: " + zaakIdentificatie);
        }
        ZgwZaakType zgwZaakType = this.zgwClient.getZaakTypeByZaak(zgwZaak);

        ZgwInformatieObjectType zgwInformatieObjectType = this.zgwClient.getZgwInformatieObjectTypeByOmschrijving(zgwZaakType, zdsInformatieObject.omschrijving);
        if (zgwInformatieObjectType == null) {
            throw new RuntimeException("Documenttype not found for omschrijving: " + zdsInformatieObject.omschrijving);
        }


        ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.modelMapper.map(zdsInformatieObject, ZgwEnkelvoudigInformatieObject.class);
        zgwEnkelvoudigInformatieObject.informatieobjecttype = zgwInformatieObjectType.url;
        zgwEnkelvoudigInformatieObject.bronorganisatie = rsin;
        // https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
        // 		Move code to the ModelMapperConfig.java
        if (zgwEnkelvoudigInformatieObject.verzenddatum != null && zgwEnkelvoudigInformatieObject.verzenddatum.length() == 0) {
            zgwEnkelvoudigInformatieObject.verzenddatum = null;
        }
        // https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
        // 		Move code to the ModelMapperConfig.java
        if (zgwEnkelvoudigInformatieObject.taal != null && zgwEnkelvoudigInformatieObject.taal.length() > 1) {
            debugWarning("taal only had 2, expected 3 characted, trying to convert: '" + zgwEnkelvoudigInformatieObject.taal + "'");
            // https://nl.wikipedia.org/wiki/Lijst_van_ISO_639-codes
            switch (zgwEnkelvoudigInformatieObject.taal.toLowerCase()) {
                case "fy":
                    // Fryslân boppe!
                    zgwEnkelvoudigInformatieObject.taal = "fry";
                    break;
                case "nl":
                    zgwEnkelvoudigInformatieObject.taal = "nld";
                    break;
                case "en":
                    zgwEnkelvoudigInformatieObject.taal = "eng";
                    break;
                case "nl-nl" :
                    zgwEnkelvoudigInformatieObject.taal = "nld";
                    break;
                default:
                    debugWarning("could not convert: '" + zgwEnkelvoudigInformatieObject.taal.toLowerCase() + "', this will possible result in an error");
            }
        }

        zgwEnkelvoudigInformatieObject.indicatieGebruiksrecht = "false";

        if (zgwEnkelvoudigInformatieObject.status != null) {
			/*
			in_bewerking - (In bewerking) Aan het informatieobject wordt nog gewerkt.
			ter_vaststelling - (Ter vaststelling) Informatieobject gereed maar moet nog vastgesteld worden.
			definitief - (Definitief) Informatieobject door bevoegd iets of iemand vastgesteld dan wel ontvangen.
			gearchiveerd - (Gearchiveerd) Informatieobject duurzaam bewaarbaar gemaakt; een gearchiveerd informatie-element.
			*/
            zgwEnkelvoudigInformatieObject.status = zgwEnkelvoudigInformatieObject.status.replace(" ", "_");
            zgwEnkelvoudigInformatieObject.status = zgwEnkelvoudigInformatieObject.status.toLowerCase();
        }
        zgwEnkelvoudigInformatieObject = this.zgwClient.addZaakDocument(zgwEnkelvoudigInformatieObject);
        ZgwZaakInformatieObject zgwZaakInformatieObject = addZaakInformatieObject(zgwEnkelvoudigInformatieObject, zgwZaak.url);

        // status
        if (zdsInformatieObject.isRelevantVoor.volgnummer != null
            && zdsInformatieObject.isRelevantVoor.omschrijving != null
            && zdsInformatieObject.isRelevantVoor.omschrijving.length() > 0
            && zdsInformatieObject.isRelevantVoor.datumStatusGezet != null) {
            log.debug("Update of zaakid:" + zgwZaak.identificatie + " has  status changes");
            var zgwStatusType = this.zgwClient.getStatusTypeByZaakTypeAndOmschrijving(zgwZaakType,
                zdsInformatieObject.isRelevantVoor.omschrijving, zdsInformatieObject.isRelevantVoor.volgnummer);
            // ZgwStatus zgwStatus = modelMapper.map(zdsHeeft, ZgwStatus.class);
            ZgwStatus zgwStatus = new ZgwStatus();
            zgwStatus.zaak = zgwZaak.url;
            zgwStatus.statustype = zgwStatusType.url;
            this.zgwClient.addZaakStatus(zgwStatus);
        }

        return zgwEnkelvoudigInformatieObject;
    }

    public ZgwZaakInformatieObject addZaakInformatieObject(ZgwEnkelvoudigInformatieObject doc, String zaakUrl) {
        var zgwZaakInformatieObject = new ZgwZaakInformatieObject();
        zgwZaakInformatieObject.setZaak(zaakUrl);
        zgwZaakInformatieObject.setInformatieobject(doc.getUrl());
        zgwZaakInformatieObject.setTitel(doc.getTitel());
        return this.zgwClient.addDocumentToZaak(zgwZaakInformatieObject);
    }

    public ZdsZaakDocumentInhoud getZaakDocumentLezen(String documentIdentificatie) {
        log.debug("getZaakDocumentLezen:" + documentIdentificatie);
        ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient
            .getZgwEnkelvoudigInformatieObjectByIdentiticatie(documentIdentificatie);
        if (zgwEnkelvoudigInformatieObject == null) {
            throw new ConverterException(
                "ZgwEnkelvoudigInformatieObject #" + documentIdentificatie + " could not be found");
        }
        ZgwInformatieObjectType documenttype = this.zgwClient
            .getZgwInformatieObjectTypeByUrl(zgwEnkelvoudigInformatieObject.informatieobjecttype);
        if (documenttype == null) {
            throw new ConverterException("getZgwInformatieObjectType #"
                + zgwEnkelvoudigInformatieObject.informatieobjecttype + " could not be found");
        }
        var zgwZaakInformatieObject = this.zgwClient
            .getZgwZaakInformatieObjectByEnkelvoudigInformatieObjectUrl(zgwEnkelvoudigInformatieObject.getUrl());
        if (zgwZaakInformatieObject == null) {
            throw new ConverterException("getZgwZaakInformatieObjectByUrl #" + zgwEnkelvoudigInformatieObject.getUrl()
                + " could not be found");
        }
        var zgwZaak = this.zgwClient.getZaakByUrl(zgwZaakInformatieObject.getZaak());
        if (zgwZaak == null) {
            throw new ConverterException("getZaakByUrl #" + zgwZaakInformatieObject.getZaak() + " could not be found");
        }
        String inhoud = this.zgwClient.getBas64Inhoud(zgwEnkelvoudigInformatieObject.getInhoud());
        if (inhoud == null) {
            throw new ConverterException(
                "getBas64Inhoud #" + zgwEnkelvoudigInformatieObject.getInhoud() + " could not be found");
        }

        ZdsZaakDocumentInhoud result = this.modelMapper.map(zgwEnkelvoudigInformatieObject,
            ZdsZaakDocumentInhoud.class);
        result.inhoud = new ZdsInhoud();
        var mimeType = URLConnection.guessContentTypeFromName(zgwEnkelvoudigInformatieObject.bestandsnaam);

        // documenttype
        result.omschrijving = documenttype.omschrijving;
        if (result.ontvangstdatum == null) {
            result.ontvangstdatum = "00010101";
        }
        result.titel = zgwEnkelvoudigInformatieObject.titel;
        result.beschrijving = zgwEnkelvoudigInformatieObject.beschrijving;
        if (result.beschrijving.length() == 0) {
            result.beschrijving = null;
        }
        if (result.versie.length() == 0) {
            result.versie = null;
        }
        if (result.taal.length() == 0) {
            result.taal = null;
        }
        if (result.status.length() == 0) {
            result.status = null;
        }

        result.formaat = zgwEnkelvoudigInformatieObject.bestandsnaam
            .substring(zgwEnkelvoudigInformatieObject.bestandsnaam.lastIndexOf(".") + 1);
        result.inhoud.contentType = mimeType;
        result.inhoud.bestandsnaam = zgwEnkelvoudigInformatieObject.bestandsnaam;
        result.inhoud.value = inhoud;
        result.isRelevantVoor = new ZdsIsRelevantVoor();
        result.isRelevantVoor.gerelateerde = new ZdsGerelateerde();
        result.isRelevantVoor.gerelateerde.entiteittype = "ZAK";
        result.isRelevantVoor.gerelateerde.identificatie = zgwZaak.identificatie;
        result.isRelevantVoor.gerelateerde.omschrijving = zgwZaak.omschrijving;

        return result;
    }

    public ZgwZaak actualiseerZaakstatus(ZdsZaak wasZaak, ZdsZaak wordtZaak) {
        log.debug("actualiseerZaakstatus:" + wasZaak.identificatie);
        var zaakid = wasZaak.identificatie;
        ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(zaakid);
        ZgwZaakType zgwZaakType = this.zgwClient.getZaakTypeByZaak(zgwZaak);
        setResultaatAndStatus(wordtZaak, zgwZaak, zgwZaakType);

        return zgwZaak;
    }

    public List<ZdsZaak> getZaakDetailsByBsn(String bsn) {
        log.debug("getZaakDetailsByBsn:" + bsn);
        var zgwRollen = this.zgwClient.getRollenByBsn(bsn);
        var zdsZaken = new ArrayList<ZdsZaak>();
        var result = new ArrayList<ZdsZaak>();
        for (ZgwRol rol : zgwRollen) {
            var zgwRolType = this.zgwClient.getRolTypeByUrl(rol.roltype);
            ZgwRolOmschrijving zgwRolOmschrijving = this.configService.getConfiguration().getZgwRolOmschrijving();
            if (zgwRolType.omschrijving.equals(zgwRolOmschrijving.getHeeftAlsInitiator())) {
                // TODO: hier minder overhead: hier wordt nu 2 keer achterelkaar een getzaak op openzaak gedaan!
                var zgwZaak = this.zgwClient.getZaakByUrl(rol.zaak);
                result.add(getZaakDetailsByIdentificatie(zgwZaak.identificatie));
            }
            if (result.size() >= 20) {
                // Max 20 results, it seems we get get unpredicted results after that
                debugWarning("Limit activated, no more than 20 results! (total amound found: " + zgwRollen.size() + " relations)");
                break;
            }
        }
        return result;
    }

    public ZdsZaak getZaakDetailsByIdentificatie(String zaakidentificatie) {
        log.debug("getZaakDetailsByIdentificatie:" + zaakidentificatie);
        var zgwZaak = this.zgwClient.getZaakByIdentificatie(zaakidentificatie);
        if (zgwZaak == null) {
            throw new ConverterException("Zaak not found for identification: '" + zaakidentificatie + "'");
        }
        var zgwZaakType = this.zgwClient.getZaakTypeByZaak(zgwZaak);

        //ZdsZaak zaak = new ZdsZaak();
        ZdsZaak zaak = this.modelMapper.map(zgwZaak, ZdsZaak.class);
        ZgwRolOmschrijving zgwRolOmschrijving = this.configService.getConfiguration().getZgwRolOmschrijving();

        for (ZgwRol zgwRol : this.zgwClient.getRollenByZaakUrl(zgwZaak.url)) {
            var rolGeconverteerd = false;

            if (zgwRolOmschrijving.getHeeftBetrekkingOp().equalsIgnoreCase(zgwRol.getOmschrijving())) {
                zaak.heeftBetrekkingOp = getZdsRol(zgwZaak, zgwZaakType, zgwRolOmschrijving.getHeeftAlsBelanghebbende(), "ZAKOBJ");
                rolGeconverteerd = true;
            }
            if (zgwRolOmschrijving.getHeeftAlsBelanghebbende().equalsIgnoreCase(zgwRol.getOmschrijving())) {
                zaak.heeftAlsBelanghebbende = getZdsRol(zgwZaak, zgwZaakType, zgwRolOmschrijving.getHeeftAlsBelanghebbende(), "ZAKBTRBLH");
                rolGeconverteerd = true;
            }
            if (zgwRolOmschrijving.getHeeftAlsInitiator().equalsIgnoreCase(zgwRol.getOmschrijving())) {
                zaak.heeftAlsInitiator = getZdsRol(zgwZaak, zgwZaakType, zgwRolOmschrijving.getHeeftAlsInitiator(), "ZAKBTRINI");
                rolGeconverteerd = true;
            }
            if (zgwRolOmschrijving.getHeeftAlsUitvoerende().equalsIgnoreCase(zgwRol.getOmschrijving())) {
                zaak.heeftAlsUitvoerende = getZdsRol(zgwZaak, zgwZaakType, zgwRolOmschrijving.getHeeftAlsUitvoerende(), "ZAKBTRUTV");
                rolGeconverteerd = true;
            }
            if (zgwRolOmschrijving.getHeeftAlsVerantwoordelijke().equalsIgnoreCase(zgwRol.getOmschrijving())) {
                zaak.heeftAlsVerantwoordelijke = getZdsRol(zgwZaak, zgwZaakType, zgwRolOmschrijving.getHeeftAlsVerantwoordelijke(), "ZAKBTRVRA");
                rolGeconverteerd = true;
            }
            if (zgwRolOmschrijving.getHeeftAlsGemachtigde().equalsIgnoreCase(zgwRol.getOmschrijving())) {
                zaak.heeftAlsGemachtigde = getZdsRol(zgwZaak, zgwZaakType, zgwRolOmschrijving.getHeeftAlsGemachtigde(), "ZAKBTRGMC");
                rolGeconverteerd = true;
            }
            if (zgwRolOmschrijving.getHeeftAlsOverigBetrokkene().equalsIgnoreCase(zgwRol.getOmschrijving())) {
                zaak.heeftAlsOverigBetrokkene = getZdsRol(zgwZaak, zgwZaakType, zgwRolOmschrijving.getHeeftAlsOverigBetrokkene(), "ZAKBTROVR");
                rolGeconverteerd = true;
            }
            if (!rolGeconverteerd) {
                throw new ConverterException("Rol: " + zgwRol.getOmschrijving() + " (" + zgwRol.getOmschrijvingGeneriek() + ") niet geconverteerd worden (" + zgwRol.uuid + ")");
            }
        }
        zaak.isVan = new ZdsRol();
        zaak.isVan.entiteittype = "ZAKZKT";
        zaak.isVan.gerelateerde = new ZdsGerelateerde();
        zaak.isVan.gerelateerde.entiteittype = "ZKT";

        zaak.isVan.gerelateerde.code = zgwZaakType.identificatie;
        zaak.isVan.gerelateerde.omschrijving = zgwZaakType.omschrijving;

        if (zgwZaak.getKenmerk() != null && !zgwZaak.getKenmerk().isEmpty()) {
            zaak.kenmerk = new ArrayList<>();
            for (ZgwKenmerk zgwKenmerk : zgwZaak.getKenmerk()) {
                var zdsKenmerkKenmerk = this.modelMapper.map(zgwKenmerk, ZdsKenmerk.class);
                zaak.kenmerk.add(zdsKenmerkKenmerk);
            }
        }

        zaak.opschorting = zgwZaak.getOpschorting() != null
            ? this.modelMapper.map(zgwZaak.getOpschorting(), ZdsOpschorting.class)
            : null;
        zaak.verlenging = zgwZaak.getVerlenging() != null
            ? this.modelMapper.map(zgwZaak.getVerlenging(), ZdsVerlenging.class)
            : null;

        var zdsStatussen = new ArrayList<ZdsHeeft>();
        for (ZgwStatus zgwStatus : this.zgwClient.getStatussenByZaakUrl(zgwZaak.url)) {
            ZgwStatusType zgwStatusType = this.zgwClient.getResource(zgwStatus.statustype, ZgwStatusType.class);
            // ZdsHeeft zdsHeeft = modelMapper.map(zgwStatus, ZdsHeeft.class);
            ZdsHeeft zdsHeeft = new ZdsHeeft();
            zdsHeeft.setEntiteittype("ZAKSTT");
            zdsHeeft.setIndicatieLaatsteStatus(Boolean.valueOf(zgwStatusType.isEindstatus) ? "J" : "N");

            zdsHeeft.gerelateerde = this.modelMapper.map(zgwStatus, ZdsGerelateerde.class);
            zdsHeeft.gerelateerde.setEntiteittype("STT");

            zdsHeeft.gerelateerde.zktCode = zgwZaakType.identificatie;
            zdsHeeft.gerelateerde.zktOmschrijving = zgwZaakType.omschrijving;
            zdsHeeft.gerelateerde.omschrijving = zgwStatus.statustoelichting;

            zdsStatussen.add(zdsHeeft);
        }
        zaak.heeft = zdsStatussen;
        return zaak;
    }

    private ZgwZaakType getZaakTypeByUrl(String url) {
        var zaakype = this.zgwClient.getZaakTypes(null).stream().filter(zgwZaakType -> zgwZaakType.url.equalsIgnoreCase(url)).findFirst().orElse(null);
        if (zaakype == null) {
            throw new ConverterException("Zaaktype met url:" + url + " niet gevonden!");
        }
        return zaakype;
    }

    private ZdsRol getZdsRol(ZgwZaak zgwZaak, ZgwZaakType zgwZaakType, String rolOmschrijving, String entiteittype) {
        var zgwRolType = this.zgwClient.getRolTypeByZaaktypeAndOmschrijving(zgwZaakType, rolOmschrijving);
        ZgwRol zgwRol = this.zgwClient.getRolByZaakUrlAndRolTypeUrl(zgwZaak.url, zgwRolType.url);
        if (zgwRol == null) {
            // geen rol voor deze
            return null;
        }
        ZdsRol zdsRol = this.modelMapper.map(zgwRol, ZdsRol.class);
        zdsRol.setEntiteittype(entiteittype);
        return zdsRol;
    }

    private void updateRolInZgw(ZgwZaak zgwZaak, ZgwZaakType zgwZaakType, String typeRolOmschrijving, ZdsRol newValue) {
        log.debug("updateRolInZgw Rol:" + typeRolOmschrijving);

        // no put action for rollen, so first delete then add
        log.debug("Attempting to update rol by deleting and adding as new");
        deleteRolFromZgw(zgwZaak, zgwZaakType, typeRolOmschrijving);

        if (newValue.gerelateerde == null) {
            log.debug("Not adding the rol:" + typeRolOmschrijving + ", gerelateerde == null ");
            return;
        }

        if (typeRolOmschrijving == null) {
            debugWarning("Not adding the rol, typeRolOmschrijving == null ");
            return;
        }

        addRolToZgw(zgwZaak, zgwZaakType, newValue, typeRolOmschrijving);
    }

    private void deleteRolFromZgw(ZgwZaak zgwZaak, ZgwZaakType zgwZaakType, String typeRolOmschrijving) {
        log.debug("deleteRolFromZgw Rol:" + typeRolOmschrijving);

        var roltype = this.zgwClient.getRolTypeByZaaktypeAndOmschrijving(zgwZaakType, typeRolOmschrijving);
        if (roltype == null) {
            // throw new ConverterException("Roltype: " + typeRolOmschrijving + " niet gevonden bij zaaktype voor zaak: " + zgwZaak.identificatie);
            debugWarning("Roltype: " + typeRolOmschrijving + " niet gevonden bij zaaktype voor zaak: " + zgwZaak.identificatie);
            return;
        }
        ZgwRol rol = this.zgwClient.getRolByZaakUrlAndRolTypeUrl(zgwZaak.url, roltype.url);
        if (rol == null) {
            //throw new ConverterException("Rol: " + typeRolOmschrijving + " niet gevonden bij zaak: " + zgwZaak.identificatie);
            debugWarning("Rol: " + typeRolOmschrijving + " niet gevonden bij zaaktype voor zaak: " + zgwZaak.identificatie);
            return;

        }
        this.zgwClient.deleteRol(rol.uuid);
    }

    public String getRolOmschrijvingGeneriekByRolName(String rolName) {
        ZgwRolOmschrijving zgwRolOmschrijving = this.configService.getConfiguration().getZgwRolOmschrijving();

        switch (rolName.toLowerCase()) {
            case "heeftalsbelanghebbende":
                return zgwRolOmschrijving.getHeeftAlsBelanghebbende();
            case "heeftalsinitiator":
                return zgwRolOmschrijving.getHeeftAlsInitiator();
            case "heeftalsuitvoerende":
                return zgwRolOmschrijving.getHeeftAlsUitvoerende();
            case "heeftalsverantwoordelijke":
                return zgwRolOmschrijving.getHeeftAlsVerantwoordelijke();
            case "heeftalsgemachtigde":
                return zgwRolOmschrijving.getHeeftAlsGemachtigde();
            case "heeftalsoverigbetrokkene":
                return zgwRolOmschrijving.getHeeftAlsOverigBetrokkene();
            case "heeftbetrekkingop":
                return zgwRolOmschrijving.getHeeftBetrekkingOp();
            default:
                return null;
        }
    }

    public String checkOutZaakDocument(String documentIdentificatie) {
        log.debug("checkOutZaakDocument:" + documentIdentificatie);
        ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient.getZgwEnkelvoudigInformatieObjectByIdentiticatie(documentIdentificatie);
        if (zgwEnkelvoudigInformatieObject == null) {
            throw new ConverterException("ZgwEnkelvoudigInformatieObjectByIdentiticatie not found for identificatie: " + documentIdentificatie);
        }
        if (zgwEnkelvoudigInformatieObject.locked) {
            throw new ConverterException("ZgwEnkelvoudigInformatieObjectByIdentiticatie with identificatie: " + zgwEnkelvoudigInformatieObject.identificatie + " cannot be locked and then changed. Object is allready locked");
        }

        ZgwLock lock = this.zgwClient.getZgwInformatieObjectLock(zgwEnkelvoudigInformatieObject);
        log.debug("received lock:" + lock.lock);
        return lock.lock;
    }

    public Object cancelCheckOutZaakDocument(String documentIdentificatie, String lock) {
        log.debug("checkOutZaakDocument:" + documentIdentificatie);
        ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient
            .getZgwEnkelvoudigInformatieObjectByIdentiticatie(documentIdentificatie);
        if (zgwEnkelvoudigInformatieObject == null) {
            throw new ConverterException(
                "ZgwEnkelvoudigInformatieObject #" + documentIdentificatie + " could not be found");
        }
        ZgwLock zgwLock = new ZgwLock();
        zgwLock.lock = lock;
        this.zgwClient.getZgwInformatieObjectUnLock(zgwEnkelvoudigInformatieObject, zgwLock);
        return null;
    }

    public ZgwEnkelvoudigInformatieObject updateZaakDocument(String lock, ZdsZaakDocumentInhoud zdsWasInformatieObject, ZdsZaakDocumentInhoud zdsWordtInformatieObject) {
        log.debug("updateZaakDocument lock:" + lock + " informatieobject:" + zdsWasInformatieObject.identificatie);

        var zgwWasEnkelvoudigInformatieObject = this.zgwClient.getZgwEnkelvoudigInformatieObjectByIdentiticatie(zdsWasInformatieObject.identificatie);
        if ("definitief".equals(zgwWasEnkelvoudigInformatieObject.status)) {
            throw new RuntimeException("ZgwEnkelvoudigInformatieObjectByIdentiticatie with identificatie: " + zdsWasInformatieObject.identificatie + " cannot be locked and then changed. Document status is 'definitief'");
        }


        // https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
        // 		Move code to the ModelMapperConfig.java
        //		Also merge, we shouldnt overwrite the old values this hard
        var zgwWordtEnkelvoudigInformatieObject = this.modelMapper.map(zdsWordtInformatieObject, ZgwEnkelvoudigInformatieObject.class);

        //Temporary fix for bug in Openzaak
        //Add calculated file Size, but only if it's > 0
        //This will add it if the document itself is updated, but won't add it if only metadata is changed
        var bestandsomvang = getFileSize(zgwWordtEnkelvoudigInformatieObject.inhoud);
        if(bestandsomvang > 0){
            zgwWordtEnkelvoudigInformatieObject.bestandsomvang = bestandsomvang;
        }
        if (zgwWordtEnkelvoudigInformatieObject.verzenddatum != null && zgwWordtEnkelvoudigInformatieObject.verzenddatum.length() == 0) {
            zgwWordtEnkelvoudigInformatieObject.verzenddatum = null;
        }
        //zgwEnkelvoudigInformatieObject.indicatieGebruiksrecht = "false";
        zgwWordtEnkelvoudigInformatieObject.bronorganisatie = zgwWasEnkelvoudigInformatieObject.bronorganisatie;
        zgwWordtEnkelvoudigInformatieObject.informatieobjecttype = zgwWasEnkelvoudigInformatieObject.informatieobjecttype;

        //	"in_bewerking" "ter_vaststelling" "definitief" "gearchiveerd"
        zgwWordtEnkelvoudigInformatieObject.status = zgwWordtEnkelvoudigInformatieObject.status.toLowerCase();
        zgwWordtEnkelvoudigInformatieObject.lock = lock;
        zgwWordtEnkelvoudigInformatieObject.url = zgwWasEnkelvoudigInformatieObject.url;
        zgwWasEnkelvoudigInformatieObject = this.zgwClient.patchZaakDocument(zgwWordtEnkelvoudigInformatieObject);
        //ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(zdsInformatieObject.isRelevantVoor.gerelateerde.identificatie);
        //ZgwZaakInformatieObject zgwZaakInformatieObject = addZaakInformatieObject(zgwEnkelvoudigInformatieObject, zgwZaak.url);
        ZgwLock zgwLock = new ZgwLock();
        zgwLock.lock = lock;
        this.zgwClient.getZgwInformatieObjectUnLock(zgwWordtEnkelvoudigInformatieObject, zgwLock);

        // status
        //if (zdsInformatieObject.isRelevantVoor.volgnummer != null
        //		&& zdsInformatieObject.isRelevantVoor.omschrijving != null
        //		&& zdsInformatieObject.isRelevantVoor.datumStatusGezet != null) {
        //	log.debug("Update of zaakid:" + zgwZaak.identificatie + " has  status changes");
        //	var zgwStatusType = this.zgwClient.getStatusTypeByZaakTypeAndOmschrijving(zgwZaak.zaaktype,
        //			zdsInformatieObject.isRelevantVoor.omschrijving, zdsInformatieObject.isRelevantVoor.volgnummer);
        //	// ZgwStatus zgwStatus = modelMapper.map(zdsHeeft, ZgwStatus.class);
        //	ZgwStatus zgwStatus = new ZgwStatus();
        //	zgwStatus.zaak = zgwZaak.url;
        //	zgwStatus.statustype = zgwStatusType.url;
        //	this.zgwClient.actualiseerZaakStatus(zgwStatus);
        //}
        return zgwWasEnkelvoudigInformatieObject;
    }

    private void debugWarning(String message) {
        log.info("[processing warning] " + message);
        debug.infopoint("Warning", message);
    }

    private Long getFileSize(String base64Inhoud) {
        if (base64Inhoud == null || base64Inhoud.isEmpty() || base64Inhoud.length() < 3) {
            return 0L;
        }
        return (long) Base64.getDecoder().decode(base64Inhoud).length;
    }

}
