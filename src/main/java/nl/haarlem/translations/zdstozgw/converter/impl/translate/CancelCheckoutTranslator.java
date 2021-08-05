package nl.haarlem.translations.zdstozgw.converter.impl.translate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsBv02;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsCancelCheckoutDi02;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

public class CancelCheckoutTranslator extends Converter {

	public CancelCheckoutTranslator(RequestResponseCycle context, Translation translation, ZaakService zaakService) {
		super(context, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsCancelCheckoutDi02) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(), ZdsCancelCheckoutDi02.class);
	}

	@Override
	public ResponseEntity<?> execute() throws ResponseStatusException {
		var zdsCancelCheckoutDi02 = (ZdsCancelCheckoutDi02) this.getZdsDocument();
		var lock = zdsCancelCheckoutDi02.parameters.checkedOutId;
		var documentIdentificatie = zdsCancelCheckoutDi02.document.identificatie;

		this.getSession().setFunctie("CancelCheckout");
		this.getSession().setKenmerk("documentidentificatie:" + documentIdentificatie + " with lock:" + lock);

		var result = this.getZaakService().cancelCheckOutZaakDocument(documentIdentificatie, lock);

		var bv02 = new ZdsBv02();
		var response = XmlUtils.getSOAPMessageFromObject(bv02);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
