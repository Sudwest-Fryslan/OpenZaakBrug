package nl.haarlem.translations.zdstozgw.translation.zds.model;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.ZKN;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(namespace = ZKN, name = "geefZaakdocumentbewerken_Du02")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsGeefZaakdocumentbewerkenDu02 extends ZdsZknDocument {

	@XmlElement(namespace = ZKN)
	public ZdsParameters parameters;

	@XmlElement(namespace = ZKN)
	public ZdsEdcLa01 edcLa01;

	private ZdsGeefZaakdocumentbewerkenDu02() {
	}

	public ZdsGeefZaakdocumentbewerkenDu02(ZdsStuurgegevens fromRequest, String referentienummer) {
		super(fromRequest, referentienummer);
		this.stuurgegevens.crossRefnummer = fromRequest.referentienummer;
		this.stuurgegevens.berichtcode = "Du02";
		this.stuurgegevens.functie = "geefZaakdocumentbewerken";
	}
}
