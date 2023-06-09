package nl.haarlem.translations.zdstozgw.translation.zds.model;

import lombok.Data;

import javax.xml.bind.annotation.*;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.BG;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ZdsAoaAdres extends ZdsObject {
	@XmlElement(namespace = BG, name = "identificatie")
	public String identificatie;

	@XmlElement(namespace = BG)
	public String authentiek;

	@XmlElement(namespace = BG, name = "wpl.woonplaatsNaam")
	public String woonplaatsnaam;

	@XmlElement(namespace = BG, name = "gor.openbareRuimteNaam")
	public String openbareRuimteNaam;

	@XmlElement(namespace = BG, name = "postcode")
	public String postcode;

	@XmlElement(namespace = BG, name = "huisnummer")
	public String huisnummer;

	@XmlElement(namespace = BG, name = "huisletter")
	public String huisletter;

	@XmlElement(namespace = BG, name = "huisnummertoevoeging")
	public String huisnummertoevoeging;

}
