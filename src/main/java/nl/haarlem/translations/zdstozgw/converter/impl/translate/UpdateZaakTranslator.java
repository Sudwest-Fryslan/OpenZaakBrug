package nl.haarlem.translations.zdstozgw.converter.impl.translate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsBv03;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaak;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZakLk01;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

public class UpdateZaakTranslator extends Converter {

	public UpdateZaakTranslator(RequestResponseCycle context, Translation translation, ZaakService zaakService) {
		super(context, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsZakLk01) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(), ZdsZakLk01.class);
	}

	@Override
	public ResponseEntity<?> execute() throws ResponseStatusException {
		var zdsZakLk01 = (ZdsZakLk01) this.getZdsDocument();

		this.getSession().setFunctie("UpdateZaak");
		this.getSession().setKenmerk("zaakidentificatie:" + zdsZakLk01.objects.get(0).identificatie);

		ZdsZaak was = null;
		ZdsZaak wordt = null;
		if(zdsZakLk01.objects.size() == 1) {
			wordt = zdsZakLk01.objects.get(0);
		}
		else if(zdsZakLk01.objects.size() == 2) {
			was = zdsZakLk01.objects.get(0);
			wordt = zdsZakLk01.objects.get(1);
		}
		this.getZaakService().updateZaak(was, wordt);
		var bv03 = new ZdsBv03(zdsZakLk01.stuurgegevens, this.getSession().getReferentienummer());
		var response = XmlUtils.getSOAPMessageFromObject(bv03);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
