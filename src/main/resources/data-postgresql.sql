-- Used by nl.haarlem.translations.zdstozgw.convertor.impl.GenereerZaakIdentificatie 
INSERT INTO emulate_parameter (parameter_name, parameter_description, parameter_value)  
SELECT 'ZaakIdentificatiePrefix', 'De prefix die wordt gebruikt bij de zaakidentificaties in nl.haarlem.translations.zdstozgw.convertor.impl.GenereerZaakIdentificatie', '1900'
WHERE ( SELECT COUNT(*) FROM emulate_parameter WHERE parameter_name = 'ZaakIdentificatiePrefix') = 0;
INSERT INTO emulate_parameter (parameter_name, parameter_description, parameter_value)  
SELECT 'ZaakIdentificatieHuidige', 'Het laatste volgnummer dat is gebruikt voor de zaakidentificatie in nl.haarlem.translations.zdstozgw.convertor.impl.GenereerZaakIdentificatie', '1'
WHERE ( SELECT COUNT(*) FROM emulate_parameter WHERE parameter_name = 'ZaakIdentificatieHuidige') = 0;
-- Used by nl.haarlem.translations.zdstozgw.convertor.impl.GenereerDocumentIdentificatie
INSERT INTO emulate_parameter (parameter_name, parameter_description, parameter_value)  
SELECT 'DocumentIdentificatiePrefix', 'De prefix die wordt gebruikt bij de documentidentificaties in nl.haarlem.translations.zdstozgw.convertor.impl.GenereerDocumentIdentificatie', '1900'
WHERE ( SELECT COUNT(*) FROM emulate_parameter WHERE parameter_name = 'DocumentIdentificatiePrefix') = 0;
INSERT INTO emulate_parameter (parameter_name, parameter_description, parameter_value)  
SELECT 'DocumentIdentificatieHuidige', 'Het laatste volgnummer dat is gebruikt voor de documentidentificatie in nl.haarlem.translations.zdstozgw.convertor.impl.GenereerDocumentIdentificatie', '1'
WHERE ( SELECT COUNT(*) FROM emulate_parameter WHERE parameter_name = 'DocumentIdentificatieHuidige') = 0;

CREATE OR REPLACE FUNCTION tekstTussenTekst(zoektekst TEXT, voorTussen TEXT, naTussen TEXT) RETURNS TEXT AS $$
DECLARE
  resultaat text;
  positie INT;
BEGIN
	positie := POSITION(voorTussen IN zoektekst);
  	IF positie = 0 THEN
 		RETURN NULL;
  	END IF;	
	resultaat := SUBSTRING(zoektekst, positie + LENGTH(voorTussen));	
	positie := POSITION(naTussen IN resultaat);
  	IF positie = 0 THEN
 		RETURN NULL;
  	END IF;	
	resultaat := SUBSTRING(resultaat, 0, positie);	
  	RETURN resultaat;
END;
$$ LANGUAGE 'plpgsql'