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
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsGerelateerde;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsHeeft;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsHeeftGerelateerde;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsHeeftRelevant;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsInhoud;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsIsRelevantVoor;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsKenmerk;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsOpschorting;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsRol;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsVan;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsVerlenging;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaak;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocument;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocumentInhoud;
import nl.haarlem.translations.zdstozgw.translation.zgw.client.ZGWClient;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwAdres;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwAndereZaak;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwBetrokkeneIdentificatie;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwInformatieObjectType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwKenmerk;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwLock;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwResultaat;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwRol;
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
					ZgwZaak zgwAndereZaak= this.zgwClient.getZaakByIdentificatie(heeftBetrekkingOpAndere.gerelateerde.identificatie);
					if (zgwAndereZaak == null) {
							// throw new ConverterException("Zaak with identification '" + heeftBetrekkingOpAndere.gerelateerde.identificatie + "' not found in ZGW");
						debugWarning("Setting heeftBetrekkingOpAndere, zaak: '" + heeftBetrekkingOpAndere.gerelateerde.identificatie + "' not found in ZGW (referenced from within zaak:" + zdsZaak.identificatie + ")");
					}
					else {
						//	bijdrage	een zaak van het ZAAKTYPE levert een bijdrage aan het bereiken van de uitkomst van een zaak van het andere ZAAKTYPE
						//	onderwerp	een zaak van het ZAAKTYPE heeft betrekking op een zaak van het andere ZAAKTYPE of een zaak van het andere ZAAKTYPE is relevant voor of is onderwerp van een zaak van het ZAAKTYPE
						// 	vervolg		een zaak van het ZAAKTYPE is een te plannen vervolg op een zaak van het andere ZAAKTYPE
						
						// we need to make the relation to both sides (to get the expected behaviour)
						zgwClient.addRelevanteAndereZaakToZaak(zgwZaak, zgwAndereZaak, "onderwerp");
						zgwClient.addRelevanteAndereZaakToZaak(zgwAndereZaak, zgwZaak, "onderwerp"); 
					}
				}	
			}
		}
		
		// last, the resultaat and status (lastly, since it things go wrong, it is often over here) 
		setResultaatAndStatus(zdsZaak, zgwZaak, zgwZaakType);		
		
		return zgwZaak;
	}

	public void updateZaak(ZdsZaak zdsWordtZaak) {
		var zdsWasZaak = getZaakDetailsByIdentificatie(zdsWordtZaak.getIdentificatie());
		updateZaak(zdsWasZaak, zdsWordtZaak);
	}
	
	public void updateZaak(ZdsZaak zdsWasZaak, ZdsZaak zdsWordtZaak) {
		log.debug("updateZaak:" + zdsWordtZaak.identificatie);
		ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(zdsWordtZaak.identificatie);
		if (zgwZaak == null) {
			throw new ConverterException("Zaak with identification: '" + zdsWordtZaak.identificatie + "' not found in ZGW");
		}
		ZgwZaakType zgwZaakType = this.zgwClient.getZaakTypeByZaak(zgwZaak);

		var changed = false;
		ChangeDetector changeDetector = new ChangeDetector();

		// check if the zdsWasZaak is equal to the one stored inside OpenZaak
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
				// ZgwZaakPut zgwWordtZaak = this.modelMapper.map(zdsWordtZaak, ZgwZaakPut.class);
				// ZgwZaakPut updatedZaak = ZgwZaakPut.merge(zgwZaak, zgwWordtZaak);
				// this.zgwClient.updateZaak(zgwZaak.uuid, updatedZaak);
				// changed = true;
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
				// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
				// 		Move code to the ModelMapperConfig.java
				if("verlenging".equals(change.getField().getName())) {
					if(change.getCurrentValue() != null) {
						var verlenging  = (nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsVerlenging) change.getCurrentValue();
						verlenging.duur = "P" + verlenging.duur + "D";
					}
					if(change.getNewValue() != null) {
						var verlenging  = (nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsVerlenging) change.getNewValue();
						verlenging.duur = "P" + verlenging.duur + "D";
					}
				}
					
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

//			for(ChangeDetector.Change change : wasVsWordtRolChanges.keySet()) {
//				log.info("Rol field:" + change.getField().getName() + " changetype:" + change.getChangeType() + " currentvalue:" + change.getCurrentValue() + " newvalue:" + change.getNewValue());				
//			}
			
			changeDetector.filterChangesByType(wasVsWordtRolChanges, ChangeDetector.ChangeType.NEW)
					.forEach((change, changeType) -> {
						var rolnaam = getRolOmschrijvingGeneriekByRolName(change.getField().getName());
						log.debug("[CHANGE ROL] New Rol:" + rolnaam);
						addRolToZgw(zgwZaak, zgwZaakType, (ZdsRol) change.getNewValue(), rolnaam);
					});

			changeDetector.filterChangesByType(wasVsWordtRolChanges, ChangeDetector.ChangeType.DELETED)
					.forEach((change, changeType) -> {
						var rolnaam = getRolOmschrijvingGeneriekByRolName(change.getField().getName());
						if(rolnaam != null) {
							log.debug("[CHANGE ROL] Deleted Rol:" + rolnaam);

							deleteRolFromZgw(zgwZaak, zgwZaakType, rolnaam);
						}
					});

			changeDetector.filterChangesByType(wasVsWordtRolChanges, ChangeDetector.ChangeType.CHANGED)
					.forEach((change, changeType) -> {
						var rolnaam = getRolOmschrijvingGeneriekByRolName(change.getField().getName());
						log.debug("[CHANGE ROL] Update Rol:" + rolnaam);
						updateRolInZgw(zgwZaak, zgwZaakType, rolnaam, (ZdsRol) change.getNewValue());
					});
			changed = true;
		}

		boolean hasChanged = setResultaatAndStatus(zdsWordtZaak, zgwZaak, zgwZaakType);

		if (!changed && ! hasChanged) {
			debugWarning("Update of zaakid:" + zdsWasZaak.identificatie + " without any changes");
		}
	}

	private String convertZdsStatusDatumtoZgwDateTime(ZgwZaak zgwZaak, String zdsStatusDatum) {
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
			int index = this.zgwClient.getStatussenByZaakUrl(zgwZaak.url).size();
			zgwStatusDatumTijd = (ModelMapperConfig.convertStufDateTimeToZgwDateTime(zdsStatusDatum, index));
		}
		return zgwStatusDatumTijd;
	}
	
	public boolean setResultaatAndStatus(ZdsZaak zdsZaak, ZgwZaak zgwZaak, ZgwZaakType zgwZaakType) {
		var changed = false;
		var beeindigd = false;

		// if there is a resultaat		
		if (zdsZaak.resultaat != null && zdsZaak.resultaat.omschrijving != null && zdsZaak.resultaat.omschrijving.length() > 0) {
			var resultaatomschrijving = zdsZaak.resultaat.omschrijving;
			log.debug("Update of zaakid:" + zdsZaak.identificatie + " wants resultaat to be changed to:" + resultaatomschrijving );
			var zgwResultaatType = this.zgwClient.getResultaatTypeByZaakTypeAndOmschrijving(zgwZaakType, resultaatomschrijving);
			if(zgwResultaatType == null) {
				throw new ConverterException("Resultaattype: " + resultaatomschrijving + " niet gevonden bij Zaaktype: " + zgwZaakType.identificatie);							
			}
			
			var resultaten = this.zgwClient.getResultatenByZaakUrl(zgwZaak.url);

			// remove any existing resultaten (we only want to have 1)
			for (ZgwResultaat resultaat : resultaten) {
				debugWarning("Zaak with identificatie:" + zdsZaak.identificatie + " already has resultaat #" + resultaten.indexOf(resultaat) + " met toelichting:" +  resultaat.toelichting + "(" + resultaat.uuid  + "), will be deleted");
				this.zgwClient.deleteZaakResultaat(resultaat.uuid);
			}
			ZgwResultaat zgwResultaat = new ZgwResultaat();
			zgwResultaat.zaak = zgwZaak.url;
			zgwResultaat.resultaattype = zgwResultaatType.url;
			zgwResultaat.toelichting = zdsZaak.resultaat.omschrijving;
			this.zgwClient.addZaakResultaat(zgwResultaat);
			changed = true;			
		}

		// if there is a status
		if (zdsZaak.heeft != null) {
			var statussen = this.zgwClient.getStatussenByZaakUrl(zgwZaak.url);
			for (ZdsHeeft zdsHeeftIterator : zdsZaak.heeft) {
				ZdsGerelateerde zdsStatus = zdsHeeftIterator.gerelateerde;
				if(zdsStatus != null && zdsStatus.omschrijving != null && zdsStatus.omschrijving.length() > 0) {
					log.debug("Update of zaakid:" + zdsZaak.identificatie + " wants status to be changed to:" + zdsStatus.omschrijving);
					ZgwStatusType zgwStatusType = this.zgwClient.getStatusTypeByZaakTypeAndOmschrijving(zgwZaakType, zdsStatus.omschrijving, zdsStatus.volgnummer);
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
					zgwStatus.setDatumStatusGezet(convertZdsStatusDatumtoZgwDateTime(zgwZaak, zdsStatusDatum));
					// check if the status doesnt exist yet
					var statusexists = false;
					for(ZgwStatus s : statussen) {
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
					if(!statusexists) {
						this.zgwClient.addZaakStatus(zgwStatus);
						changed = true;
					}
				}
				else {
					debugWarning("status has 'heeft' without 'gerelateerde' or  omschrijving");
				}
			}
		}
		
		// beeindigZaakWanneerEinddatum logica
		if(zdsZaak.einddatum != null && zdsZaak.einddatum.length() > 0 && !beeindigd) {
			for(var beeindig : this.configService.getConfiguration().getBeeindigZaakWanneerEinddatum() ) {
				if(beeindig.zaakType.equals(zgwZaakType.getIdentificatie())) {
					// is the result also missing?
					var resultaten = this.zgwClient.getResultatenByZaakUrl(zgwZaak.url);
					if(resultaten.size() == 0) {
						var zgwResultaatType = this.zgwClient.getResultaatTypeByZaakTypeAndOmschrijving(zgwZaakType, beeindig.coalesceResultaat);
						if(zgwResultaatType == null) {
							throw new ConverterException("Resultaattype: '" + beeindig.coalesceResultaat + "' niet gevonden bij Zaaktype:'" + zgwZaakType.identificatie + "'");							
						}
						ZgwResultaat zgwResultaat = new ZgwResultaat();
						zgwResultaat.zaak = zgwZaak.url;
						zgwResultaat.resultaattype = zgwResultaatType.url;
						zgwResultaat.toelichting = zgwResultaatType.omschrijving;
						debugWarning("BeeindigZaakWanneerEinddatum was defined for zaaktype:'" + zgwZaakType.identificatie + "' and an einddatum was provided, no resultaat, zaak will get resultaat:'" + zgwResultaatType.getOmschrijving() + "'");
						this.zgwClient.addZaakResultaat(zgwResultaat);						
					}
								
					var zgwStatusType = this.zgwClient.getLastStatusTypeByZaakType(zgwZaakType);					
					ZgwStatus zgwStatus = new ZgwStatus();
					zgwStatus.zaak = zgwZaak.url;
					zgwStatus.statustype = zgwStatusType.url;
					zgwStatus.statustoelichting = zgwStatusType.omschrijving;
					zgwStatus.setDatumStatusGezet(convertZdsStatusDatumtoZgwDateTime(zgwZaak, zdsZaak.einddatum));
					debugWarning("BeeindigZaakWanneerEinddatum was defined for zaaktype:'" + zgwZaakType.identificatie + "' and an einddatum was provided, no eindstatus, zaak will get status:'" + zgwStatusType.getOmschrijving() + "' with time:" + zgwStatus.getDatumStatusGezet());
					this.zgwClient.addZaakStatus(zgwStatus);
					beeindigd = true;
					changed = true;
				}
			}
		}		
		return changed;
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
						zgwRol.betrokkeneIdentificatie.verblijfsadres = new ZgwAdres();
						zgwRol.betrokkeneIdentificatie.verblijfsadres.aoaIdentificatie = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.identificatie;
						zgwRol.betrokkeneIdentificatie.verblijfsadres.wplWoonplaatsNaam = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.woonplaatsnaam;
						zgwRol.betrokkeneIdentificatie.verblijfsadres.gorOpenbareRuimteNaam = zdsRol.gerelateerde.natuurlijkPersoon.verblijfsadres.straatnaam;
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

			var rechtsvorm = zdsRol.gerelateerde.nietNatuurlijkPersoon.innRechtsvorm.toLowerCase();
			if(rechtsvorm == null || rechtsvorm.length() == 0 ) {
				// do nothing
			} else if(rechtsvorm.contains("vennootschap")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "besloten_vennootschap";
			} else if(rechtsvorm.contains("economische")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "cooperatie_europees_economische_samenwerking";
			} else if(rechtsvorm.contains("cooperatieve")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "europese_cooperatieve_venootschap";
			} else if(rechtsvorm.contains("europese")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "europese_naamloze_vennootschap";
			} else if(rechtsvorm.contains("kerkelijke")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "kerkelijke_organisatie";
			} else if(rechtsvorm.contains("vennootschap")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "naamloze_vennootschap";
			} else if(rechtsvorm.contains("waarborg")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "onderlinge_waarborg_maatschappij";
			} else if(rechtsvorm.contains("privaatrechtelijke")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "overig_privaatrechtelijke_rechtspersoon";
			} else if(rechtsvorm.contains("stichting")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "stichting";
			} else if(rechtsvorm.contains("vereniging")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "vereniging";
			} else if(rechtsvorm.contains("eigenaars")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "vereniging_van_eigenaars";
			} else if(rechtsvorm.contains("publiekrechtelijke")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "publiekrechtelijke_rechtspersoon";
			} else if(rechtsvorm.contains("firma")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "vennootschap_onder_firma";
			} else if(rechtsvorm.contains("maatschap")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "maatschap";
			} else if(rechtsvorm.contains("rederij")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "rederij";
			} else if(rechtsvorm.contains("commanditaire")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "commanditaire_vennootschap";
			} else if(rechtsvorm.contains("binnen")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "kapitaalvennootschap_binnen_eer";
			} else if(rechtsvorm.contains("buitenlandse")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "overige_buitenlandse_rechtspersoon_vennootschap";
			} else if(rechtsvorm.contains("buiten")) {
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "kapitaalvennootschap_buiten_eer";
			} else {
				// maybe a good default?
				debugWarning("Rechtsvorm:" + zdsRol.gerelateerde.nietNatuurlijkPersoon.innRechtsvorm + " kon niet worden geconverteerd, using default value: overig_privaatrechtelijke_rechtspersoon");
				zgwRol.betrokkeneIdentificatie.innRechtsvorm = "overig_privaatrechtelijke_rechtspersoon";
			}
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
		var zgwZaakInformatieObjecten = this.zgwClient.getZaakInformatieObjectenByZaak(zgwZaak.url);
		if(this.zgwClient.additionalCallToRetrieveRelatedObjectInformatieObjectenForCaching && zgwZaakInformatieObjecten.size() > 0) {
			// fill the cache in the drc if needed 
			var zgwObjectInformatieObjecten = this.zgwClient.getObjectInformatieObjectByObject(zgwZaak.url);
			debugWarning("Retrieved ObjectInformatieObjecten to fill the cache on the (CMIS-)DRC (call not needed for ZdsToZgw)");
		}	
		for (ZgwZaakInformatieObject zgwZaakInformatieObject : zgwZaakInformatieObjecten) {
			ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient
					.getZaakDocumentByUrl(zgwZaakInformatieObject.informatieobject);
			if (zgwEnkelvoudigInformatieObject == null || zgwEnkelvoudigInformatieObject.informatieobjecttype == null) {
				throw new ConverterException("could not get the zaakdocument: "
						+ zgwZaakInformatieObject.informatieobject + " for zaak:" + zaakidentificatie);
			}			
			ZgwInformatieObjectType documenttype = this.zgwClient
					.getZgwInformatieObjectTypeByUrl(zgwEnkelvoudigInformatieObject.informatieobjecttype);
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

	public ZgwEnkelvoudigInformatieObject voegZaakDocumentToe(String rsin, ZdsZaakDocumentInhoud zdsInformatieObject) {
		log.debug("voegZaakDocumentToe:" + zdsInformatieObject.identificatie);

		var zaakIdentificatie = zdsInformatieObject.isRelevantVoor.gerelateerde.identificatie;
		ZgwZaak zgwZaak = this.zgwClient.getZaakByIdentificatie(zaakIdentificatie);
		if (zgwZaak == null) {
			throw new ConverterException("Zaak not found for identificatie: " + zaakIdentificatie);
		}
		ZgwZaakType zgwZaakType = this.zgwClient.getZaakTypeByZaak(zgwZaak);

		ZgwInformatieObjectType zgwInformatieObjectType = this.zgwClient.getZgwInformatieObjectTypeByOmschrijving(zgwZaakType, zdsInformatieObject.omschrijving);
		if (zgwInformatieObjectType == null) {
			throw new ConverterException("Documenttype not found for: '" + zdsInformatieObject.omschrijving + "' in zaaktype:" + zgwZaakType.identificatie + " (" + zgwZaakType.omschrijving + ")");
		}


		ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.modelMapper.map(zdsInformatieObject, ZgwEnkelvoudigInformatieObject.class);
		zgwEnkelvoudigInformatieObject.informatieobjecttype = zgwInformatieObjectType.url;
		zgwEnkelvoudigInformatieObject.bronorganisatie = rsin;
		// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
		// 		Move code to the ModelMapperConfig.java
		if(zgwEnkelvoudigInformatieObject.verzenddatum != null && zgwEnkelvoudigInformatieObject.verzenddatum.length() == 0) {
			zgwEnkelvoudigInformatieObject.verzenddatum = null;
		}
		// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
		// 		Move code to the ModelMapperConfig.java
		if(zgwEnkelvoudigInformatieObject.taal != null && zgwEnkelvoudigInformatieObject.taal.length() == 2) {
			debugWarning("taal only had 2, expected 3 characted, trying to convert: '" + zgwEnkelvoudigInformatieObject.taal  + "'");
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
			default:
				debugWarning("could not convert: '" + zgwEnkelvoudigInformatieObject.taal.toLowerCase()  + "', this will possible result in an error");
			}
		}

		zgwEnkelvoudigInformatieObject.indicatieGebruiksrecht = "false";

		if(zgwEnkelvoudigInformatieObject.status != null) {
			/*			
			in_bewerking - (In bewerking) Aan het informatieobject wordt nog gewerkt.
			ter_vaststelling - (Ter vaststelling) Informatieobject gereed maar moet nog vastgesteld worden.
			definitief - (Definitief) Informatieobject door bevoegd iets of iemand vastgesteld dan wel ontvangen.
			gearchiveerd - (Gearchiveerd) Informatieobject duurzaam bewaarbaar gemaakt; een gearchiveerd informatie-element.
			*/			
			zgwEnkelvoudigInformatieObject.status = zgwEnkelvoudigInformatieObject.status.replace(" ", "_");
			zgwEnkelvoudigInformatieObject.status = zgwEnkelvoudigInformatieObject.status.toLowerCase();
			if(!List.of("in_bewerking", "ter_vaststelling", "definitief", "gearchiveerd").contains(zgwEnkelvoudigInformatieObject.status)) {
				debugWarning("document-status: '" + zgwEnkelvoudigInformatieObject.status + "', resetting to null (possible values: in_bewerking / ter_vaststelling / definitief / gearchiveerd)");
				zgwEnkelvoudigInformatieObject.status = null;
			}
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
			if(result.size() >= 20) {
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
				debugWarning("Rol: " +  zgwRol.getOmschrijving() + " (" +  zgwRol.getOmschrijvingGeneriek() + ") niet geconverteerd worden ("+ zgwRol.uuid + ")");
			}
		}
		zaak.isVan = new ZdsVan();
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
		
		if(zaak.verlenging != null && zaak.verlenging.duur != null) {
			zaak.verlenging.duur = zaak.verlenging.duur.replace("P", "").replace("Y", "").replace("M", "").replace("D", "");
		}
		
		// heefd deze zaak ook een hoofdzaak
		var hoofdzaak = zgwZaak.getHoofdzaak();
		if(hoofdzaak != null) {
			var parentzaak = zgwClient.getZaakByUrl(hoofdzaak);
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
			var childzaak = zgwClient.getZaakByUrl(deelzaak);			
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
			var childzaak = zgwClient.getZaakByUrl(relevanteAndereZaak.url);			
			var heeftGerelateerde = new ZdsHeeftGerelateerde();
			heeftGerelateerde.entiteittype = "ZAKZAKBTR";
			heeftGerelateerde.gerelateerde = new ZdsGerelateerde();
			heeftGerelateerde.gerelateerde.entiteittype = "ZAK";
			heeftGerelateerde.gerelateerde.identificatie = childzaak.identificatie;		
			heeftGerelateerde.gerelateerde.omschrijving = childzaak.omschrijving;
			zaak.heeftBetrekkingOpAndere.add(heeftGerelateerde);
		}		
		
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
		if(zaakype == null) {
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

		if(newValue.gerelateerde == null) {
			log.debug("Not adding the rol:"  + typeRolOmschrijving + ", gerelateerde == null ");
			return;
		}

		if(typeRolOmschrijving == null) {
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
		case "heeftalsoverigBetrokkene":
			return zgwRolOmschrijving.getHeeftAlsOverigBetrokkene();
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
		if(zgwEnkelvoudigInformatieObject.locked) {
			throw new ConverterException("ZgwEnkelvoudigInformatieObjectByIdentiticatie with identificatie: " + zgwEnkelvoudigInformatieObject.identificatie + " cannot be locked and then changed");
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
		if("definitief".equals(zgwWasEnkelvoudigInformatieObject.status)) {
			throw new ConverterException("ZgwEnkelvoudigInformatieObjectByIdentiticatie with identificatie: " + zdsWasInformatieObject.identificatie + " cannot be locked and then changed");
		}


		// https://github.com/Sudwest-Fryslan/OpenZaakBrug/issues/54
		// 		Move code to the ModelMapperConfig.java
		//		Also merge, we shouldnt overwrite the old values this hard
		var zgwWordtEnkelvoudigInformatieObject = this.modelMapper.map(zdsWordtInformatieObject, ZgwEnkelvoudigInformatieObject.class);
		if(zgwWordtEnkelvoudigInformatieObject.verzenddatum != null && zgwWordtEnkelvoudigInformatieObject.verzenddatum.length() == 0) {
			zgwWordtEnkelvoudigInformatieObject.verzenddatum = null;
		}
		//zgwEnkelvoudigInformatieObject.indicatieGebruiksrecht = "false";
		zgwWordtEnkelvoudigInformatieObject.bronorganisatie = zgwWasEnkelvoudigInformatieObject.bronorganisatie;
		zgwWordtEnkelvoudigInformatieObject.informatieobjecttype = zgwWasEnkelvoudigInformatieObject.informatieobjecttype;

		//	"in_bewerking" "ter_vaststelling" "definitief" "gearchiveerd"
		zgwWordtEnkelvoudigInformatieObject.status = zgwWordtEnkelvoudigInformatieObject.status.toLowerCase();
		zgwWordtEnkelvoudigInformatieObject.lock = lock;
		zgwWordtEnkelvoudigInformatieObject.url = zgwWasEnkelvoudigInformatieObject.url;
		zgwWasEnkelvoudigInformatieObject = this.zgwClient.putZaakDocument(zgwWordtEnkelvoudigInformatieObject);
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
}