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
package nl.haarlem.translations.zdstozgw.config;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import nl.haarlem.translations.zdstozgw.translation.zds.model.*;
import nl.haarlem.translations.zdstozgw.translation.zds.model.enumeration.SoortRechtsvorm;

import org.apache.commons.lang.StringUtils;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.translation.BetrokkeneType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwAdres;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwBetrokkeneIdentificatie;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwOpschorting;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwRol;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwStatus;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaak;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakPut;

@Configuration
public class ModelMapperConfig {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final BuildProperties buildProperties;

	@Value("${nl.haarlem.translations.zdstozgw.timeoffset.minutes}")
	public String timeoffset;
	public static ModelMapperConfig singleton;

	@Autowired
    public ModelMapperConfig(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
	public ModelMapper modelMapper() {
		log.info("Application name:\t\t" + buildProperties.getName());
		log.info("Application version:\t" + buildProperties.getVersion());
		log.info("nl.haarlem.translations.zdstozgw.timeoffset.minutes: " + this.timeoffset);
		ModelMapper modelMapper = new ModelMapper();
		ModelMapperConfig.singleton = this;

		modelMapper.getConfiguration() // Fetch the configuration
				.setMatchingStrategy(MatchingStrategies.STRICT).setSkipNullEnabled(true)
				.setPropertyCondition(Conditions.isNotNull());

		modelMapper.typeMap(ZgwStatus.class, ZdsHeeft.class)
				.addMappings(mapper -> mapper.map(ZgwStatus::getStatustoelichting, ZdsHeeft::setToelichting))
				.addMappings(mapper -> mapper.using(convertZgwDateTimeToStufDateTime())
						.map(ZgwStatus::getDatumStatusGezet, ZdsHeeft::setDatumStatusGezet));

		modelMapper.typeMap(ZgwStatus.class, ZdsGerelateerde.class);

		modelMapper.typeMap(ZgwZaakInformatieObject.class, ZdsHeeftRelevant.class)
				.addMappings(mapper -> mapper.using(convertZgwDateTimeToStufDateTime())
						.map(ZgwZaakInformatieObject::getRegistratiedatum, ZdsHeeftRelevant::setRegistratiedatum));

		modelMapper.typeMap(ZdsHeeft.class, ZgwStatus.class)
				.addMappings(mapper -> mapper.using(convertStufDateTimeToZgwDateTime())
						.map(ZdsHeeft::getDatumStatusGezet, ZgwStatus::setDatumStatusGezet));

		modelMapper.typeMap(ZdsOpschorting.class, ZgwOpschorting.class).addMappings(mapper -> mapper
				.using(convertStringToBoolean()).map(ZdsOpschorting::getIndicatie, ZgwOpschorting::setIndicatie));
		modelMapper.typeMap(ZgwOpschorting.class, ZdsOpschorting.class).addMappings(mapper -> mapper
				.using(convertBooleanToString()).map(ZgwOpschorting::getIndicatie, ZdsOpschorting::setIndicatie));

		addZdsZaakToZgwZaakTypeMapping(modelMapper);
		addZgwZaakToZdsZaakTypeMapping(modelMapper);

		addZdsZaakToZgwZaakPutTypeMapping(modelMapper);
		addZgwZaakPutToZdsZaakTypeMapping(modelMapper);

		addZgwBetrokkeneIdentificatieToNatuurlijkPersoonTypeMapping(modelMapper);
		addZgwAdresToZdsAdresTypeMapping(modelMapper);
		addZgwEnkelvoudigInformatieObjectToZaakDocumentLinkTypeMapping(modelMapper);
		addZgwEnkelvoudigInformatieObjectToZdsZaakDocumentInhoudTypeMapping(modelMapper);

		addZdsZaakDocumentInhoudToZgwEnkelvoudigInformatieObjectTypeMapping(modelMapper);

		addZdsNatuurlijkPersoonToZgwBetrokkeneIdentificatieTypeMapping(modelMapper);
		addZdsNietNatuurlijkPersoonToZgwBetrokkeneIdentificatieTypeMapping(modelMapper);
		addZdsAdresToZgwAdresTypeMapping(modelMapper);
		addZdsZaakDocumentToZgwEnkelvoudigInformatieObjectTypeMapping(modelMapper);
		addZdsZaakDocumentRelevantToZgwEnkelvoudigInformatieObjectTypeMapping(modelMapper);

		addZgwZaakToGeefZaakDetailsTypeMappingTypeMapping(modelMapper);
        addZdsAdresToZgwAdresTypeMapping(modelMapper);

		modelMapper.addConverter(convertZgwRolToZdsRol());



		return modelMapper;
	}

	private void addZdsAdresToZgwAdresTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZdsAdres.class, ZgwAdres.class);
	}

	private void addZgwAdresToZdsAdresTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZgwAdres.class, ZdsAdres.class);
	}

	private void addZgwBetrokkeneIdentificatieToNatuurlijkPersoonTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZgwBetrokkeneIdentificatie.class, ZdsNatuurlijkPersoon.class)
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate())
						.map(ZgwBetrokkeneIdentificatie::getGeboortedatum, ZdsNatuurlijkPersoon::setGeboortedatum))
				.addMappings(mapper -> mapper.using(convertToUpperCase()).map(
						ZgwBetrokkeneIdentificatie::getGeslachtsaanduiding,
						ZdsNatuurlijkPersoon::setGeslachtsaanduiding))
				.addMappings(mapper -> mapper.using(convertToLowerCase()).map(ZgwBetrokkeneIdentificatie::getInpBsn,
						ZdsNatuurlijkPersoon::setBsn));
	}

	private void addZgwZaakToZdsZaakTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZgwZaak.class, ZdsZaak.class)
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaakPut::getStartdatum,
						ZdsZaak::setStartdatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaakPut::getRegistratiedatum,
						ZdsZaak::setRegistratiedatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaakPut::getPublicatiedatum,
						ZdsZaak::setPublicatiedatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaakPut::getEinddatumGepland,
						ZdsZaak::setEinddatumGepland))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate())
						.map(ZgwZaakPut::getUiterlijkeEinddatumAfdoening, ZdsZaak::setUiterlijkeEinddatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaak::getEinddatum,
						ZdsZaak::setEinddatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaakPut::getArchiefactiedatum,
						ZdsZaak::setDatumVernietigingDossier))
				.addMappings(mapper -> mapper.using(convertZgwArchiefNomitieToZdsArchiefNominatie())
						.map(ZgwZaakPut::getArchiefnominatie, ZdsZaak::setArchiefnominatie));
	}

	private void addZgwZaakPutToZdsZaakTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZgwZaakPut.class, ZdsZaak.class)
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaakPut::getStartdatum,
						ZdsZaak::setStartdatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaakPut::getRegistratiedatum,
						ZdsZaak::setRegistratiedatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaakPut::getPublicatiedatum,
						ZdsZaak::setPublicatiedatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaakPut::getEinddatumGepland,
						ZdsZaak::setEinddatumGepland))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate())
						.map(ZgwZaakPut::getUiterlijkeEinddatumAfdoening, ZdsZaak::setUiterlijkeEinddatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate()).map(ZgwZaakPut::getArchiefactiedatum,
						ZdsZaak::setDatumVernietigingDossier))
				.addMappings(mapper -> mapper.using(convertZgwArchiefNomitieToZdsArchiefNominatie())
						.map(ZgwZaakPut::getArchiefnominatie, ZdsZaak::setArchiefnominatie));
	}

	private void addZgwZaakToGeefZaakDetailsTypeMappingTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZgwZaak.class, ZdsZaak.class);
	}

	private void addZgwEnkelvoudigInformatieObjectToZaakDocumentLinkTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZgwEnkelvoudigInformatieObject.class, ZdsZaakDocument.class)
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate())
						.map(ZgwEnkelvoudigInformatieObject::getCreatiedatum, ZdsZaakDocument::setCreatiedatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate())
						.map(ZgwEnkelvoudigInformatieObject::getOntvangstdatum, ZdsZaakDocument::setOntvangstdatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate())
						.map(ZgwEnkelvoudigInformatieObject::getVerzenddatum, ZdsZaakDocument::setVerzenddatum))
				.addMappings(mapper -> mapper.using(convertToUpperCase()).map(
						ZgwEnkelvoudigInformatieObject::getVertrouwelijkheidaanduiding,
						ZdsZaakDocument::setVertrouwelijkAanduiding))
				.addMappings(mapper -> mapper.map(ZgwEnkelvoudigInformatieObject::getUrl, ZdsZaakDocument::setLink));
	}

	public void addZgwEnkelvoudigInformatieObjectToZdsZaakDocumentInhoudTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZgwEnkelvoudigInformatieObject.class, ZdsZaakDocumentInhoud.class)
