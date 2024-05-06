package nl.haarlem.translations.zdstozgw.translation.zgw.model;

import com.google.gson.annotations.Expose;

public class ZgwEigenschap extends ZgwObject {
		
	@Expose
	public String zaak;
	
	@Expose
	public String eigenschap;

	@Expose
	public String naam;
	
	@Expose
	public String waarde;
}
