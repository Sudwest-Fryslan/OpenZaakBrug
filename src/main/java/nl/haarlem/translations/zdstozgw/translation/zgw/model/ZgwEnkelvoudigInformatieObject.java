package nl.haarlem.translations.zdstozgw.translation.zgw.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class ZgwEnkelvoudigInformatieObject {

	@SerializedName("url")
	@Expose
	public String url;

	@SerializedName("identificatie")
	@Expose
	public String identificatie;

	@SerializedName("bronorganisatie")
	@Expose
	public String bronorganisatie;

	@SerializedName("creatiedatum")
	@Expose
	public String creatiedatum;

	@SerializedName("titel")
	@Expose
	public String titel;

	@SerializedName("vertrouwelijkheidaanduiding")
	@Expose
	public String vertrouwelijkheidaanduiding;

	@SerializedName("auteur")
	@Expose
	public String auteur;

	@SerializedName("formaat")
	@Expose
	public String formaat;

	@SerializedName("taal")
	@Expose
	public String taal;

	@SerializedName("bestandsnaam")
	@Expose
	public String bestandsnaam;

	@SerializedName("informatieobjecttype")
	@Expose
	public String informatieobjecttype;

	@SerializedName("status")
	@Expose
	public String status;

	@SerializedName("versie")
	@Expose
	public String versie;

	@SerializedName("ontvangstdatum")
	@Expose
	public String ontvangstdatum;

	@SerializedName("verzenddatum")
	@Expose
	public String verzenddatum;

	@SerializedName("indicatieGebruiksrecht")
	@Expose
	public String indicatieGebruiksrecht;

	@SerializedName("beschrijving")
	@Expose
	public String beschrijving;

	@SerializedName("inhoud")
	@Expose
	public String inhoud;

	@SerializedName("lock")
	@Expose
	public String lock;

	@SerializedName("locked")
	@Expose
	public boolean locked;

    @SerializedName("bestandsomvang")
    @Expose
    public Long bestandsomvang;
}
