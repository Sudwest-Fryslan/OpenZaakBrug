package nl.haarlem.translations.zdstozgw.translation.zgw.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class BetrokkeneIdentificatieMedewerker extends BetrokkeneIdentificatie {

	@SerializedName("identificatie")
	@Expose
	public String identificatie;
}