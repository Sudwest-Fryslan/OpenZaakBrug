package nl.haarlem.translations.zdstozgw.translation.zgw.model;

import lombok.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Data
public class ZgwZaakObject {
	@SerializedName("url")
	@Expose
	private String url;
	@SerializedName("uuid")
	@Expose
	private String uuid;

    @SerializedName("zaak")
    @Expose
    private String zaak;

    @SerializedName("object")
    @Expose
    private String object;

    @SerializedName("objectType")
    @Expose
    private String objectType;

    @SerializedName("objectTypeOverige")
    @Expose
    private String objectTypeOverige;

    @SerializedName("relatieomschrijving")
    @Expose
    private String relatieomschrijving = "Heeft betrekking op";

}
