package nl.haarlem.translations.zdstozgw.translation.zds.model;

import lombok.Data;
import nl.haarlem.translations.zdstozgw.utils.StufUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.STUF;

@Data
@XmlRootElement(name = "Bv03Bericht", namespace = STUF)
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsBv03 {

    @XmlElement(namespace = STUF)
    public ZdsStuurgegevens zdsStuurgegevens;

    public ZdsBv03() {
    }

    public ZdsBv03(ZdsStuurgegevens zdsStuurgegevens) {
        this.zdsStuurgegevens = new ZdsStuurgegevens(zdsStuurgegevens);
        this.zdsStuurgegevens.tijdstipBericht = StufUtils.getStufDateTime();
        this.zdsStuurgegevens.berichtcode = "Bv03";
        this.zdsStuurgegevens.referentienummer = zdsStuurgegevens.referentienummer;
    }
}