package nl.haarlem.translations.zdstozgw.config.model;

import com.google.gson.annotations.Expose;

import lombok.Data;

@Data
public class TranslateVerblijfsadresForZaaktype {
	@Expose
	public String zaakType;
	@Expose
	public Boolean useOpenbareRuimteNaam;
}
