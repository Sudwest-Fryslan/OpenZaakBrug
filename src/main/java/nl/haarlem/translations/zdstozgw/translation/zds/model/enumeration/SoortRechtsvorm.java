package nl.haarlem.translations.zdstozgw.translation.zds.model.enumeration;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;

@Getter
public enum SoortRechtsvorm {

	BESLOTEN_VENNOOTSCHAP("Besloten vennootschap", "besloten_vennootschap"),
	EUROPESE_COOPERATIEVE_VENNOOTSCHAP("Europese Cooperatieve Vennootschap", "europese_cooperatieve_vennootschap"),
	EUROPESE_NAAMLOZE_VENNOOTSCHAP("Europese Naamloze Vennootschap", "europese_naamloze_vennootschap"),
	COMMANDITAIRE_VENNOOTSCHAP("commanditaire vennootschap", "commanditaire_vennootschap"),
	COOPERATIEVE_EUROPESCHE_ECONOMISCHE_SAMENWERKING("cooperatie Europees Economische Samenwerking",
			"cooperatie_europees_economische_samenwerking"),
	KAPITAAL_VENNOOTSCHAP_BINNEN_EER("kapitaalvennootschap binnen EER", "kapitaalvennootschap_binnen_eer"),
	KAPITAAL_VENNOOTSCHAP_BUITEN_EER("kapitaalvennootschap buiten EER", "kapitaalvennootschap_buiten_eer"),
	KERKELIJKE_ORGANISATIE("kerkelijke Organisatie", "kerkelijke_organisatie"), MAATSCHAP("maatschap", "maatschap"),
	NAAMLOZE_VENNOOTSCHAP("naamloze Vennootschap", "naamloze_vennootschap"),
	ONDERLINGE_WAARBORG_MAATSCHAPPIJ("onderlinge Waarborg Maatschappij", "onderlinge_waarborg_maatschappij"),
	OVERIG_PRIVAATRECHTELIJKE_RECHTSPERSOON("overig privaatrechtelijke rechtspersoon",
			"overig_privaatrechtelijke_rechtspersoon"),
	OVERIGE_BUITENLANDSE_RECHTSPERSOON_VENNOOTSCHAP("overige buitenlandse rechtspersoon vennootschap",
			"overige_buitenlandse_rechtspersoon_vennootschap"),
	PUBLIEKRECHTELIJKE_RECHTSPERSOON("publiekrechtelijke Rechtspersoon", "publiekrechtelijke_rechtspersoon"),
	REDERIJ("rederij", "rederij"), STICHTING("stichting", "stichting"),
	VENNOOTSCHAP_ONDER_FIRMA("vennootschap onder Firma", "vennootschap_onder_firma"),
	VERENINGING("vereniging", "vereniging"),
	VERENIGING_VAN_EIGENAARS("vereniging van Eigenaars", "vereniging_van_eigenaars");

	private final String zdsInnRechtsvorm;
	private final String zgwInnRechtsvorm;

	SoortRechtsvorm(String zdsInnRechtsvorm, String zgwInnRechtsvorm) {
		this.zgwInnRechtsvorm = zgwInnRechtsvorm;
		this.zdsInnRechtsvorm = zdsInnRechtsvorm;
	}

	public static Optional<SoortRechtsvorm> getSoortRechtsvorm(String innRechtsvorm) {
		return Arrays.stream(SoortRechtsvorm.values())
				.filter(rechtsvorm -> innRechtsvorm != null
						&& (rechtsvorm.zdsInnRechtsvorm.toLowerCase().equals(innRechtsvorm.toLowerCase())
								|| rechtsvorm.zgwInnRechtsvorm.toLowerCase().equals(innRechtsvorm.toLowerCase())))
				.findFirst();
	}
}
