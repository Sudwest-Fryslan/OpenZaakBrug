package nl.haarlem.translations.zdstozgw.converter.impl.translate;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsEdcLa01;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsGeefZaakdocumentbewerkenDi02;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsGeefZaakdocumentbewerkenDu02;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsParameters;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocumentAntwoord;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocumentInhoud;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

public class GeefZaakdocumentBewerkenTranslator extends Converter {

	public GeefZaakdocumentBewerkenTranslator(RequestResponseCycle context, Translation translation, ZaakService zaakService) {
		super(context, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsGeefZaakdocumentbewerkenDi02) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(), ZdsGeefZaakdocumentbewerkenDi02.class);
	}

	@Override
	public ResponseEntity<?> execute() throws ResponseStatusException {
		var zdsGeefZaakdocumentbewerkenDi02 = (ZdsGeefZaakdocumentbewerkenDi02) this.getZdsDocument();
		var documentIdentificatie = zdsGeefZaakdocumentbewerkenDi02.edcLv01.gelijk.identificatie;

		this.getSession().setFunctie("GeefZaakdocumentBewerken");
		this.getSession().setKenmerk("documentidentificatie:" + documentIdentificatie);

		// het document ophalen
		ZdsZaakDocumentInhoud document = this.getZaakService().getZaakDocumentLezen(documentIdentificatie);
		// zetten van de lock
		var lock = this.getZaakService().checkOutZaakDocument(documentIdentificatie);

		var du02 = new ZdsGeefZaakdocumentbewerkenDu02(zdsGeefZaakdocumentbewerkenDi02.stuurgegevens, this.getSession().getReferentienummer());
		du02.parameters = new ZdsParameters();
		du02.parameters.checkedOutId = lock;
		du02.edcLa01 = new ZdsEdcLa01();
		du02.edcLa01.antwoord = new ZdsZaakDocumentAntwoord();
		du02.edcLa01.parameters = new ZdsParameters();
		du02.edcLa01.antwoord.document = new ArrayList<ZdsZaakDocumentInhoud>();
		document.link = "";
		du02.edcLa01.antwoord.document.add(document);

		var response = XmlUtils.getSOAPMessageFromObject(du02);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}