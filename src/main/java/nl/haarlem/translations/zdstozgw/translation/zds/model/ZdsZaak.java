package nl.haarlem.translations.zdstozgw.translation.zds.model;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.ZKN;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsZaak extends ZdsZaakIdentificatie {
//	@XmlAttribute(namespace = STUF)
//	public String entiteittype;

//	@XmlAttribute(namespace = STUF)
//	public String scope;

	@XmlElement(namespace = ZKN)
	public String omschrijving;

	@XmlElement(namespace = ZKN)
	public String toelichting;

	@XmlElement(namespace = ZKN)
	public List<ZdsKenmerk> kenmerk;

	@XmlElement(namespace = ZKN)
	public ZdsAnderZaakObject anderZaakObject;

	@XmlElement(namespace = ZKN)
	public ZdsResultaat resultaat;

	@XmlElement(namespace = ZKN)
	public String startdatum;

	@XmlElement(namespace = ZKN)
	public String registratiedatum;

	@XmlElement(namespace = ZKN)
	public String publicatiedatum;

	@XmlElement(namespace = ZKN)
	public String einddatumGepland;

	@XmlElement(namespace = ZKN)
	public String uiterlijkeEinddatum;

	@XmlElement(namespace = ZKN)
	public String einddatum;

	@XmlElement(namespace = ZKN)
	public ZdsOpschorting opschorting;

	@XmlElement(namespace = ZKN)
	public ZdsVerlenging verlenging;

	@XmlElement(namespace = ZKN)
	public String betalingsIndicatie;

	@XmlElement(namespace = ZKN)
	public String laatsteBetaaldatum;

	@XmlElement(namespace = ZKN)
	public String archiefnominatie;

	@XmlElement(namespace = ZKN)
	public String datumVernietigingDossier;

	@XmlElement(namespace = ZKN)
	public String zaakniveau;

	@XmlElement(namespace = ZKN)
	public String deelzakenIdicatie;

	@XmlElement(namespace = ZKN)
	public ZdsRol isVan;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftBetrekkingOp;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsBelanghebbende;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsGemachtigde;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsInitiator;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsUitvoerende;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsVerantwoordelijke;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsOverigBetrokkene;

	@XmlElement(namespace = ZKN)
	public List<ZdsHeeft> heeft;

	@XmlElement(namespace = ZKN)
	public ZdsHeeftRelevant heeftRelevant;
}
