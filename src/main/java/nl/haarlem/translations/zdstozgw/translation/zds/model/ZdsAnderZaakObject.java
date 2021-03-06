package nl.haarlem.translations.zdstozgw.translation.zds.model;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.ZKN;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsAnderZaakObject {
	@XmlElement(namespace = ZKN)
	public String omschrijving;

	@XmlElement(namespace = ZKN)
	public String aanduiding;

	@XmlElement(namespace = ZKN)
	public String lokatie;

	@XmlElement(namespace = ZKN)
	public String registratie;
}
