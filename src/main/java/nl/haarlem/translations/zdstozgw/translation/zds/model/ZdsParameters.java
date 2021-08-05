package nl.haarlem.translations.zdstozgw.translation.zds.model;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.STUF;
import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.ZKN;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsParameters extends ZdsObject {
	@XmlElement(namespace = STUF)
	public String indicatorVervolgvraag;

	@XmlElement(namespace = ZKN)
	public String checkedOutId;

	public ZdsParameters() {
	}

	public ZdsParameters(ZdsParameters zdsParameters) {
		this.indicatorVervolgvraag = zdsParameters.indicatorVervolgvraag;
	}
}