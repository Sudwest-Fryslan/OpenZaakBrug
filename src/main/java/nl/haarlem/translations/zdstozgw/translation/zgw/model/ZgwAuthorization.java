package nl.haarlem.translations.zdstozgw.translation.zgw.model;

import lombok.Data;

@Data
public class ZgwAuthorization {
	private String catalogusRsin;
	private String catalogusUrl;
    private String authorization;
}
