package nl.haarlem.translations.zdstozgw.translation.zds.model;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.STUF;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsParametersMetSortering extends ZdsParameters{

	@XmlElement(namespace = STUF, nillable = true)
	public String sortering;

	// Overrides the base.indicatorVervolgvraag
	@XmlElement(namespace = STUF, nillable = true)
	public String indicatorVervolgvraag;

	public ZdsParametersMetSortering() {
	}

	public ZdsParametersMetSortering(ZdsParametersMetSortering zdsParameters) {
		this.sortering = zdsParameters.sortering;
		this.indicatorVervolgvraag = zdsParameters.indicatorVervolgvraag;
	}
}