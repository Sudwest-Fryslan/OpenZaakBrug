package nl.haarlem.translations.zdstozgw.config.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class ZgwRolOmschrijving {
	@SerializedName("heeftBetrekkingOp")
	@Expose
	String heeftBetrekkingOp = null;

	@SerializedName("heeftAlsBelanghebbende")
	@Expose
	String heeftAlsBelanghebbende = null;

	@SerializedName("heeftAlsInitiator")
	@Expose
	String heeftAlsInitiator = null;

	@SerializedName("heeftAlsUitvoerende")
	@Expose
	String heeftAlsUitvoerende = null;

	@SerializedName("heeftAlsVerantwoordelijke")
	@Expose
	String heeftAlsVerantwoordelijke = null;

	@SerializedName("heeftAlsGemachtigde")
	@Expose
	String heeftAlsGemachtigde = null;

	@SerializedName("heeftAlsOverigBetrokkene")
	@Expose
	String heeftAlsOverigBetrokkene = null;

}
