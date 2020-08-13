package nl.haarlem.translations.zdstozgw.translation.zds.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.STUF;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsParameters {
    @XmlElement(namespace = STUF, nillable = true)
    public String indicatorVervolgvraag;
    @XmlElement(namespace = STUF, nillable = true)
    public String sortering;

    public ZdsParameters() {
    }

    ;

    public ZdsParameters(ZdsParameters zdsParameters) {
        this.sortering = zdsParameters.sortering;
        this.indicatorVervolgvraag = zdsParameters.indicatorVervolgvraag;
    }
}