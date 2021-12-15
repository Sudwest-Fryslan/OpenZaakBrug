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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import lombok.Data;
import nl.haarlem.translations.zdstozgw.config.model.ZaaktypeMetCoalesceResultaat;
import nl.haarlem.translations.zdstozgw.config.model.Configuration;
import nl.haarlem.translations.zdstozgw.config.model.Organisatie;
import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;

@Service
@Data
public class ConfigService {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Configuration configuration;

	public ConfigService(@Value("${config.json.location:config.json}") String configPath) throws Exception {
		var cpr = new ClassPathResource(configPath);

		try(InputStream configStream = cpr.getInputStream()){
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(configStream));
			Gson gson = new Gson();
			this.configuration = gson.fromJson(bufferedReader, Configuration.class);
		}

		validateConfiguration();
		log.debug("ConfigService succesfully loaded");
	}

	private void validateConfiguration() throws Exception {
		log.debug("validateConfiguration");
		var section = "";
		try {
			section = "requestHandlerImplementation";
			log.debug("=== " + section + " ===");			
			log.debug("\trequestHandlerImplementation:" + this.configuration.getRequestHandlerImplementation());

			section = "organisaties";
			log.debug("=== " + section + " #" + this.configuration.getOrganisaties().size() + " ===");
			for (Organisatie organisatie : this.configuration.getOrganisaties()) {
				log.debug("\t===>\tgemeentenaam:" + organisatie.getGemeenteNaam());
				log.debug("\t\tgemeentecode:" + organisatie.getGemeenteCode());
				log.debug("\t\trsin:" + organisatie.getRSIN());
			}

			section = "zgwRolOmschrijving";
			log.debug("=== " + section + " ===");
			var rolomschrijving = this.configuration.getZgwRolOmschrijving();
			log.debug("\theeftBetrekkingOp:" + rolomschrijving.getHeeftBetrekkingOp());
			log.debug("\theeftAlsBelanghebbende:" + rolomschrijving.getHeeftAlsBelanghebbende());
			log.debug("\theeftAlsInitiator:" + rolomschrijving.getHeeftAlsInitiator());
			log.debug("\theeftAlsUitvoerende:" + rolomschrijving.getHeeftAlsUitvoerende());
			log.debug("\theeftAlsVerantwoordelijke:" + rolomschrijving.getHeeftAlsVerantwoordelijke());
			log.debug("\theeftAlsGemachtigde:" + rolomschrijving.getHeeftAlsGemachtigde());
			log.debug("\theeftAlsOverigeBetrokkene:" + rolomschrijving.getHeeftAlsOverigBetrokkene());
	
			section = "beeindigZaakWanneerEinddatum";
			log.debug("=== " + section + " #" + this.configuration.getBeeindigZaakWanneerEinddatum().size() + " ===");			
			for (ZaaktypeMetCoalesceResultaat beeindigZaakWanneerEinddatum : this.configuration.getBeeindigZaakWanneerEinddatum()) {
				log.debug("\t===>\tzaakType:" + beeindigZaakWanneerEinddatum.getZaakType());
				log.debug("\t\tcoalesceResultaat:" + beeindigZaakWanneerEinddatum.getCoalesceResultaat());
			}

			section = "einddatumEnResultaatWanneerLastStatus";
			log.debug("=== " + section + " #" + this.configuration.getEinddatumEnResultaatWanneerLastStatus().size() + " ===");			
			for (ZaaktypeMetCoalesceResultaat beeindigZaakWanneerEinddatum : this.configuration.getEinddatumEnResultaatWanneerLastStatus()) {
				log.debug("\t===>\tzaakType:" + beeindigZaakWanneerEinddatum.getZaakType());
				log.debug("\t\tcoalesceResultaat:" + beeindigZaakWanneerEinddatum.getCoalesceResultaat());
			}
			
			section = "replicatie";
			log.debug("=== " + section + " ===");
			var replicatie = this.configuration.getReplication();
			log.debug("\tgeefZaakDetailsAction:" + replicatie.getGeefZaakdetails().getSoapaction());
			log.debug("\tgeefZaakDetailsUrl:" + replicatie.getGeefZaakdetails().getUrl());
			log.debug("\tgeefLijstZaakdocumentenAction:" + replicatie.getGeefLijstZaakdocumenten().getSoapaction());
			log.debug("\tgeefLijstZaakdocumentenUrl:" + replicatie.getGeefLijstZaakdocumenten().getUrl());
			log.debug("\tgeefZaakDocumentLezenAction:" + replicatie.getGeefZaakdocumentLezen().getSoapaction());
			log.debug("\tgeefZaakDocumentLezenUrl:" + replicatie.getGeefZaakdocumentLezen().getUrl());
	
			section = "translations";
			log.debug("=== " + section + " #" + this.configuration.getTranslations().size() +  " ===");
			for (Translation translation : this.configuration.getTranslations()) {
				log.debug("\t===>\ttranslation:" + translation.getTranslation());
				log.debug("\t\tpath:" + translation.getPath());
				log.debug("\t\tsoapAction:" + translation.getSoapaction());
				log.debug("\t\ttemplate:" + translation.getTemplate());
				log.debug("\t\timplementation:" + translation.getImplementation());
				log.debug("\t\tlegacyservice:" + translation.getLegacyservice());
			}
		}
		catch(Exception e) {
			var msg = "Invalid config.json, error in section: '" + section + "'";
			log.error(msg, e);
			throw new ConverterException(msg, e);
		}
	}

	public Translation getTranslationByPathAndSoapAction(String path, String soapAction) {
		log.debug("searching first translaton for : /" + path + "/ with soapaction: " + soapAction);
		for (Translation translation : this.configuration.getTranslations()) {
			log.debug("\t checking path '" + translation.getPath() + "' with action: '" + translation.getSoapaction()
					+ "'");
			if (path.equals(translation.getPath()) && soapAction.equals(translation.getSoapaction())) {
				return translation;
			}
		}
		return null;
	}
}
