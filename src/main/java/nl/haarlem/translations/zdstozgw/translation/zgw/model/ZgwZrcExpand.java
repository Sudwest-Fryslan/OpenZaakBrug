package nl.haarlem.translations.zdstozgw.translation.zgw.model;

import java.util.List;

import com.google.gson.annotations.Expose;

import lombok.Data;

@Data
public class ZgwZrcExpand {
	
	@Expose
	public ZgwZaakType zaaktype;
	
	@Expose
	public ZgwZaak hoofdzaak;
	
	@Expose
	public List<ZgwZaak> deelzaken;

	@Expose 
	public List<ZgwEigenschap> eigenschappen;

	@Expose 
	public ZgwStatus  status;
	
	@Expose
	public ZgwStatusType statustype;			
	
	@Expose 
	public ZgwResultaat  resultaat;

	@Expose
	public ZgwResultaatType resultaattype;		
	
	@Expose
	public List<ZgwRol> rollen;
	
	@Expose
	public ZgwRolType roltype;	
		
	@Expose 
	public List<ZgwZaakInformatieObject> zaakinformatieobjecten;

	@Expose 
	public ZgwZaakInformatieObjectType zaakinformatieobjecttype;	
	
	@Expose 
	public List<ZgwZaakObject> zaakobjecten;	
}
