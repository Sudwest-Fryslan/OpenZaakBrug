package nl.haarlem.translations.zdstozgw.translation.zgw.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class ZgwResultaatType {

	@SerializedName("url")
	@Expose
	public String url;

	@SerializedName("zaaktype")
	@Expose
	public String zaaktype;

	@SerializedName("omschrijving")
	@Expose
	public String omschrijving;

	@SerializedName("resultaattypeomschrijving")
	@Expose
	public String resultaattypeomschrijving;

	@SerializedName("omschrijvingGeneriek")
	@Expose
	public String omschrijvingGeneriek;

	@SerializedName("selectielijstklasse")
	@Expose
	public String selectielijstklasse;

	@SerializedName("toelichting")
	@Expose
	public String toelichting;

	@SerializedName("archiefnominatie")
	@Expose
	public String archiefnominatie;

	@SerializedName("archiefactietermijn")
	@Expose
	public String archiefactietermijn;
}
