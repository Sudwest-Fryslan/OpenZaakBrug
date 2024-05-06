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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsGerelateerde;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsHeeft;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsHeeftGerelateerde;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsHeeftRelevant;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsInhoud;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsIsRelevantVoor;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsKenmerk;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsOpschorting;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsRol;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsStuurgegevens;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsVan;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsVerlenging;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaak;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocument;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocumentInhoud;
import nl.haarlem.translations.zdstozgw.translation.zgw.client.ZGWClient;
import nl.haarlem.translations.zdstozgw.translation.zgw.client.ZgwAuthorization;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.BetrokkeneType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwAdres;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwAndereZaak;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwBetrokkeneIdentificatie;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObjectPost;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObjectPut;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwInformatieObjectType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwKenmerk;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwLock;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwResultaat;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwRol;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwRolType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwStatus;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwStatusType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaak;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakPut;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakType;
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
	
	public ZgwZaak creeerZaak(ZgwAuthorization authorization, ZdsZaak zdsZaak) {
		log.debug("creeerZaak:" + zdsZaak.identificatie);
		ZgwZaak zgwZaak = this.modelMapper.map(zdsZaak, ZgwZaak.class);

		var zaaktypecode = zdsZaak.isVan.gerelateerde.code;
		
		var zgwZaakType = this.zgwClient.getZgwZaakTypeByIdentificatie(authorization, zaaktypecode);
		if (zgwZaakType == null) {
			throw new ConverterException("Zaaktype met code:" + zaaktypecode + " could not be found");
		}
		zgwZaak.zaaktype = zgwZaakType.url;
		zgwZaak.bronorganisatie = authorization.getCatalogusRsin();
		zgwZaak.verantwoordelijkeOrganisatie = authorization.getCatalogusRsin();

		if (zdsZaak.getKenmerk() != null && !zdsZaak.getKenmerk().isEmpty()) {
			zgwZaak.kenmerken = new ArrayList<>();
			// TODO: controleren of werkt
			for (ZdsKenmerk kenmerk : zdsZaak.getKenmerk()) {
				zgwZaak.kenmerken.add(this.modelMapper.map(kenmerk, ZgwKenmerk.class));
			}
		}

		// alleen een verlenging meenemen als er echt waarden in staan
		if(zgwZaak.verlenging != null) {
			if (zgwZaak.verlenging.reden == null || zgwZaak.verlenging.reden.length() == 0) {
				zgwZaak.verlenging = null;
			}
			else {
				// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
				// 		Move code to the ModelMapperConfig.java
				zgwZaak.verlenging.duur = "P" + zgwZaak.verlenging.duur + "D";
			}
		}

		zgwZaak = this.zgwClient.addZaak(authorization, zgwZaak);
		log.debug("Created a ZGW Zaak with UUID: " + zgwZaak.getUuid());

		// rollen
		ZgwRolOmschrijving zgwRolOmschrijving = this.configService.getConfiguration().getZgwRolOmschrijving();
		addRolToZgw(authorization, zgwZaak, zgwZaakType, zdsZaak.heeftBetrekkingOp, zgwRolOmschrijving.getHeeftBetrekkingOp());
		addRolToZgw(authorization, zgwZaak, zgwZaakType, zdsZaak.heeftAlsBelanghebbende, zgwRolOmschrijving.getHeeftAlsBelanghebbende());
		addRolToZgw(authorization, zgwZaak, zgwZaakType, zdsZaak.heeftAlsInitiator, zgwRolOmschrijving.getHeeftAlsInitiator());
		addRolToZgw(authorization, zgwZaak, zgwZaakType, zdsZaak.heeftAlsUitvoerende, zgwRolOmschrijving.getHeeftAlsUitvoerende());
		addRolToZgw(authorization, zgwZaak, zgwZaakType, zdsZaak.heeftAlsVerantwoordelijke, zgwRolOmschrijving.getHeeftAlsVerantwoordelijke());
		addRolToZgw(authorization, zgwZaak, zgwZaakType, zdsZaak.heeftAlsGemachtigde, zgwRolOmschrijving.getHeeftAlsGemachtigde());
		addRolToZgw(authorization, zgwZaak, zgwZaakType, zdsZaak.heeftAlsOverigBetrokkene, zgwRolOmschrijving.getHeeftAlsOverigBetrokkene());


		/*
            <object StUF:sleutelVerzendend="184105" StUF:entiteittype="ZAK">
               <identificatie>1900207494</identificatie>
				....
               <heeftAlsHoofdzaak xsi:nil="true" StUF:noValue="geenWaarde" StUF:entiteittype="ZAKZAKHFD"/>
               <heeftBetrekkingOpAndere StUF:entiteittype="ZAKZAKBTR">
                  <gerelateerde StUF:entiteittype="ZAK">
                     <identificatie>1900242904</identificatie>
                     <omschrijving>Aanvraag Begeleiding ind.</omschrijving>
                     <isVan StUF:entiteittype="ZAKZKT">
                        <gerelateerde StUF:entiteittype="ZKT">
                           <omschrijving>Aanvraag Begeleiding individueel</omschrijving>
                           <code>B1577</code>
                        </gerelateerde>
                     </isVan>
                  </gerelateerde>
               </heeftBetrekkingOpAndere>
               <heeftBetrekkingOpAndere StUF:entiteittype="ZAKZAKBTR">
                  <gerelateerde StUF:entiteittype="ZAK">
                     <identificatie>1900330334</identificatie>
                     <omschrijving>Aanvraag Begeleiding ind.</omschrijving>
                     <isVan StUF:entiteittype="ZAKZKT">
                        <gerelateerde StUF:entiteittype="ZKT">
                           <omschrijving>Aanvraag Begeleiding individueel</omschrijving>
                           <code>B1577</code>
                        </gerelateerde>
                     </isVan>
                  </gerelateerde>
               </heeftBetrekkingOpAndere>
				....
              </object>


            <object StUF:sleutelVerzendend="215427" StUF:entiteittype="ZAK">
               <identificatie>1900242904</identificatie>
				....
               <heeftAlsHoofdzaak xsi:nil="true" StUF:noValue="geenWaarde" StUF:entiteittype="ZAKZAKHFD"/>
               <heeftBetrekkingOpAndere StUF:entiteittype="ZAKZAKBTR">
                  <gerelateerde StUF:entiteittype="ZAK">
                     <identificatie>1900207494</identificatie>
                     <omschrijving>Ondersteuningsplan D009757</omschrijving>
                     <isVan StUF:entiteittype="ZAKZKT">
                        <gerelateerde StUF:entiteittype="ZKT">
                           <omschrijving>Planzaak</omschrijving>
                           <code>B1647</code>
                        </gerelateerde>
                     </isVan>
                  </gerelateerde>
               </heeftBetrekkingOpAndere>
               ....

            </object>


		 */
		// hoofd-zaak
		if(zdsZaak.heeftAlsHoofdzaak != null) {
			debugWarning("In zaak:" + zdsZaak.identificatie + " unsupported 'heeftAlsHoofdzaak'");
		}
		// deel-zaak
		if(zdsZaak.heeftAlsDeelzaak != null) {
			debugWarning("In zaak:" + zdsZaak.identificatie + " unsupported 'heeftAlsDeelzaak'");
		}
		// related-zaak
		if(zdsZaak.heeftBetrekkingOpAndere != null) {
			for(ZdsHeeftGerelateerde heeftBetrekkingOpAndere: zdsZaak.heeftBetrekkingOpAndere) {
				if(heeftBetrekkingOpAndere.gerelateerde != null  && "ZAK".equals(heeftBetrekkingOpAndere.gerelateerde.entiteittype)) {
					ZgwZaak zgwAndereZaak= this.zgwClient.getZaakByIdentificatie(authorization, heeftBetrekkingOpAndere.gerelateerde.identificatie);
					if (zgwAndereZaak == null) {
							// throw new ConverterException("Zaak with identification '" + heeftBetrekkingOpAndere.gerelateerde.identificatie + "' not found in ZGW");
						debugWarning("Setting heeftBetrekkingOpAndere, zaak: '" + heeftBetrekkingOpAndere.gerelateerde.identificatie + "' not found in ZGW (referenced from within zaak:" + zdsZaak.identificatie + ")");
					}
					else {
						//	bijdrage	een zaak van het ZAAKTYPE levert een bijdrage aan het bereiken van de uitkomst van een zaak van het andere ZAAKTYPE
						//	onderwerp	een zaak van het ZAAKTYPE heeft betrekking op een zaak van het andere ZAAKTYPE of een zaak van het andere ZAAKTYPE is relevant voor of is onderwerp van een zaak van het ZAAKTYPE
						// 	vervolg		een zaak van het ZAAKTYPE is een te plannen vervolg op een zaak van het andere ZAAKTYPE

						// we need to make the relation to both sides (to get the expected behaviour)
						zgwClient.addRelevanteAndereZaakToZaak(authorization, zgwZaak, zgwAndereZaak, "onderwerp");
						zgwClient.addRelevanteAndereZaakToZaak(authorization, zgwAndereZaak, zgwZaak, "onderwerp");
					}
				}
			}
		}

		// last, the resultaat and status (lastly, since it things go wrong, it is often over here)
		setResultaatAndStatus(authorization, zdsZaak, zgwZaak, zgwZaakType);

		return zgwZaak;
	}

	public void updateZaak(ZgwAuthorization authorization, ZdsZaak zdsWordtZaak) {
		var zdsWasZaak = getZaakDetailsByIdentificatie(authorization, zdsWordtZaak.getIdentificatie());
		updateZaak(authorization, zdsWasZaak, zdsWordtZaak);
	}

	public void updateZaak(ZgwAuthorization authorization, ZdsZaak zdsWasZaak, ZdsZaak zdsWordtZaak) {
		log.debug("updateZaak:" + zdsWordtZaak.identificatie);
		ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(authorization, zdsWordtZaak.identificatie);
		if (zgwZaak == null) {
			throw new ConverterException("Zaak with identification: '" + zdsWordtZaak.identificatie + "' not found in ZGW");
		}
		ZgwZaakType zgwZaakType = this.zgwClient.getZaakTypeByZaak(authorization, zgwZaak);

		var changed = false;
		ChangeDetector changeDetector = new ChangeDetector();

		// check if the zdsWasZaak is equal to the one stored inside ZgwRegistry
		// this should be the case
		ZdsZaak zdsStored = this.modelMapper.map(zgwZaak, ZdsZaak.class);
		if(zdsWasZaak != null) {
			var storedVsWasChanges = changeDetector.detect(zdsStored, zdsWasZaak);
			var storedVsWasFieldsChanges = storedVsWasChanges.getAllChangesByDeclaringClassAndFilter(ZdsZaak.class, ZdsRol.class);
			if (storedVsWasFieldsChanges.size() > 0) {
				log.debug("Update of zaakid:" + zdsWasZaak.identificatie + " has # " + storedVsWasFieldsChanges.size() + " field changes between stored and was");
				for (Change change : storedVsWasFieldsChanges.keySet()) {
					debugWarning("The field: " + change.getField().getName() + " does not match (" + change.getChangeType() + ") stored-value:'" + change.getCurrentValue()  + "' , was-value:'" + change.getNewValue() + "'");
				}
			}
		}
		else {
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
			// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
			// 		Move code to the ModelMapperConfig.java
			if(updatedZaak.verlenging != null) {
				if(updatedZaak.verlenging.reden == null || updatedZaak.verlenging.reden.length() == 0) {
					updatedZaak.verlenging = null;
				}
				else if(updatedZaak.verlenging.duur == null || updatedZaak.verlenging.duur.length() == 0) {
					updatedZaak.verlenging = null;
				}
				else if(!(updatedZaak.verlenging.duur.startsWith("P") && updatedZaak.verlenging.duur.endsWith("D"))) {
					updatedZaak.verlenging.duur = "P" + updatedZaak.verlenging.duur + "D";
				}
			}
			this.zgwClient.updateZaak(authorization, zgwZaak.uuid, updatedZaak);
			changed = true;
		}

		// rollen
		var wasVsWordtRolChanges = wasVsWordtChanges.getAllChangesByFieldType(ZdsRol.class);
		if (wasVsWordtRolChanges.size() > 0) {
			log.debug("Update of zaakid:" + zdsWasZaak.identificatie + " has # " + wasVsWordtRolChanges.size() + " rol changes:");

//			for(ChangeDetector.Change change : wasVsWordtRolChanges.keySet()) {
//				log.info("Rol field:" + change.getField().getName() + " changetype:" + change.getChangeType() + " currentvalue:" + change.getCurrentValue() + " newvalue:" + change.getNewValue());
//			}

			changeDetector.filterChangesByType(wasVsWordtRolChanges, ChangeDetector.ChangeType.NEW)
					.forEach((change, changeType) -> {
						var rolnaam = getRolOmschrijvingGeneriekByRolName(change.getField().getName());
						// Special case since Initiator may occur once in open-zaak
						boolean initiatorExists=false;
						if("Initiator".equals(rolnaam)) {
							ZdsZaak storedZaak = getZaakDetailsByIdentificatie(authorization, zdsWordtZaak.identificatie);
							if(storedZaak.heeftAlsInitiator != null) {
								initiatorExists=true;
							}
						}

						if(initiatorExists) {
							log.debug("[CHANGE ROL] Update Rol:" + rolnaam);
							debugWarning("Rol [Initiator] already exists updating the existing");
							updateRolInZgw(authorization, zgwZaak, zgwZaakType, rolnaam, (ZdsRol) change.getNewValue());
						} else {
							log.debug("[CHANGE ROL] New Rol:" + rolnaam);
							addRolToZgw(authorization, zgwZaak, zgwZaakType, (ZdsRol) change.getNewValue(), rolnaam);
						}
					});

			changeDetector.filterChangesByType(wasVsWordtRolChanges, ChangeDetector.ChangeType.DELETED)
					.forEach((change, changeType) -> {
						var rolnaam = getRolOmschrijvingGeneriekByRolName(change.getField().getName());
						if(rolnaam != null) {
							log.debug("[CHANGE ROL] Deleted Rol:" + rolnaam);
							deleteRolFromZgw(authorization, zgwZaak, zgwZaakType, rolnaam);
						}
					});

			changeDetector.filterChangesByType(wasVsWordtRolChanges, ChangeDetector.ChangeType.CHANGED)
					.forEach((change, changeType) -> {
						var rolnaam = getRolOmschrijvingGeneriekByRolName(change.getField().getName());
						log.debug("[CHANGE ROL] Update Rol:" + rolnaam);
						updateRolInZgw(authorization, zgwZaak, zgwZaakType, rolnaam, (ZdsRol) change.getNewValue());
					});
			changed = true;
		}

		boolean hasChanged = setResultaatAndStatus(authorization, zdsWordtZaak, zgwZaak, zgwZaakType);

		if (!changed && ! hasChanged) {
			debugWarning("Update of zaakid:" + zdsWasZaak.identificatie + " without any changes");
		}
	}

	private String convertZdsStatusDatumtoZgwDateTime(ZgwAuthorization authorization, ZgwZaak zgwZaak, String zdsStatusDatum) {
		var formatter = new SimpleDateFormat("yyyyMMdd00000000");
		var dagstart = formatter.format(new Date());
		formatter = new SimpleDateFormat("yyyyMMddHHmmssSS");
		if(zdsStatusDatum == null || zdsStatusDatum.length() == 0) {
			debugWarning("no statusdatetime provided, using now()");
			zdsStatusDatum = formatter.format(new Date());
		}
		else if(zdsStatusDatum.length() < 16) {
			// maken it length of 16
			zdsStatusDatum = zdsStatusDatum + StringUtils.repeat("0", 16 - zdsStatusDatum.length());
		}

		if(dagstart.startsWith(zdsStatusDatum)) {
			debugWarning("statusdatetime contains no time, using now() (DatumGezet, has to be unique)");
			zdsStatusDatum = formatter.format(new Date());
		}

		var zgwStatusDatumTijd = (ModelMapperConfig.convertStufDateTimeToZgwDateTime(zdsStatusDatum));
		if(zgwStatusDatumTijd.endsWith("T00:00:00.000000Z")) {
			// The combination of zaak-uuid with datetime should be unique...
			// We only do this, when we have a datetime, thus when time without seconds
			int index = this.zgwClient.getStatussenByZaakUrl(authorization, zgwZaak.url).size();
			zgwStatusDatumTijd = (ModelMapperConfig.convertStufDateTimeToZgwDateTime(zdsStatusDatum, index));
		}
		return zgwStatusDatumTijd;
	}

	public boolean setResultaatAndStatus(ZgwAuthorization authorization, ZdsZaak zdsZaak, ZgwZaak zgwZaak, ZgwZaakType zgwZaakType) {
		var changed = false;
		var beeindigd = false;

		var newStatuses = new ArrayList<ZgwStatus>();

		// Check if zaak already has the endstatus set (updateZaak can happen after zaak is closed)
		var statusTypes = this.zgwClient.getStatusTypesByZaakType(authorization, zgwZaakType);
		var endStatusType = statusTypes.stream()
				.filter(statusType -> "true".equals(statusType.getIsEindstatus()))
				.findFirst();

		var presentStatuses = this.zgwClient.getStatussenByZaakUrl(authorization, zgwZaak.url);
		beeindigd = presentStatuses!=null && presentStatuses.stream().anyMatch(s -> s.statustype.equals(endStatusType.get().url) ? true : false);

		if (zdsZaak.heeft != null) {
			for (ZdsHeeft zdsHeeftIterator : zdsZaak.heeft) {
				ZdsGerelateerde zdsStatus = zdsHeeftIterator.gerelateerde;
				if(zdsStatus != null && zdsStatus.omschrijving != null && zdsStatus.omschrijving.length() > 0) {
					log.debug("Update of zaakid:" + zdsZaak.identificatie + " wants status to be changed to:" + zdsStatus.omschrijving);
					ZgwStatusType zgwStatusType = this.zgwClient.getStatusTypeByZaakTypeAndOmschrijving(authorization, zgwZaakType, zdsStatus.omschrijving, zdsStatus.volgnummer);
					ZgwStatus zgwStatus = this.modelMapper.map(zdsHeeftIterator, ZgwStatus.class);
					zgwStatus.zaak = zgwZaak.url;
					zgwStatus.statustype = zgwStatusType.url;
					zgwStatus.statustoelichting = zgwStatusType.omschrijving;
					String zdsStatusDatum = zdsHeeftIterator.getDatumStatusGezet();

					if("true".equals(zgwStatusType.getIsEindstatus())) {
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
						beeindigd = true;
					}
					zgwStatus.setDatumStatusGezet(convertZdsStatusDatumtoZgwDateTime(authorization, zgwZaak, zdsStatusDatum));

					// check if the status doesnt exist yet
					var statusexists = false;
					if(beeindigd) { // check if last status already exists
						statusexists = presentStatuses.stream().anyMatch(s -> s.statustype.equals(zgwStatus.statustype) ? true : false);
						log.debug("last status ["+zgwStatus.statustoelichting+"] is already registered: "+statusexists);
					} else {
						for(ZgwStatus s : presentStatuses) {
							var foundDateTime = ZonedDateTime.parse(s.datumStatusGezet);
							var newDateTime =  ZonedDateTime.parse(zgwStatus.datumStatusGezet);
							if(foundDateTime.equals(newDateTime)) {
								// throw an exception when the status descr
								if(!s.statustype.equals((zgwStatus.statustype))) {
									throw new ConverterException("found status on exact same timestamp with an different type. Got statustype" + s.statustype + " found:" + zgwStatus.statustype);
								}
								// already exist
								statusexists = true;
							}
						}
					}
					if(!statusexists) {
						newStatuses.add(zgwStatus);
					}
				}
				else {
					debugWarning("status has 'heeft' without 'gerelateerde' or  omschrijving");
				}
			}
		}

		// if there is a resultaat
		if (zdsZaak.resultaat != null && zdsZaak.resultaat.omschrijving != null && zdsZaak.resultaat.omschrijving.length() > 0) {
			var resultaatomschrijving = zdsZaak.resultaat.omschrijving;
			log.debug("Update of zaakid:" + zdsZaak.identificatie + " wants resultaat to be changed to:" + zdsZaak.resultaat.omschrijving );
			var zgwResultaatType = this.zgwClient.getResultaatTypeByZaakTypeAndOmschrijving(authorization, zgwZaakType, zdsZaak.resultaat.omschrijving);
			if(zgwResultaatType == null) {
				throw new ConverterException("Resultaattype: " + resultaatomschrijving + " niet gevonden bij Zaaktype: " + zgwZaakType.identificatie);
			}

			// remove any existing resultaten (we only want to have 1)
			var resultaten = this.zgwClient.getResultatenByZaakUrl(authorization, zgwZaak.url);
			for (ZgwResultaat resultaat : resultaten) {
				debugWarning("Zaak with identificatie:" + zdsZaak.identificatie + " already has resultaat #" + resultaten.indexOf(resultaat) + " met toelichting:" +  resultaat.toelichting + "(" + resultaat.uuid  + "), will be deleted");
				this.zgwClient.deleteZaakResultaat(authorization, resultaat.uuid);
			}

			ZgwResultaat zgwResultaat = new ZgwResultaat();
			zgwResultaat.zaak = zgwZaak.url;
			zgwResultaat.resultaattype = zgwResultaatType.url;
			zgwResultaat.toelichting = zdsZaak.resultaat.omschrijving;

			this.zgwClient.addZaakResultaat(authorization, zgwResultaat);
			changed = true;
		}

		// Use-case of the current action closing a zaak and no resultaat being present
		Optional<ZgwStatus> newEndStatus = Optional.empty();
		if(endStatusType.isPresent()) {
			newEndStatus = newStatuses.stream()
					.filter(newStatus -> newStatus.statustype.equals(endStatusType.get().url))
					.findFirst();
		}

		if(newEndStatus.isPresent()) {
			var resultaten = this.zgwClient.getResultatenByZaakUrl(authorization, zgwZaak.url);
			if(resultaten.isEmpty()) {
				for(var beeindig : this.configService.getConfiguration().getBeeindigZaakWanneerEinddatum() ) {
					if(beeindig.zaakType.equals(zgwZaakType.getIdentificatie())) {
						var zgwResultaatType = this.zgwClient.getResultaatTypeByZaakTypeAndOmschrijving(authorization, zgwZaakType, beeindig.coalesceResultaat);
						if(zgwResultaatType == null) {
							throw new ConverterException("Resultaattype: '" + beeindig.coalesceResultaat + "' niet gevonden bij Zaaktype:'" + zgwZaakType.identificatie + "'");
						}
						ZgwResultaat zgwResultaat = new ZgwResultaat();
						zgwResultaat.zaak = zgwZaak.url;
						zgwResultaat.resultaattype = zgwResultaatType.url;
						zgwResultaat.toelichting = zgwResultaatType.omschrijving;
						debugWarning("BeeindigZaakWanneerEinddatum was defined for zaaktype:'" + zgwZaakType.identificatie + "', zaak is about to be closed but has no resultaat, zaak will get resultaat:'" + zgwResultaatType.getOmschrijving() + "'");
						this.zgwClient.addZaakResultaat(authorization, zgwResultaat);
					}
				}
			}
		}

		// beeindigZaakWanneerEinddatum logica
		if(zdsZaak.einddatum != null && zdsZaak.einddatum.length() > 0 && !beeindigd) {
			for(var beeindig : this.configService.getConfiguration().getBeeindigZaakWanneerEinddatum() ) {
				if(beeindig.zaakType.equals(zgwZaakType.getIdentificatie())) {
					// is the result also missing?
					var resultaten = this.zgwClient.getResultatenByZaakUrl(authorization, zgwZaak.url);
					if(resultaten.size() == 0) {
						var zgwResultaatType = this.zgwClient.getResultaatTypeByZaakTypeAndOmschrijving(authorization, zgwZaakType, beeindig.coalesceResultaat);
						if(zgwResultaatType == null) {
							throw new ConverterException("Resultaattype: '" + beeindig.coalesceResultaat + "' niet gevonden bij Zaaktype:'" + zgwZaakType.identificatie + "'");
						}
						ZgwResultaat zgwResultaat = new ZgwResultaat();
						zgwResultaat.zaak = zgwZaak.url;
						zgwResultaat.resultaattype = zgwResultaatType.url;
						zgwResultaat.toelichting = zgwResultaatType.omschrijving;
						debugWarning("BeeindigZaakWanneerEinddatum was defined for zaaktype:'" + zgwZaakType.identificatie + "' and an einddatum was provided, no resultaat, zaak will get resultaat:'" + zgwResultaatType.getOmschrijving() + "'");
						this.zgwClient.addZaakResultaat(authorization, zgwResultaat);
					}

					var zgwStatusType = this.zgwClient.getLastStatusTypeByZaakType(authorization, zgwZaakType);
					ZgwStatus zgwStatus = new ZgwStatus();
					zgwStatus.zaak = zgwZaak.url;
					zgwStatus.statustype = zgwStatusType.url;
					zgwStatus.statustoelichting = zgwStatusType.omschrijving;
					zgwStatus.setDatumStatusGezet(convertZdsStatusDatumtoZgwDateTime(authorization, zgwZaak, zdsZaak.einddatum));
					debugWarning("BeeindigZaakWanneerEinddatum was defined for zaaktype:'" + zgwZaakType.identificatie + "' and an einddatum was provided, no eindstatus, zaak will get status:'" + zgwStatusType.getOmschrijving() + "' with time:" + zgwStatus.getDatumStatusGezet());
					this.zgwClient.addZaakStatus(authorization, zgwStatus);
					beeindigd = true;
					changed = true;
				}
			}
		}

		newStatuses.forEach(newStatus -> this.zgwClient.addZaakStatus(authorization, newStatus));
		if(!newStatuses.isEmpty()) {
			changed = true;
		}

		return changed;
	}

	private void addRolToZgw(ZgwAuthorization authorization, ZgwZaak createdZaak, ZgwZaakType zgwZaakType, ZdsRol zdsRol, String typeRolOmschrijving) {
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
		var roltype = this.zgwClient.getRolTypeByZaaktypeAndOmschrijving(authorization, zgwZaakType, typeRolOmschrijving);
		if (roltype == null) {
			var zaaktype = this.zgwClient.getZaakTypeByUrl(authorization, createdZaak.zaaktype);
			throw new ConverterException(
					"Rol: '" + typeRolOmschrijving + "' niet gevonden bij Zaaktype: '" + zaaktype.identificatie + "'");
		}
		zgwRol.roltype = roltype.url;
		zgwRol.zaak = createdZaak.getUrl();
		this.zgwClient.addZgwRol(authorization, zgwRol);
	}

	public List<ZdsHeeftRelevant> geefLijstZaakdocumenten(ZgwAuthorization authorization, String zaakidentificatie) {
		log.debug("geefLijstZaakdocumenten:" + zaakidentificatie);
		ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(authorization, zaakidentificatie);
	
		var relevanteDocumenten = new ArrayList<ZdsHeeftRelevant>();
		List<ZgwZaakInformatieObject> zgwZaakInformatieObjecten = new ArrayList<ZgwZaakInformatieObject>();
		if(zgwZaak.getZaakinformatieobjecten() != null) {
			for(String zaakinformatieobjectenUrl : zgwZaak.zaakinformatieobjecten) {
				zgwZaakInformatieObjecten.add(this.zgwClient.getZaakInformatieObjectByUrl(authorization, zaakinformatieobjectenUrl));
			}
		}
		else {
			zgwZaakInformatieObjecten = this.zgwClient.getZaakInformatieObjectenByZaak(authorization, zgwZaak.url);
		}
			
		// var zgwZaakInformatieObjecten = this.zgwClient.getZaakInformatieObjectenByZaak(authorization, zgwZaak.url);
		
		if(this.zgwClient.additionalCallToRetrieveRelatedObjectInformatieObjectenForCaching && zgwZaakInformatieObjecten.size() > 0) {
			// fill the cache in the drc if needed
			var zgwObjectInformatieObjecten = this.zgwClient.getObjectInformatieObjectsByUrl(authorization, zgwZaak.url);
			debugWarning("Retrieved ObjectInformatieObjecten to fill the cache on the (CMIS-)DRC (call not needed for ZdsToZgw)");
		}
		for (ZgwZaakInformatieObject zgwZaakInformatieObject : zgwZaakInformatieObjecten) {
			ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient
					.getZaakDocumentByUrl(authorization, zgwZaakInformatieObject.informatieobject);
			if (zgwEnkelvoudigInformatieObject == null || zgwEnkelvoudigInformatieObject.informatieobjecttype == null) {
				throw new ConverterException("could not get the zaakdocument: "
						+ zgwZaakInformatieObject.informatieobject + " for zaak:" + zaakidentificatie);
			}
			ZgwInformatieObjectType documenttype = this.zgwClient
					.getZgwInformatieObjectTypeByUrl(authorization, zgwEnkelvoudigInformatieObject.informatieobjecttype);
			if (documenttype == null) {
				throw new ConverterException("getZgwInformatieObjectType #"
						+ zgwEnkelvoudigInformatieObject.informatieobjecttype + " could not be found");
			}
			ZdsZaakDocument zdsZaakDocument = this.modelMapper.map(zgwEnkelvoudigInformatieObject,
					ZdsZaakDocument.class);
			zdsZaakDocument.omschrijving = documenttype.omschrijving;
			ZdsHeeftRelevant heeftRelevant = this.modelMapper.map(zgwZaakInformatieObject, ZdsHeeftRelevant.class);
			heeftRelevant.gerelateerde = zdsZaakDocument;
			relevanteDocumenten.add(heeftRelevant);

		}
		return relevanteDocumenten;
	}

	
	public ZgwEnkelvoudigInformatieObject voegZaakDocumentToe(ZgwAuthorization authorization,  ZdsZaakDocumentInhoud zdsInformatieObject) {
		log.debug("voegZaakDocumentToe:" + zdsInformatieObject.identificatie);
		var zaakIdentificatie = zdsInformatieObject.isRelevantVoor.gerelateerde.identificatie;
		ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(authorization, zaakIdentificatie);
		if (zgwZaak == null) {
			throw new ConverterException("Zaak not found for identificatie: " + zaakIdentificatie);
		}
		ZgwZaakType zgwZaakType = this.zgwClient.getZaakTypeByZaak(authorization, zgwZaak);

		ZgwInformatieObjectType zgwInformatieObjectType = this.zgwClient.getZgwInformatieObjectTypeByOmschrijving(authorization, zgwZaakType, zdsInformatieObject.omschrijving);
		if (zgwInformatieObjectType == null) {
			throw new ConverterException("Documenttype not found for: '" + zdsInformatieObject.omschrijving + "' in zaaktype:" + zgwZaakType.identificatie + " (" + zgwZaakType.omschrijving + ")");
		}


		ZgwEnkelvoudigInformatieObjectPost zgwEnkelvoudigInformatieObjectPost = this.modelMapper.map(zdsInformatieObject, ZgwEnkelvoudigInformatieObjectPost.class);
		zgwEnkelvoudigInformatieObjectPost.informatieobjecttype = zgwInformatieObjectType.url;
		zgwEnkelvoudigInformatieObjectPost.bronorganisatie = authorization.getCatalogusRsin();
		// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
		// 		Move code to the ModelMapperConfig.java
		if(zgwEnkelvoudigInformatieObjectPost.verzenddatum != null && zgwEnkelvoudigInformatieObjectPost.verzenddatum.length() == 0) {
			zgwEnkelvoudigInformatieObjectPost.verzenddatum = null;
		}
		// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
		// 		Move code to the ModelMapperConfig.java
		if(zgwEnkelvoudigInformatieObjectPost.taal != null && zgwEnkelvoudigInformatieObjectPost.taal.length() == 2) {
			debugWarning("taal only had 2, expected 3 characted, trying to convert: '" + zgwEnkelvoudigInformatieObjectPost.taal  + "'");
			// https://nl.wikipedia.org/wiki/Lijst_van_ISO_639-codes
			switch (zgwEnkelvoudigInformatieObjectPost.taal.toLowerCase()) {
			case "fy":
				// Fryslân boppe!
				zgwEnkelvoudigInformatieObjectPost.taal = "fry";
				break;
			case "nl":
				zgwEnkelvoudigInformatieObjectPost.taal = "nld";
				break;
			case "en":
				zgwEnkelvoudigInformatieObjectPost.taal = "eng";
				break;
			default:
				debugWarning("could not convert: '" + zgwEnkelvoudigInformatieObjectPost.taal.toLowerCase()  + "', this will possible result in an error");
			}
		}

		zgwEnkelvoudigInformatieObjectPost.indicatieGebruiksrecht = "false";

		if(zgwEnkelvoudigInformatieObjectPost.status != null) {
			/*
			in_bewerking - (In bewerking) Aan het informatieobject wordt nog gewerkt.
			ter_vaststelling - (Ter vaststelling) Informatieobject gereed maar moet nog vastgesteld worden.
			definitief - (Definitief) Informatieobject door bevoegd iets of iemand vastgesteld dan wel ontvangen.
			gearchiveerd - (Gearchiveerd) Informatieobject duurzaam bewaarbaar gemaakt; een gearchiveerd informatie-element.
			*/
			zgwEnkelvoudigInformatieObjectPost.status = zgwEnkelvoudigInformatieObjectPost.status.replace(" ", "_");
			zgwEnkelvoudigInformatieObjectPost.status = zgwEnkelvoudigInformatieObjectPost.status.toLowerCase();
			if(!List.of("in_bewerking", "ter_vaststelling", "definitief", "gearchiveerd").contains(zgwEnkelvoudigInformatieObjectPost.status)) {
				debugWarning("document-status: '" + zgwEnkelvoudigInformatieObjectPost.status + "', resetting to null (possible values: in_bewerking / ter_vaststelling / definitief / gearchiveerd)");
				zgwEnkelvoudigInformatieObjectPost.status = null;
			}
		}
		if(StringUtils.isEmpty(zgwEnkelvoudigInformatieObjectPost.titel)) {
			debugWarning("Titel is empty, using the bestandsnaam ["+zgwEnkelvoudigInformatieObjectPost.bestandsnaam+"] as titel");
			zgwEnkelvoudigInformatieObjectPost.titel = zgwEnkelvoudigInformatieObjectPost.bestandsnaam;
		}
		ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient.addZaakDocument(authorization, zgwEnkelvoudigInformatieObjectPost);
		ZgwZaakInformatieObject zgwZaakInformatieObject = addZaakInformatieObject(authorization, zgwEnkelvoudigInformatieObject, zgwZaak.url);

		// status
		if (zdsInformatieObject.isRelevantVoor.volgnummer != null
				&& zdsInformatieObject.isRelevantVoor.omschrijving != null
				&& zdsInformatieObject.isRelevantVoor.omschrijving.length() > 0
				&& zdsInformatieObject.isRelevantVoor.datumStatusGezet != null) {
			log.debug("Update of zaakid:" + zgwZaak.identificatie + " has  status changes");
			var zgwStatusType = this.zgwClient.getStatusTypeByZaakTypeAndOmschrijving(authorization, zgwZaakType,
					zdsInformatieObject.isRelevantVoor.omschrijving, zdsInformatieObject.isRelevantVoor.volgnummer);
			// ZgwStatus zgwStatus = modelMapper.map(zdsHeeft, ZgwStatus.class);
			ZgwStatus zgwStatus = new ZgwStatus();
			zgwStatus.zaak = zgwZaak.url;
			zgwStatus.statustype = zgwStatusType.url;
			this.zgwClient.addZaakStatus(authorization, zgwStatus);
		}

		return zgwEnkelvoudigInformatieObject;
	}

	public ZgwZaakInformatieObject addZaakInformatieObject(ZgwAuthorization authorization, ZgwEnkelvoudigInformatieObject doc, String zaakUrl) {
		var zgwZaakInformatieObject = new ZgwZaakInformatieObject();
		zgwZaakInformatieObject.setZaak(zaakUrl);
		zgwZaakInformatieObject.setInformatieobject(doc.getUrl());
		zgwZaakInformatieObject.setTitel(doc.getTitel());
		return this.zgwClient.addDocumentToZaak(authorization, zgwZaakInformatieObject);
	}

	public ZdsZaakDocumentInhoud getZaakDocumentLezen(
			ZgwAuthorization authorization, String documentIdentificatie) {
		log.debug("getZaakDocumentLezen:" + documentIdentificatie);
		ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient
				.getZgwEnkelvoudigInformatieObjectByIdentiticatie(authorization, documentIdentificatie);
		if (zgwEnkelvoudigInformatieObject == null) {
			throw new ConverterException(
					"ZgwEnkelvoudigInformatieObject #" + documentIdentificatie + " could not be found");
		}
		ZgwInformatieObjectType documenttype = this.zgwClient
				.getZgwInformatieObjectTypeByUrl(authorization, zgwEnkelvoudigInformatieObject.informatieobjecttype);
		if (documenttype == null) {
			throw new ConverterException("getZgwInformatieObjectType #"
					+ zgwEnkelvoudigInformatieObject.informatieobjecttype + " could not be found");
		}
		var zgwZaakInformatieObject = this.zgwClient
				.getZgwZaakInformatieObjectByEnkelvoudigInformatieObjectUrl(authorization, zgwEnkelvoudigInformatieObject.getUrl());
		if (zgwZaakInformatieObject == null) {
			throw new ConverterException("getZgwZaakInformatieObjectByUrl #" + zgwEnkelvoudigInformatieObject.getUrl()
					+ " could not be found");
		}
		var zgwZaak = this.zgwClient.getZaakByUrl(authorization, zgwZaakInformatieObject.getZaak());
		if (zgwZaak == null) {
			throw new ConverterException("getZaakByUrl #" + zgwZaakInformatieObject.getZaak() + " could not be found");
		}
		String inhoud = this.zgwClient.getBas64Inhoud(authorization, zgwEnkelvoudigInformatieObject.getInhoud());
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
		if (result.beschrijving != null && result.beschrijving.length() == 0) {
			result.beschrijving = null;
		}
		if (result.versie != null && result.versie.length() == 0) {
			result.versie = null;
		}
		if (result.taal != null && result.taal.length() == 0) {
			result.taal = null;
		}
		if (result.status != null && result.status.length() == 0) {
			result.status = null;
		}
		if(result.vertrouwelijkAanduiding != null && result.vertrouwelijkAanduiding.length()==0) {
			result.vertrouwelijkAanduiding=null;
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

	public ZgwZaak actualiseerZaakstatus(ZgwAuthorization authorization, ZdsZaak wasZaak, ZdsZaak wordtZaak) {
		log.debug("actualiseerZaakstatus:" + wordtZaak.identificatie);
		var zaakid = wordtZaak.identificatie;
		ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(authorization, zaakid);
		if (zgwZaak == null) {
			throw new ConverterException("Zaak with identification: '" + wordtZaak.identificatie + "' not found in ZGW");
		}
		ZgwZaakType zgwZaakType = this.zgwClient.getZaakTypeByZaak(authorization, zgwZaak);
		
		ChangeDetector changeDetector = new ChangeDetector();
		ZdsZaak zdsStored = this.modelMapper.map(zgwZaak, ZdsZaak.class);
		
		if(wasZaak != null) {
			var storedVsWasChanges = changeDetector.detect(zdsStored, wasZaak);
			var storedVsWasFieldsChanges = storedVsWasChanges.getAllChangesByDeclaringClassAndFilter(ZdsZaak.class, ZdsRol.class);
			if (storedVsWasFieldsChanges.size() > 0) {
				log.debug("Update of zaakid:" + wasZaak.identificatie + " has # " + storedVsWasFieldsChanges.size() + " field changes between stored and was");
				for (Change change : storedVsWasFieldsChanges.keySet()) {
					debugWarning("The field: " + change.getField().getName() + " does not match (" + change.getChangeType() + ") stored-value:'" + change.getCurrentValue()  + "' , was-value:'" + change.getNewValue() + "'");
				}
			}
		}
		else {
			// when there was no "was" provided
			wasZaak = zdsStored;
		}
		setResultaatAndStatus(authorization, wordtZaak, zgwZaak, zgwZaakType);
		return zgwZaak;		
	}

	public List<ZdsZaak> getZaakDetailsByBsn(ZgwAuthorization authorization, String bsn) {
		log.debug("getZaakDetailsByBsn:" + bsn);
		
		var zgwZaken = this.zgwClient.getZakenByBsn(authorization, bsn);
		var result = new ArrayList<ZdsZaak>();
		for (ZgwZaak zgwZaak : zgwZaken) {
			List<String> rollen = zgwZaak.getRollen();
			for(String rolUrl: rollen) {
				ZgwRol zgwRol = this.zgwClient.getRolByUrl(authorization, rolUrl);
				ZgwRolType zgwRolType = this.zgwClient.getRolTypeByUrl(authorization, zgwRol.roltype);
				ZgwRolOmschrijving zgwRolOmschrijving = this.configService.getConfiguration().getZgwRolOmschrijving();
				if (zgwRolType.omschrijving.equals(zgwRolOmschrijving.getHeeftAlsInitiator())) {
					// TODO: hier minder overhead: hier wordt nu 2 keer achterelkaar een getzaak op ZgwRegistry gedaan!
					result.add(getZaakDetailsByZgwZaak(authorization, zgwZaak));
				}
				if(result.size() >= 20) {
					// Max 20 results, it seems we get get unpredicted results after that
					debugWarning("Limit activated, no more than 20 results! (total amound found: " + zgwZaken.size() + " relations)");
					break;
				}				
			}			
		}
		return result;
	}

	
	public ZdsZaak getZaakDetailsByIdentificatie(ZgwAuthorization authorization, String zaakidentificatie) {
		log.debug("getZaakDetailsByIdentificatie:" + zaakidentificatie);
		var zgwZaak = this.zgwClient.getZaakByIdentificatie(authorization, zaakidentificatie);
		if (zgwZaak == null) {
			throw new ConverterException("Zaak not found for identification: '" + zaakidentificatie + "'");
		}		
		return getZaakDetailsByZgwZaak(authorization, zgwZaak);
	}
	private ZdsZaak getZaakDetailsByZgwZaak(ZgwAuthorization authorization, ZgwZaak zgwZaak) {	
		var zgwZaakType = this.zgwClient.getZaakTypeByZaak(authorization, zgwZaak);

		//ZdsZaak zaak = new ZdsZaak();
		ZdsZaak zaak = this.modelMapper.map(zgwZaak, ZdsZaak.class);
		ZgwRolOmschrijving zgwRolOmschrijving = this.configService.getConfiguration().getZgwRolOmschrijving();

		List<ZgwRol> rollen = null;
		if(zgwZaak._expand !=null &&  zgwZaak.rollen != null) {
			rollen = new ArrayList<ZgwRol>();
			for (String rolUrl : zgwZaak.rollen) {
				ZgwRol zgwRol = this.zgwClient.getRolByUrl(authorization, rolUrl);
				rollen.add(zgwRol);
			}
		}
		else {
			rollen = this.zgwClient.getRollenByZaakUrl(authorization, zgwZaak.url);
		}
		for (ZgwRol zgwRol : rollen) {
			var rolGeconverteerd = false;

			if (zgwRolOmschrijving.getHeeftBetrekkingOp().equalsIgnoreCase(zgwRol.getOmschrijving())) {							
				ZdsRol zdsRol = this.modelMapper.map(zgwRol, ZdsRol.class);
				zdsRol.setEntiteittype("ZAKOBJ");
				zaak.heeftBetrekkingOp = zdsRol;
				rolGeconverteerd = true;
			}
			if (zgwRolOmschrijving.getHeeftAlsBelanghebbende().equalsIgnoreCase(zgwRol.getOmschrijving())) {
				ZdsRol zdsRol = this.modelMapper.map(zgwRol, ZdsRol.class);
				zdsRol.setEntiteittype("ZAKBTRBLH");
				zaak.heeftAlsBelanghebbende = zdsRol;
				rolGeconverteerd = true;
			}
			if (zgwRolOmschrijving.getHeeftAlsInitiator().equalsIgnoreCase(zgwRol.getOmschrijving())) {
				ZdsRol zdsRol = this.modelMapper.map(zgwRol, ZdsRol.class);
				zdsRol.setEntiteittype("ZAKBTRINI");
				zaak.heeftAlsInitiator = zdsRol;
				rolGeconverteerd = true;				
			}
			if (zgwRolOmschrijving.getHeeftAlsUitvoerende().equalsIgnoreCase(zgwRol.getOmschrijving())) {
				ZdsRol zdsRol = this.modelMapper.map(zgwRol, ZdsRol.class);
				zdsRol.setEntiteittype("ZAKBTRUTV");
				zaak.heeftAlsUitvoerende = zdsRol;
				rolGeconverteerd = true;				
			}
			if (zgwRolOmschrijving.getHeeftAlsVerantwoordelijke().equalsIgnoreCase(zgwRol.getOmschrijving())) {
				ZdsRol zdsRol = this.modelMapper.map(zgwRol, ZdsRol.class);
				zdsRol.setEntiteittype("ZAKBTRVRA");
				zaak.heeftAlsVerantwoordelijke = zdsRol;
				rolGeconverteerd = true;
				
			}
			if (zgwRolOmschrijving.getHeeftAlsGemachtigde().equalsIgnoreCase(zgwRol.getOmschrijving())) {
				ZdsRol zdsRol = this.modelMapper.map(zgwRol, ZdsRol.class);
				zdsRol.setEntiteittype("ZAKBTRGMC");
				zaak.heeftAlsGemachtigde = zdsRol;
				rolGeconverteerd = true;
				
			}
			if (zgwRolOmschrijving.getHeeftAlsOverigBetrokkene().equalsIgnoreCase(zgwRol.getOmschrijving())) {
				ZdsRol zdsRol = this.modelMapper.map(zgwRol, ZdsRol.class);
				zdsRol.setEntiteittype("ZAKBTROVR");
				zaak.heeftAlsOverigBetrokkene = zdsRol;
				rolGeconverteerd = true;
				
			}
			if (!rolGeconverteerd) {
				debugWarning("Rol: " +  zgwRol.getOmschrijving() + " (" +  zgwRol.getOmschrijvingGeneriek() + ") niet geconverteerd worden ("+ zgwRol.uuid + ")");
			}
		}
		zaak.isVan = new ZdsVan();
		zaak.isVan.entiteittype = "ZAKZKT";
		zaak.isVan.gerelateerde = new ZdsGerelateerde();
		zaak.isVan.gerelateerde.entiteittype = "ZKT";

		zaak.isVan.gerelateerde.code = zgwZaakType.identificatie;
		zaak.isVan.gerelateerde.omschrijving = zgwZaakType.omschrijving;

		if (zgwZaak.getKenmerken() != null && !zgwZaak.getKenmerken().isEmpty()) {
			zaak.kenmerk = new ArrayList<>();
			for (ZgwKenmerk zgwKenmerk : zgwZaak.getKenmerken()) {
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

		if(zaak.verlenging != null && zaak.verlenging.duur != null) {
			zaak.verlenging.duur = zaak.verlenging.duur.replace("P", "").replace("Y", "").replace("M", "").replace("D", "");
		}

		// heefd deze zaak ook een hoofdzaak
		var hoofdzaak = zgwZaak.getHoofdzaak();
		if(hoofdzaak != null) {
			var parentzaak = zgwClient.getZaakByUrl(authorization, hoofdzaak);
			zaak.heeftAlsHoofdzaak = new ZdsHeeftGerelateerde();
			zaak.heeftAlsHoofdzaak.entiteittype = "ZAKZAKBTR";
			zaak.heeftAlsHoofdzaak.gerelateerde = new ZdsGerelateerde();
			zaak.heeftAlsHoofdzaak.gerelateerde.entiteittype = "ZAK";
			zaak.heeftAlsHoofdzaak.gerelateerde.identificatie = parentzaak.identificatie;
			zaak.heeftAlsHoofdzaak.gerelateerde.omschrijving = parentzaak.omschrijving;
		}

		// heeft deze zaak ook deelzaken
		var deelzaken = zgwZaak.getDeelzaken();
		for(String deelzaak: deelzaken) {
			if(zaak.heeftAlsDeelzaak == null) {
				zaak.heeftAlsDeelzaak = new ArrayList<ZdsHeeftGerelateerde>();
			}
			var childzaak = zgwClient.getZaakByUrl(authorization, deelzaak);
			var heeftGerelateerde = new ZdsHeeftGerelateerde();
			heeftGerelateerde.entiteittype = "ZAKZAKBTR";
			heeftGerelateerde.gerelateerde = new ZdsGerelateerde();
			heeftGerelateerde.gerelateerde.entiteittype = "ZAK";
			heeftGerelateerde.gerelateerde.identificatie = childzaak.identificatie;
			heeftGerelateerde.gerelateerde.omschrijving = childzaak.omschrijving;
			zaak.heeftAlsDeelzaak.add(heeftGerelateerde);
		}

		// heeft deze zaak ook gerelateerde zaken
		var relevanteAndereZaken = zgwZaak.getRelevanteAndereZaken();
		for(ZgwAndereZaak relevanteAndereZaak: relevanteAndereZaken) {
			if(zaak.heeftBetrekkingOpAndere == null) {
				zaak.heeftBetrekkingOpAndere = new ArrayList<ZdsHeeftGerelateerde>();
			}
			var childzaak = zgwClient.getZaakByUrl(authorization, relevanteAndereZaak.url);
			var heeftGerelateerde = new ZdsHeeftGerelateerde();
			heeftGerelateerde.entiteittype = "ZAKZAKBTR";
			heeftGerelateerde.gerelateerde = new ZdsGerelateerde();
			heeftGerelateerde.gerelateerde.entiteittype = "ZAK";
			heeftGerelateerde.gerelateerde.identificatie = childzaak.identificatie;
			heeftGerelateerde.gerelateerde.omschrijving = childzaak.omschrijving;
			zaak.heeftBetrekkingOpAndere.add(heeftGerelateerde);
		}
		
		var zdsStatussen = new ArrayList<ZdsHeeft>();
		for (ZgwStatus zgwStatus : this.zgwClient.getStatussenByZaakUrl(authorization, zgwZaak.url)) {
			ZgwStatusType zgwStatusType = this.zgwClient.getResource(authorization, zgwStatus.statustype, ZgwStatusType.class);
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

	private ZgwZaakType getZaakTypeByUrl(ZgwAuthorization authorization, String url) {
		var zaakype = this.zgwClient.getZaakTypes(authorization, null).stream().filter(zgwZaakType -> zgwZaakType.url.equalsIgnoreCase(url)).findFirst().orElse(null);
		if(zaakype == null) {
			throw new ConverterException("Zaaktype met url:" + url + " niet gevonden!");
		}
		return zaakype;
	}
	
	private ZdsRol getZdsRol(ZgwAuthorization authorization, ZgwZaak zgwZaak, ZgwZaakType zgwZaakType, String rolOmschrijving, String entiteittype) {
		var zgwRolType = this.zgwClient.getRolTypeByZaaktypeAndOmschrijving(authorization, zgwZaakType, rolOmschrijving);
		ZgwRol zgwRol = this.zgwClient.getRolByZaakUrlAndRolTypeUrl(authorization, zgwZaak.url, zgwRolType.url);
		if (zgwRol == null) {
			// geen rol voor deze
			return null;
		}
		ZdsRol zdsRol = this.modelMapper.map(zgwRol, ZdsRol.class);
		zdsRol.setEntiteittype(entiteittype);
		return zdsRol;
	}

	private void updateRolInZgw(ZgwAuthorization authorization, ZgwZaak zgwZaak, ZgwZaakType zgwZaakType, String typeRolOmschrijving, ZdsRol newValue) {
		log.debug("updateRolInZgw Rol:" + typeRolOmschrijving);

		// no put action for rollen, so first delete then add
		log.debug("Attempting to update rol by deleting and adding as new");
		deleteRolFromZgw(authorization, zgwZaak, zgwZaakType, typeRolOmschrijving);

		if(newValue.gerelateerde == null) {
			log.debug("Not adding the rol:"  + typeRolOmschrijving + ", gerelateerde == null ");
			return;
		}

		if(typeRolOmschrijving == null) {
			debugWarning("Not adding the rol, typeRolOmschrijving == null ");
			return;
		}

		addRolToZgw(authorization, zgwZaak, zgwZaakType, newValue, typeRolOmschrijving);
	}

	private void deleteRolFromZgw(ZgwAuthorization authorization, ZgwZaak zgwZaak, ZgwZaakType zgwZaakType, String typeRolOmschrijving) {
		log.debug("deleteRolFromZgw Rol:" + typeRolOmschrijving);

		var roltype = this.zgwClient.getRolTypeByZaaktypeAndOmschrijving(authorization, zgwZaakType, typeRolOmschrijving);
		if (roltype == null) {
			// throw new ConverterException("Roltype: " + typeRolOmschrijving + " niet gevonden bij zaaktype voor zaak: " + zgwZaak.identificatie);
			debugWarning("Roltype: " + typeRolOmschrijving + " niet gevonden bij zaaktype voor zaak: " + zgwZaak.identificatie);
			return;
		}
		ZgwRol rol = this.zgwClient.getRolByZaakUrlAndRolTypeUrl(authorization, zgwZaak.url, roltype.url);
		if (rol == null) {
			//throw new ConverterException("Rol: " + typeRolOmschrijving + " niet gevonden bij zaak: " + zgwZaak.identificatie);
			debugWarning("Rol: " + typeRolOmschrijving + " niet gevonden bij zaaktype voor zaak: " + zgwZaak.identificatie);
			return;

		}
		this.zgwClient.deleteRol(authorization, rol.uuid);
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
		case "heeftalsoverigBetrokkene":
			return zgwRolOmschrijving.getHeeftAlsOverigBetrokkene();
		case "heeftbetrekkingop":
			return zgwRolOmschrijving.getHeeftBetrekkingOp();
		default:
			return null;
		}
	}

	public String checkOutZaakDocument(ZgwAuthorization authorization, String documentIdentificatie) {
		log.debug("checkOutZaakDocument:" + documentIdentificatie);
		ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient.getZgwEnkelvoudigInformatieObjectByIdentiticatie(authorization, documentIdentificatie);
		if (zgwEnkelvoudigInformatieObject == null) {
			throw new ConverterException("ZgwEnkelvoudigInformatieObject #" + documentIdentificatie + " could not found" );
		}
		if(zgwEnkelvoudigInformatieObject.locked) {
			throw new ConverterException("ZgwEnkelvoudigInformatieObject #" + zgwEnkelvoudigInformatieObject.identificatie + " IS ALREADY LOCKED and cannot be changed");
		}

		ZgwLock lock = this.zgwClient.getZgwInformatieObjectLock(authorization, zgwEnkelvoudigInformatieObject);
		log.debug("received lock:" + lock.lock);
		return lock.lock;
	}

	public Object cancelCheckOutZaakDocument(ZgwAuthorization authorization, String documentIdentificatie, String lock) {
		log.debug("checkOutZaakDocument:" + documentIdentificatie);
		ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient
				.getZgwEnkelvoudigInformatieObjectByIdentiticatie(authorization, documentIdentificatie);
		if (zgwEnkelvoudigInformatieObject == null) {
			throw new ConverterException(
					"ZgwEnkelvoudigInformatieObject #" + documentIdentificatie + " could not be found");
		}
		ZgwLock zgwLock = new ZgwLock();
		zgwLock.lock = lock;
		this.zgwClient.getZgwInformatieObjectUnLock(authorization, zgwEnkelvoudigInformatieObject, zgwLock);
		return null;
	}

	public ZgwEnkelvoudigInformatieObject updateZaakDocument(ZgwAuthorization authorization, String lock, ZdsZaakDocumentInhoud zdsWasInformatieObject, ZdsZaakDocumentInhoud zdsWordtInformatieObject) {
		log.debug("updateZaakDocument lock:" + lock + " informatieobject:" + zdsWasInformatieObject.identificatie);

		var zgwWasEnkelvoudigInformatieObject = this.zgwClient.getZgwEnkelvoudigInformatieObjectByIdentiticatie(authorization, zdsWasInformatieObject.identificatie);
		if("definitief".equals(zgwWasEnkelvoudigInformatieObject.status)) {
			throw new ConverterException("ZgwEnkelvoudigInformatieObject #: " + zdsWasInformatieObject.identificatie + " has status 'defintief ' and therefore cannot be locked and then changed");
		}


		// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
		// 		Move code to the ModelMapperConfig.java
		//		Also merge, we shouldnt overwrite the old values this hard
		var zgwWordtEnkelvoudigInformatieObjectPut = this.modelMapper.map(zdsWordtInformatieObject, ZgwEnkelvoudigInformatieObjectPut.class);
		zgwWordtEnkelvoudigInformatieObjectPut.bronorganisatie = zgwWasEnkelvoudigInformatieObject.bronorganisatie;
		zgwWordtEnkelvoudigInformatieObjectPut.informatieobjecttype = zgwWasEnkelvoudigInformatieObject.informatieobjecttype;
		// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
		// 		Move code to the ModelMapperConfig.java
		if(zgwWordtEnkelvoudigInformatieObjectPut.verzenddatum != null && zgwWordtEnkelvoudigInformatieObjectPut.verzenddatum.length() == 0) {
			zgwWordtEnkelvoudigInformatieObjectPut.verzenddatum = null;
		}
		// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
		// 		Move code to the ModelMapperConfig.java
		if(zgwWordtEnkelvoudigInformatieObjectPut.taal != null && zgwWordtEnkelvoudigInformatieObjectPut.taal.length() == 2) {
			debugWarning("taal only had 2, expected 3 characted, trying to convert: '" + zgwWordtEnkelvoudigInformatieObjectPut.taal  + "'");
			// https://nl.wikipedia.org/wiki/Lijst_van_ISO_639-codes
			switch (zgwWordtEnkelvoudigInformatieObjectPut.taal.toLowerCase()) {
			case "fy":
				// Fryslân boppe!
				zgwWordtEnkelvoudigInformatieObjectPut.taal = "fry";
				break;
			case "nl":
				zgwWordtEnkelvoudigInformatieObjectPut.taal = "nld";
				break;
			case "en":
				zgwWordtEnkelvoudigInformatieObjectPut.taal = "eng";
				break;
			default:
				debugWarning("could not convert: '" + zgwWordtEnkelvoudigInformatieObjectPut.taal.toLowerCase()  + "', this will possible result in an error");
			}
		}
		zgwWordtEnkelvoudigInformatieObjectPut.indicatieGebruiksrecht = "false";
		if(zgwWordtEnkelvoudigInformatieObjectPut.status != null) {
			/*
			in_bewerking - (In bewerking) Aan het informatieobject wordt nog gewerkt.
			ter_vaststelling - (Ter vaststelling) Informatieobject gereed maar moet nog vastgesteld worden.
			definitief - (Definitief) Informatieobject door bevoegd iets of iemand vastgesteld dan wel ontvangen.
			gearchiveerd - (Gearchiveerd) Informatieobject duurzaam bewaarbaar gemaakt; een gearchiveerd informatie-element.
			*/
			zgwWordtEnkelvoudigInformatieObjectPut.status = zgwWordtEnkelvoudigInformatieObjectPut.status.replace(" ", "_");
			zgwWordtEnkelvoudigInformatieObjectPut.status = zgwWordtEnkelvoudigInformatieObjectPut.status.toLowerCase();
			if(!List.of("in_bewerking", "ter_vaststelling", "definitief", "gearchiveerd").contains(zgwWordtEnkelvoudigInformatieObjectPut.status)) {
				debugWarning("document-status: '" + zgwWordtEnkelvoudigInformatieObjectPut.status + "', resetting to null (possible values: in_bewerking / ter_vaststelling / definitief / gearchiveerd)");
				zgwWordtEnkelvoudigInformatieObjectPut.status = null;
			}
		}

		zgwWordtEnkelvoudigInformatieObjectPut.lock = lock;
		zgwWordtEnkelvoudigInformatieObjectPut.url = zgwWasEnkelvoudigInformatieObject.url;
		zgwWasEnkelvoudigInformatieObject = this.zgwClient.putZaakDocument(authorization, zgwWordtEnkelvoudigInformatieObjectPut);
		//ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(zdsInformatieObject.isRelevantVoor.gerelateerde.identificatie);
		//ZgwZaakInformatieObject zgwZaakInformatieObject = addZaakInformatieObject(zgwEnkelvoudigInformatieObject, zgwZaak.url);


		ZgwLock zgwLock = new ZgwLock();
		zgwLock.lock = lock;
		this.zgwClient.getZgwInformatieObjectUnLock(authorization, zgwWordtEnkelvoudigInformatieObjectPut, zgwLock);

		return zgwWasEnkelvoudigInformatieObject;
	}

	private void debugWarning(String message) {
		log.info("[processing warning] " + message);
		debug.infopoint("Warning", message);
	}
}