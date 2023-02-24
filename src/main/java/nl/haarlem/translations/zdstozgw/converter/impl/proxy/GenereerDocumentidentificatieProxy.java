package nl.haarlem.translations.zdstozgw.converter.impl.proxy;

import nl.haarlem.translations.zdstozgw.config.SpringContext;
import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.client.ZDSClient;
import nl.haarlem.translations.zdstozgw.translation.zds.model.*;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

public class GenereerDocumentidentificatieProxy extends Converter {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GenereerDocumentidentificatieProxy(RequestResponseCycle context, Translation translation, ZaakService zaakService) {
		super(context, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsGenereerDocumentIdentificatieDi02) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(), ZdsGenereerDocumentIdentificatieDi02.class);
	}

	@Override
	public ResponseEntity<?> execute() throws ResponseStatusException {
        var url = this.getTranslation().getLegacyservice();
        var soapaction = this.getTranslation().getSoapAction();
        var request = XmlUtils.getSOAPMessageFromObject(this.zdsDocument);

        this.getSession().setFunctie("Proxy");
        this.getSession().setKenmerk(url);

        log.info("relaying request to url: " + url + " with soapaction: " + soapaction + " request-size:"
            + request.length());

        ZDSClient zdsClient = SpringContext.getBean(ZDSClient.class);

        return zdsClient.post(this.getSession().getReferentienummer(), url, soapaction, request);

	}
}
