package nl.haarlem.translations.zdstozgw.config;

import java.util.List;

import nl.haarlem.translations.zdstozgw.config.model.Configuration;
import nl.haarlem.translations.zdstozgw.config.model.Organisatie;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsStuurgegevens;

public class StufHeader {
	public static String getRSIN(Configuration config, ZdsStuurgegevens stuurgegevens) {
		String rsin = null;
		if(stuurgegevens != null && stuurgegevens.zender != null && stuurgegevens.zender.organisatie != null) {
			String organisatie = stuurgegevens.zender.organisatie;
			return StufHeader.getRSIN(config, organisatie);
		}
		else {			
			var organisaties = config.getOrganisaties(); 
			Organisatie foundOrganisatie = organisaties.stream().filter(current -> Boolean.TRUE.equals(current.getVoorkeur())).findFirst().orElse(null);
			if(foundOrganisatie == null) throw new ConverterException("Geen stuurgegevens.zender.organisatie of voorkeur organisatie gedefinieerd in config.json");
			return foundOrganisatie.getRSIN();			
		}		
	}
	
	public static String getRSIN(Configuration config, String gemeenteCode) {
		List<Organisatie> organisaties = config.getOrganisaties();
		for (Organisatie organisatie : organisaties) {
			if (organisatie.getGemeenteCode().equals(gemeenteCode)) {
				return organisatie.getRSIN();
			}
		}
		return "";
	}
	
	public static String getApplicatie(ZdsStuurgegevens stuurgegevens) {
		if(stuurgegevens == null) return null;
		if(stuurgegevens.zender == null) return null;
		if(stuurgegevens.zender.applicatie == null) return null;
		
		return stuurgegevens.zender.applicatie;
	}

	public static String getGebruiker(ZdsStuurgegevens stuurgegevens) {
		if(stuurgegevens == null) return null;
		if(stuurgegevens.zender == null) return null;
		if(stuurgegevens.zender.gebruiker == null) return null;
		
		return stuurgegevens.zender.gebruiker;
	}	
}