//				.includeBase(ZgwEnkelvoudigInformatieObject.class, ZdsZaakDocument.class)
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate())
						.map(ZgwEnkelvoudigInformatieObject::getCreatiedatum, ZdsZaakDocument::setCreatiedatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate())
						.map(ZgwEnkelvoudigInformatieObject::getOntvangstdatum, ZdsZaakDocument::setOntvangstdatum))
				.addMappings(mapper -> mapper.using(convertZgwDateToStufDate())
						.map(ZgwEnkelvoudigInformatieObject::getVerzenddatum, ZdsZaakDocument::setVerzenddatum))
				.addMappings(mapper -> mapper.using(convertToUpperCase()).map(
						ZgwEnkelvoudigInformatieObject::getVertrouwelijkheidaanduiding,
						ZdsZaakDocument::setVertrouwelijkAanduiding));
	}

	public void addZdsZaakDocumentInhoudToZgwEnkelvoudigInformatieObjectTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZdsZaakDocumentInhoud.class, ZgwEnkelvoudigInformatieObject.class)
//				.includeBase(ZgwEnkelvoudigInformatieObject.class, ZdsZaakDocument.class)
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate())
						.map(ZdsZaakDocument::getCreatiedatum, ZgwEnkelvoudigInformatieObject::setCreatiedatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate())
						.map(ZdsZaakDocument::getOntvangstdatum, ZgwEnkelvoudigInformatieObject::setOntvangstdatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate())
						.map(ZdsZaakDocument::getVerzenddatum, ZgwEnkelvoudigInformatieObject::setVerzenddatum))
				.addMappings(mapper -> mapper.using(convertToLowerCase()).map(
						ZdsZaakDocument::getVertrouwelijkAanduiding,
						ZgwEnkelvoudigInformatieObject::setVertrouwelijkheidaanduiding));
	}

	public void addZdsZaakToZgwZaakTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZdsZaak.class, ZgwZaak.class)
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaak::getStartdatum,
						ZgwZaakPut::setStartdatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaak::getRegistratiedatum,
						ZgwZaakPut::setRegistratiedatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaak::getPublicatiedatum,
						ZgwZaakPut::setPublicatiedatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaak::getEinddatumGepland,
						ZgwZaakPut::setEinddatumGepland))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaak::getUiterlijkeEinddatum,
						ZgwZaak::setUiterlijkeEinddatumAfdoening))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaak::getEinddatum,
						ZgwZaak::setEinddatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate())
						.map(ZdsZaak::getDatumVernietigingDossier, ZgwZaak::setArchiefactiedatum))
				.addMappings(mapper -> mapper.using(getZGWArchiefNominatie()).map(ZdsZaak::getArchiefnominatie,
						ZgwZaakPut::setArchiefnominatie));
	}

	public void addZdsZaakToZgwZaakPutTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZdsZaak.class, ZgwZaakPut.class)
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaak::getStartdatum,
						ZgwZaakPut::setStartdatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaak::getRegistratiedatum,
						ZgwZaakPut::setRegistratiedatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaak::getPublicatiedatum,
						ZgwZaakPut::setPublicatiedatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaak::getEinddatumGepland,
						ZgwZaakPut::setEinddatumGepland))
				.addMappings(mapper -> mapper.using(getZGWArchiefNominatie()).map(ZdsZaak::getArchiefnominatie,
						ZgwZaakPut::setArchiefnominatie));
	}

	public void addZdsNatuurlijkPersoonToZgwBetrokkeneIdentificatieTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZdsNatuurlijkPersoon.class, ZgwBetrokkeneIdentificatie.class)
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate())
						.map(ZdsNatuurlijkPersoon::getGeboortedatum, ZgwBetrokkeneIdentificatie::setGeboortedatum))
				.addMappings(mapper -> mapper.map(ZdsNatuurlijkPersoon::getBsn, ZgwBetrokkeneIdentificatie::setInpBsn))
				.addMappings(
						mapper -> mapper.using(convertToLowerCase()).map(ZdsNatuurlijkPersoon::getGeslachtsaanduiding,
								ZgwBetrokkeneIdentificatie::setGeslachtsaanduiding));
	}

    public void addZdsNietNatuurlijkPersoonToZgwBetrokkeneIdentificatieTypeMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(ZdsNietNatuurlijkPersoon.class, ZgwBetrokkeneIdentificatie.class)
            .addMappings(
                mapper -> mapper.using(convertStufRechtsvormToZGWRechtsvorm()).map(ZdsNietNatuurlijkPersoon::getInnRechtsvorm,
                    ZgwBetrokkeneIdentificatie::setInnRechtsvorm));
    }

	public void addZdsZaakDocumentToZgwEnkelvoudigInformatieObjectTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZdsZaakDocument.class, ZgwEnkelvoudigInformatieObject.class)
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaakDocument::getCreatiedatum, ZgwEnkelvoudigInformatieObject::setCreatiedatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaakDocument::getOntvangstdatum, ZgwEnkelvoudigInformatieObject::setOntvangstdatum))
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaakDocument::getVerzenddatum, ZgwEnkelvoudigInformatieObject::setVerzenddatum))
				.addMappings(mapper -> mapper.using(convertToLowerCase()).map(ZdsZaakDocument::getVertrouwelijkAanduiding, ZgwEnkelvoudigInformatieObject::setVertrouwelijkheidaanduiding));
	}

	public void addZdsZaakDocumentRelevantToZgwEnkelvoudigInformatieObjectTypeMapping(ModelMapper modelMapper) {
		modelMapper.typeMap(ZdsZaakDocumentInhoud.class, ZgwEnkelvoudigInformatieObject.class)
				.includeBase(ZdsZaakDocument.class, ZgwEnkelvoudigInformatieObject.class)
				.addMappings(mapper -> mapper.using(convertStufDateToZgwDate()).map(ZdsZaakDocument::getCreatiedatum,
						ZgwEnkelvoudigInformatieObject::setCreatiedatum))
				.addMappings(
						mapper -> mapper.using(convertToLowerCase()).map(ZdsZaakDocument::getVertrouwelijkAanduiding,
								ZgwEnkelvoudigInformatieObject::setVertrouwelijkheidaanduiding))
				.addMapping(src -> src.getInhoud().getValue(), ZgwEnkelvoudigInformatieObject::setInhoud)
				.addMapping(src -> src.getInhoud().getBestandsnaam(), ZgwEnkelvoudigInformatieObject::setBestandsnaam);
	}

	static public String convertStufDateToZgwDate(String stufDate) {
		if (stufDate == null || stufDate.length() == 0) {
			return null;
		}
		var zdsDateFormatter = new SimpleDateFormat("yyyyMMdd");
		var zgwDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			if (stufDate.contains("-")) {
				throw new ConverterException("stuf date: " + stufDate + " may not contain the character '-'");
			}
			var date = zdsDateFormatter.parse(stufDate);

			// errors when 0001-01-01 was used to store documents
			if (date.before(zdsDateFormatter.parse("19000101"))){
				return null;
			}

			var zgwDate = zgwDateFormatter.format(date);
			log.debug("convertStufDateToZgwDate: " + stufDate + " (amsterdam) --> " + zgwDate
					+ "(gmt) with offset minutes:" + ModelMapperConfig.singleton.timeoffset  + "(date:" + date + ")");
			return zgwDate;

		} catch (ParseException e) {
			throw new ConverterException("ongeldige stuf-datetime: '" + stufDate + "'");
		}
	}

	static public String convertStufDateTimeToZgwDateTime(String stufDateTime) {
		return convertStufDateTimeToZgwDateTime(stufDateTime, 0);
	}

	static public String convertStufDateTimeToZgwDateTime(String stufDateTime, int offsetSeconds) {
		log.debug("convertStufDateTimeToZgwDateTime:" + stufDateTime);
		if (stufDateTime == null || stufDateTime.length() == 0) {
			return null;
		}
		if (stufDateTime.length() == 8 || stufDateTime.length() == 12 || stufDateTime.length() == 14) {
			log.debug("convertStufDateTimeToZgwDateTime input is a datetime of "+stufDateTime.length()+" characters:"
					+ stufDateTime + " will be expanded to 16");
			stufDateTime = stufDateTime + StringUtils.repeat("0", 16 - stufDateTime.length());
		}
		if (stufDateTime.length() == 17) {
			log.debug("convertStufDateTimeToZgwDateTime input is a datetime of 17 characters:"
						+ stufDateTime + " will be trimmed to 16");
			stufDateTime = stufDateTime.substring(0, 16);
		}
		if (stufDateTime.length() == 16 ) {
			// input a datetime
			log.debug("convertStufDateTimeToZgwDateTime input is a datetime:\t" + stufDateTime);
			try {

				DateTimeFormatter stufFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS");
				ZonedDateTime cetDate = LocalDateTime.parse(stufDateTime, stufFormatter).atZone(ZoneId.systemDefault());

				// check if it is a date or a datetime (ignore the seconds, this is used for the status)
				if(cetDate.getHour() == 0 && cetDate.getMinute() == 0 && cetDate.getNano() == 0) {
					// a date
					log.debug("convertStufDateTimeToZgwDateTime [date] parsed:\t\t\t" + cetDate.toString());
					cetDate = cetDate.plusSeconds(offsetSeconds);
					DateTimeFormatter zdsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
					var result = cetDate.format(zdsFormatter);
					log.debug("convertStufDateTimeToZgwDateTime [date] result:\t\t\t" + result);
					return result;
				}
				else {
					// a datetime, apply timezone and offset
					log.debug("convertStufDateTimeToZgwDateTime [datetime] parsed:\t\t\t" + cetDate.toString());
					var gmtDate = cetDate.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
					log.debug("convertStufDateTimeToZgwDateTime [datetime] to GMT tomezone:\t\t" + gmtDate.toString());
					gmtDate = gmtDate.plusMinutes(Integer.parseInt(ModelMapperConfig.singleton.timeoffset));
					gmtDate = gmtDate.plusSeconds(offsetSeconds);
					log.debug("convertStufDateTimeToZgwDateTime [datetime] added offset:\t\t" + gmtDate.toString() + " (offset in minutes:" + ModelMapperConfig.singleton.timeoffset  + ")");
					DateTimeFormatter zdsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
					var result = gmtDate.format(zdsFormatter);
					log.debug("convertStufDateTimeToZgwDateTime [datetime] result:\t\t\t" + result);
					return result;
				}
			} catch (Exception e) {
				log.warn("error parsing the string:" + stufDateTime, e);
				return e.toString();
			}
		} else {
			throw new ConverterException("datetime string: '" + stufDateTime
					+ "' has to have lengthe of 8 or 16 (current lengt:" + stufDateTime.length() + ")");
		}
	}

	static public String convertZgwDateToStufDate(String zgwDateTime) {
		log.debug("convertZgwDateToStufDate:" + zgwDateTime);
		if (zgwDateTime == null || zgwDateTime.length() == 0) {
			return null;
		}
		if (zgwDateTime.length() != 10) {
			throw new ConverterException("Verkeerde lengte(" + zgwDateTime.length()
					+ ", verwacht 10) van de datum:" + zgwDateTime);
		}
		try {
			DateTimeFormatter zdsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			var gmtDate = LocalDate.parse(zgwDateTime, zdsFormatter);
			log.debug("convertZgwDateToStufDate parsed: " + gmtDate.toString());
			DateTimeFormatter stufFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			var result = gmtDate.format(stufFormatter);
			log.debug("convertZgwDateToStufDate result: " + result);
			return result;
		} catch (Exception e) {
			log.warn("error parsing the string:" + zgwDateTime, e);
			return e.toString();
		}
	}

	static public String convertZgwDateTimeToStufDateTime(String zgwDateTime) {
		log.debug("convertZgwDateTimeToStufDateTime:\t" + zgwDateTime);
		if (zgwDateTime == null || zgwDateTime.length() == 0) {
			return null;
		}
		if (zgwDateTime.length() == 20) {
			zgwDateTime = zgwDateTime.substring(0, 19) + ".000000Z";
		}
		if (zgwDateTime.length() != 27) {
			throw new ConverterException("Verkeerde lengte(" + zgwDateTime.length()
					+ ", verwacht 27) van de datum:" + zgwDateTime);
		}
		try {
			DateTimeFormatter zdsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
			ZonedDateTime gmtDate = LocalDateTime.parse(zgwDateTime, zdsFormatter).atZone(ZoneId.of("GMT"));
			log.debug("convertZgwDateTimeToStufDateTime parsed:\t" + gmtDate.toString());
			gmtDate = gmtDate.plusMinutes(-Integer.parseInt(ModelMapperConfig.singleton.timeoffset));
			log.debug("convertZgwDateTimeToStufDateTime substractedoffset:\t" + gmtDate.toString());
			OffsetDateTime cetDate = gmtDate.toOffsetDateTime();
			log.debug("convertZgwDateTimeToStufDateTime to cet timezone:\t" + cetDate.toString());
			DateTimeFormatter stufFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			return gmtDate.format(stufFormatter);
		} catch (Exception e) {
			log.warn("error parsing the string:" + zgwDateTime, e);
			return e.toString();
		}
	}

	private AbstractConverter<String, String> convertStufDateToZgwDate() {
		return new AbstractConverter<>() {

			@Override
			protected String convert(String stufDate) {

                log.info("Convert stuf date: {}",stufDate);
                return ModelMapperConfig.convertStufDateToZgwDate(stufDate);
			}
		};
	}

	private AbstractConverter<String, String> convertStufDateTimeToZgwDateTime() {

		return new AbstractConverter<>() {

			@Override
			protected String convert(String stufDateTime) {

                log.info("Convert StufDateTime: {}", stufDateTime);
                return ModelMapperConfig.convertStufDateTimeToZgwDateTime(stufDateTime);
			}
		};
	}

	private AbstractConverter<String, String> convertZgwDateToStufDate() {
		return new AbstractConverter<>() {

			@Override
			protected String convert(String zgwDateTime) {
				return ModelMapperConfig.convertZgwDateToStufDate(zgwDateTime);
			}
		};
	}

	private AbstractConverter<String, String> convertZgwDateTimeToStufDateTime() {
		return new AbstractConverter<>() {

			@Override
			protected String convert(String zgwDateTime) {
				return ModelMapperConfig.convertZgwDateTimeToStufDateTime(zgwDateTime);
			}
		};
	}

	private AbstractConverter<String, String> getZGWArchiefNominatie() {
		return new AbstractConverter<>() {

			@Override
			protected String convert(String archiefNominatie) {
			    String result = null;
			    if(archiefNominatie != null && !archiefNominatie.equals("")){
                    result =  archiefNominatie.toUpperCase().equals("J") ? "blijvend_bewaren" : "vernietigen";
                }

				log.debug("getZGWArchiefNominatie: " + archiefNominatie + " --> " + result);
				return result;
			}
		};
	}

	private AbstractConverter<String, Boolean> convertStringToBoolean() {
		return new AbstractConverter<>() {

			@Override
			protected Boolean convert(String s) {
				var result = s.toLowerCase().equals("j");
				log.debug("convertStringToBoolean: " + s + " --> " + result);
				return result;
			}
		};
	}

	private AbstractConverter<Boolean, String> convertBooleanToString() {
		return new AbstractConverter<>() {

			@Override
			protected String convert(Boolean b) {
				var result = b ? "J" : "N";
				log.debug("convertBooleanToString: " + b + " --> " + result);
				return result;
			}
		};
	}

	private AbstractConverter<String, String> convertZgwArchiefNomitieToZdsArchiefNominatie() {
		return new AbstractConverter<>() {

			@Override
			protected String convert(String s) {
				var result = s.toUpperCase().equals("vernietigen") ? "J" : "N";
				log.debug("convertZgwArchiefNomitieToZdsArchiefNominatie: " + s + " --> " + result);
				return result;
			}
		};
	}

	private AbstractConverter<String, String> convertToLowerCase() {
		return new AbstractConverter<>() {

			@Override
			protected String convert(String s) {
				var result = s.toLowerCase();
				log.debug("convertToLowerCase: " + s + " --> " + result);
				return result;
			}
		};
	}

	private AbstractConverter<String, String> convertToUpperCase() {
		return new AbstractConverter<>() {
			@Override
			protected String convert(String s) {
				return s.toUpperCase();
			}
		};
	}

	private AbstractConverter<ZgwRol, ZdsRol> convertZgwRolToZdsRol() {
		return new AbstractConverter<>() {
			@Override
			protected ZdsRol convert(ZgwRol zgwRol) {
				ZdsRol zdsRol = new ZdsRol();
				zdsRol.gerelateerde = new ZdsGerelateerde();
				if (zgwRol.getBetrokkeneType().equalsIgnoreCase(BetrokkeneType.NATUURLIJK_PERSOON.getDescription())) {
					zdsRol.gerelateerde.natuurlijkPersoon = modelMapper().map(zgwRol.betrokkeneIdentificatie, ZdsNatuurlijkPersoon.class);
					zdsRol.gerelateerde.natuurlijkPersoon.entiteittype = "NPS";
				}
				else if (zgwRol.getBetrokkeneType().equalsIgnoreCase(BetrokkeneType.NIET_NATUURLIJK_PERSOON.getDescription())) {
					zdsRol.gerelateerde.nietNatuurlijkPersoon = modelMapper().map(zgwRol.betrokkeneIdentificatie, ZdsNietNatuurlijkPersoon.class);
					zdsRol.gerelateerde.nietNatuurlijkPersoon.entiteittype = "NNP";
				}
				else if (zgwRol.getBetrokkeneType().equalsIgnoreCase(BetrokkeneType.VESTIGING.getDescription())) {
					zdsRol.gerelateerde.vestiging = modelMapper().map(zgwRol.betrokkeneIdentificatie, ZdsVestiging.class);
					if(zgwRol.betrokkeneIdentificatie.getHandelsnaam() != null && zgwRol.betrokkeneIdentificatie.getHandelsnaam().length > 0) {
						zdsRol.gerelateerde.vestiging.handelsnaam = zgwRol.betrokkeneIdentificatie.getHandelsnaam()[0];
					}
					zdsRol.gerelateerde.vestiging.entiteittype = "VES";
				}
				else if (zgwRol.getBetrokkeneType().equalsIgnoreCase(BetrokkeneType.MEDEWERKER.getDescription())) {
					zdsRol.gerelateerde.medewerker = modelMapper().map(zgwRol.betrokkeneIdentificatie, ZdsMedewerker.class);
					zdsRol.gerelateerde.medewerker.entiteittype = "MDW";
				}
                else if (zgwRol.getBetrokkeneType().equalsIgnoreCase(BetrokkeneType.ORGANISATORISCHE_EENHEID.getDescription())) {
                    zdsRol.gerelateerde.organisatorischeEenheid = modelMapper().map(zgwRol.betrokkeneIdentificatie, ZdsOrganisatorischeEenheid.class);
                    zdsRol.gerelateerde.organisatorischeEenheid.entiteittype = "OEE";
                }
				else {
					throw new RuntimeException("Betrokkene type: " + zgwRol.getBetrokkeneType() + " nog niet geïmplementeerd");
				}
				log.debug("convertToLowerCase: " + zgwRol.roltoelichting + " --> " + zdsRol.toString());
				return zdsRol;
			}
		};
	}


    private AbstractConverter<String, String> convertStufRechtsvormToZGWRechtsvorm() {
        return new AbstractConverter<>() {
            @Override
            protected String convert(String zdsInnRechtsvorm) {
                var rechtsvorm = SoortRechtsvorm.getSoortRechtsvorm(zdsInnRechtsvorm);
                if(rechtsvorm.isPresent()){
                    return rechtsvorm.get().getZgwInnRechtsvorm();
                } else {
                    log.info("[processing warning] Rechtsvorm: '{}' kon niet worden geconverteerd", zdsInnRechtsvorm );
                    return null;
                }
            }
        };
    }
}
