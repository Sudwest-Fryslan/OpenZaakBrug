package nl.haarlem.translations.zdstozgw.translation.zgw.model;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
public class ZgwZaakObjectObjectIdentificatieAdres {
	@Expose
	private String identificatie = "identificatie";
	@Expose
	private String wplWoonplaatsNaam;
	@Expose
	private String gorOpenbareRuimteNaam;
	@Expose
	private String postcode;
	@Expose
	private String huisnummer;
	@Expose
	private String huisletter;
	@Expose
	private String huisnummertoevoeging;
}
