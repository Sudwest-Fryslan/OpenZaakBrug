package nl.haarlem.translations.zdstozgw.converter.impl.translate;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsEdcLa01GeefZaakdocumentLezen;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsEdcLv01;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsParameters;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocumentAntwoord;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocumentInhoud;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

public class GeefZaakdocumentLezenTranslator extends Converter {

	public GeefZaakdocumentLezenTranslator(RequestResponseCycle session, Translation translation,
			ZaakService zaakService) {
		super(session, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsEdcLv01) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(), ZdsEdcLv01.class);
	}

	@Override
	public ResponseEntity<?> execute() throws ResponseStatusException {
		var zdsEdcLv01 = (ZdsEdcLv01) this.getZdsDocument();
		var documentIdentificatie = zdsEdcLv01.gelijk.identificatie;

		this.getSession().setFunctie("GeefZaakdocumentLezen");
		this.getSession().setKenmerk("documentidentificatie:" + documentIdentificatie);

		ZdsZaakDocumentInhoud document = this.getZaakService().getZaakDocumentLezen(documentIdentificatie);
		var edcLa01 = new ZdsEdcLa01GeefZaakdocumentLezen(zdsEdcLv01.stuurgegevens, this.getSession().getReferentienummer());
		edcLa01.antwoord = new ZdsZaakDocumentAntwoord();
		edcLa01.antwoord.document = new ArrayList<ZdsZaakDocumentInhoud>();
		edcLa01.antwoord.document.add(document);
		edcLa01.parameters = new ZdsParameters(zdsEdcLv01.parameters);
		var response = XmlUtils.getSOAPMessageFromObject(edcLa01);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}