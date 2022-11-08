package nl.haarlem.translations.zdstozgw.config;

import java.io.*;
import java.lang.invoke.MethodHandles;

import nl.haarlem.translations.zdstozgw.config.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.google.gson.Gson;

import lombok.Data;
import nl.haarlem.translations.zdstozgw.config.model.Configuration;

@Service
@Data
public class ConfigService {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Configuration configuration;

	public ConfigService(@Value("${config.json.location:config.json}") String configPath) throws Exception {
		var cpr = new ClassPathResource(configPath);
        InputStream configStream = null;

        try{
            configStream = cpr.getInputStream();
        } catch (Exception exception){
            try {
                configStream = new FileInputStream(configPath);
            } catch (Exception exception1){
                log.error("Unable to load config from classpath or file: "+configPath);
            }
        }

        Gson gson = new Gson();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(configStream));
        this.configuration = gson.fromJson(bufferedReader, Configuration.class);

		validateConfiguration();
		log.debug("ConfigService succesfully loaded");
	}

	private void validateConfiguration() throws Exception {
		log.debug("validateConfiguration");
		log.debug("requestHandlerImplementation:" + this.configuration.getRequestHandlerImplementation());

		this.configuration.getOrganisaties().size();
		log.debug("=== organisaties ===");
		for (Organisatie organisatie : this.configuration.getOrganisaties()) {
			log.debug("gemeentenaam:" + organisatie.getGemeenteNaam());
			log.debug("gemeentecode:" + organisatie.getGemeenteCode());
			log.debug("rsin:" + organisatie.getRSIN());
		}

		var rolomschrijving = this.configuration.getZgwRolOmschrijving();
		log.debug("=== rol omschrijvingen ===");
		log.debug("heeftBetrekkingOp:" + rolomschrijving.getHeeftBetrekkingOp());
		log.debug("heeftAlsBelanghebbende:" + rolomschrijving.getHeeftAlsBelanghebbende());
		log.debug("heeftAlsInitiator:" + rolomschrijving.getHeeftAlsInitiator());
		log.debug("heeftAlsUitvoerende:" + rolomschrijving.getHeeftAlsUitvoerende());
		log.debug("heeftAlsVerantwoordelijke:" + rolomschrijving.getHeeftAlsVerantwoordelijke());
		log.debug("heeftAlsGemachtigde:" + rolomschrijving.getHeeftAlsGemachtigde());
		log.debug("heeftAlsOverigeBetrokkene:" + rolomschrijving.getHeeftAlsOverigBetrokkene());

		var replicatie = this.configuration.getReplication();
		log.debug("=== replicatie ===");
		log.debug("geefZaakDetailsAction:" + replicatie.getGeefZaakdetails().getSoapaction());
		log.debug("geefZaakDetailsUrl:" + replicatie.getGeefZaakdetails().getUrl());
		log.debug("geefLijstZaakdocumentenAction:" + replicatie.getGeefLijstZaakdocumenten().getSoapaction());
		log.debug("geefLijstZaakdocumentenUrl:" + replicatie.getGeefLijstZaakdocumenten().getUrl());
		log.debug("geefZaakDocumentLezenAction:" + replicatie.getGeefZaakdocumentLezen().getSoapaction());
		log.debug("geefZaakDocumentLezenUrl:" + replicatie.getGeefZaakdocumentLezen().getUrl());

		this.configuration.getTranslations().size();
		log.debug("=== translaties ===");
		for (Translation translation : this.configuration.getTranslations()) {
			log.debug("translation:" + translation.getTranslation());
			log.debug("path:" + translation.getPath());
			log.debug("soapAction:" + translation.getSoapAction());
			log.debug("template:" + translation.getTemplate());
			log.debug("implementation:" + translation.getImplementation());
			log.debug("legacyservice:" + translation.getLegacyservice());
		}
	}

	public Translation getTranslationByPathAndSoapAction(String path, String soapAction) {
		log.debug("searching first translaton for : /" + path + "/ with soapaction: " + soapAction);
		for (Translation translation : this.configuration.getTranslations()) {
			log.debug("\t checking path '" + translation.getPath() + "' with action: '" + translation.getSoapAction()
					+ "'");
			if (path.equals(translation.getPath()) && soapAction.equals(translation.getSoapAction())) {
				return translation;
			}
		}
		return null;
	}
}
