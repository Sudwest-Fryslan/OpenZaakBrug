package nl.haarlem.translations.zdstozgw.translation.zgw.model;

import lombok.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Data
public class ZgwZaakObjectAdres extends ZgwZaakObject {
	@Expose
	private ZgwZaakObjectObjectIdentificatieAdres objectIdentificatie;

}
