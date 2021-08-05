package nl.haarlem.translations.zdstozgw.translation.zds.model;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.STUF;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "Bv02Bericht", namespace = STUF)
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsBv02 extends ZdsObject {

	@XmlElement(namespace = STUF, name = "stuurgegevens")
	public ZdsStuurgegevens stuurgegevens;

	public ZdsBv02() {
		this.stuurgegevens = new ZdsStuurgegevens();
		this.stuurgegevens.berichtcode = "Bv02";
	}
}
