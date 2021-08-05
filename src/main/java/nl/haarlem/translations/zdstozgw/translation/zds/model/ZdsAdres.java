package nl.haarlem.translations.zdstozgw.translation.zds.model;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.BG;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsAdres extends ZdsObject {
	@XmlElement(namespace = BG, name = "aoa.identificatie")
	public String identificatie;

	@XmlElement(namespace = BG)
	public String authentiek;

	@XmlElement(namespace = BG, name = "wpl.woonplaatsNaam")
	public String woonplaatsnaam;

	@XmlElement(namespace = BG, name = "gor.openbareRuimteNaam")
	public String openbareRuimteNaam;

	@XmlElement(namespace = BG, name = "gor.straatnaam")
	public String straatnaam;

	@XmlElement(namespace = BG, name = "aoa.postcode")
	public String postcode;

	@XmlElement(namespace = BG, name = "aoa.huisnummer")
	public String huisnummer;

	@XmlElement(namespace = BG, name = "aoa.huisletter")
	public String huisletter;

	@XmlElement(namespace = BG, name = "aoa.huisnummertoevoeging")
	public String huisnummertoevoeging;

	@XmlElement(namespace = BG, name = "inp.locatiebeschrijving")
	public String locatiebeschrijving;
}