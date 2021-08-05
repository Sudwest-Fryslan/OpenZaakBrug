package nl.haarlem.translations.zdstozgw.converter.impl.translate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsBv03;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZakLk01ActualiseerZaakstatus;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

public class ActualiseerZaakStatusTranslator extends Converter {

	public ActualiseerZaakStatusTranslator(RequestResponseCycle context, Translation translation,
			ZaakService zaakService) {
		super(context, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsZakLk01ActualiseerZaakstatus) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(),
				ZdsZakLk01ActualiseerZaakstatus.class);
	}

	@Override
	public ResponseEntity<?> execute() throws ResponseStatusException {
		var zdsZakLk01ActualiseerZaakstatus = (ZdsZakLk01ActualiseerZaakstatus) this.zdsDocument;
		var zdsWasZaak = zdsZakLk01ActualiseerZaakstatus.objects.get(0);

		this.getSession().setFunctie("ActualiseerZaakStatus");
		this.getSession().setKenmerk("zaakidentificatie:" + zdsWasZaak.identificatie);

		var zdsWordtZaak = zdsZakLk01ActualiseerZaakstatus.objects.get(1);
		var zgwZaak = this.getZaakService().actualiseerZaakstatus(zdsWasZaak, zdsWordtZaak);
		var bv03 = new ZdsBv03(zdsZakLk01ActualiseerZaakstatus.stuurgegevens, this.getSession().getReferentienummer());
		var response = XmlUtils.getSOAPMessageFromObject(bv03);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
