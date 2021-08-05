package nl.haarlem.translations.zdstozgw.translation.zds.model;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.ZKN;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = ZKN, name = "edcLv01")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsEdcLv01 extends ZdsZknDocument {

	@XmlElement(namespace = ZKN)
	public ZdsParametersMetSortering parameters;

	@XmlElement(namespace = ZKN)
	public ZdsZaakDocument gelijk;

	@XmlElement(namespace = ZKN)
	public ZdsScope scope;
}
