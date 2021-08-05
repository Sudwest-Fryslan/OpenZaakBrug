package nl.haarlem.translations.zdstozgw.translation.zds.model;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.ZKN;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(namespace = ZKN, name = "geefZaakdocumentbewerken_Di02")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsGeefZaakdocumentbewerkenDi02 extends ZdsZknDocument {

	@XmlElement(namespace = ZKN)
	public ZdsEdcLv01 edcLv01;

}
