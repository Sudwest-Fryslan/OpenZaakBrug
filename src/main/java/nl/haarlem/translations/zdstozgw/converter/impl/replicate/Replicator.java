/*
 * Copyright 2020-2021 The Open Zaakbrug Contributors
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package nl.haarlem.translations.zdstozgw.converter.impl.replicate;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import nl.haarlem.translations.zdstozgw.config.SpringContext;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.converter.impl.replicate.model.ZdsReplicateGeefLijstZaakdocumentenLv01;
import nl.haarlem.translations.zdstozgw.converter.impl.replicate.model.ZdsReplicateGeefZaakdetailsLv01;
import nl.haarlem.translations.zdstozgw.converter.impl.replicate.model.ZdsReplicateGeefZaakdocumentLezenLv01;
import nl.haarlem.translations.zdstozgw.debug.Debugger;
import nl.haarlem.translations.zdstozgw.translation.zds.client.ZDSClient;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsEdcLa01GeefZaakdocumentLezen;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsHeeftRelevant;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsParametersMetSortering;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsScope;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsScopeGerelateerde;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsScopeHeeftRelevant;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsScopeObject;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaak;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocument;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZakLa01GeefZaakDetails;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZakLa01LijstZaakdocumenten;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwResultaat;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaak;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakType;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

@Service
public class Replicator {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private Converter converter;
    private final ZDSClient zdsClient;
    private static final Debugger debug = Debugger.getDebugger(MethodHandles.lookup().lookupClass());

	@Autowired
	private Replicator(ZDSClient zdsClient) {
        this.zdsClient = zdsClient;
	}

	public Replicator(Converter converter) {
		this.converter = converter;
		this.zdsClient = SpringContext.getBean(ZDSClient.class);
	}

	public void replicateZaak(String zaakidentificatie) {
		debug.infopoint("replicatie", "Start repliceren van zaak met identificatie:" + zaakidentificatie);
		String rsin = this.converter.getZaakService().getRSIN(this.converter.getZdsDocument().stuurgegevens.zender.organisatie);
		var zgwZaak = this.converter.getZaakService().zgwClient.getZaakByIdentificatie(zaakidentificatie);
		var zdsZaak = getLegacyZaak(zaakidentificatie);		
		if (zgwZaak == null) {
			createZaak(zdsZaak, rsin);
		} else {
        	updateZaak(zdsZaak);
		}
        List<ZdsHeeftRelevant> relevanteDocumenten = getLijstZaakdocumenten(zaakidentificatie);
        replicateDocumenten(zaakidentificatie, rsin, relevanteDocumenten);
    }

	private ZdsZaak getLegacyZaak(String zaakidentificatie) {
        var zdsUrl = this.converter.getZaakService().configService.getConfiguration().getReplication().getGeefZaakdetails().getUrl();
        var zdsSoapAction = this.converter.getZaakService().configService.getConfiguration().getReplication().getGeefZaakdetails().getSoapaction();
        var zdsRequest = new ZdsReplicateGeefZaakdetailsLv01();
        zdsRequest.stuurgegevens = this.converter.getZdsDocument().stuurgegevens;
        zdsRequest.stuurgegevens.berichtcode = "Lv01";
        zdsRequest.parameters = new ZdsParametersMetSortering();
        zdsRequest.parameters.setSortering("0");
        zdsRequest.parameters.setIndicatorVervolgvraag("false");
        zdsRequest.gelijk = new ZdsZaak();
        zdsRequest.gelijk.identificatie = zaakidentificatie;
        zdsRequest.scope = new ZdsScope();
        zdsRequest.scope.object = new ZdsScopeObject();
        zdsRequest.scope.object.setEntiteittype("ZAK");
        zdsRequest.scope.object.setScope("alles");

        var zdsResponse = this.zdsClient.post(this.converter.getSession().getReferentienummer(), zdsUrl, zdsSoapAction, zdsRequest);

        // fetch the zaak details
        log.debug("GeefZaakDetails response:" + zdsResponse);
        ZdsZakLa01GeefZaakDetails zakLa01 = (ZdsZakLa01GeefZaakDetails) XmlUtils.getStUFObject(zdsResponse.getBody().toString(), ZdsZakLa01GeefZaakDetails.class);
        return zakLa01.antwoord.zaak.get(0);		
	}
	
	private void createZaak(ZdsZaak zdsZaak, String rsin) {

        debug.infopoint("replicatie", "received zaak-data from zds-zaaksysteem for zaak:" + zdsZaak.identificatie + ", now storing in zgw-zaaksysteem");
        this.converter.getZaakService().creeerZaak(rsin, zdsZaak);
		var azg = this.converter.getSession().getAantalZakenGerepliceerd();
		this.converter.getSession().setAantalZakenGerepliceerd(azg + 1);
    }

    private void updateZaak(ZdsZaak zdsZaak) {
    	debug.infopoint("replicatie", "received zaak-data from zds-zaaksysteem for zaak:" + zdsZaak.identificatie + ", updating in zgw-zaaksysteem");
        this.converter.getZaakService().updateZaak(zdsZaak);        
		var azg = this.converter.getSession().getAantalZakenGerepliceerd();
		this.converter.getSession().setAantalZakenGerepliceerd(azg + 1);
    }

	public void replicateDocument(String documentidentificatie) {
		debug.infopoint("replicatie", "Start repliceren van document met identificatie:" + documentidentificatie);
		String rsin = this.converter.getZaakService().getRSIN(this.converter.getZdsDocument().stuurgegevens.zender.organisatie);

		var zgwDocument = this.converter.getZaakService().zgwClient.getZgwEnkelvoudigInformatieObjectByIdentiticatie(documentidentificatie);
		if (zgwDocument == null) {
			debug.infopoint("replicatie", "document not found, copying document with identificatie #" + documentidentificatie);
			copyDocument(documentidentificatie, rsin);
        } else {
        	debug.infopoint("replicatie", "document already found, no need to copy document with identificatie #" + documentidentificatie);
		}
	}
            
    private List<ZdsHeeftRelevant> getLijstZaakdocumenten(String zaakidentificatie) {
        List<ZdsHeeftRelevant> relevanteDocumenten = null;
        var zdsUrl = this.converter.getZaakService().configService.getConfiguration().getReplication().getGeefLijstZaakdocumenten().getUrl();
        var zdsSoapAction = this.converter.getZaakService().configService.getConfiguration().getReplication().getGeefLijstZaakdocumenten().getSoapaction();
        var zdsRequest = new ZdsReplicateGeefLijstZaakdocumentenLv01();
        zdsRequest.stuurgegevens = this.converter.getZdsDocument().stuurgegevens;
        zdsRequest.stuurgegevens.berichtcode = "Lv01";
        zdsRequest.parameters = new ZdsParametersMetSortering();
        zdsRequest.parameters.setSortering("0");
        zdsRequest.parameters.setIndicatorVervolgvraag("false");
        zdsRequest.gelijk = new ZdsZaak();
        zdsRequest.gelijk.identificatie = zaakidentificatie;
        zdsRequest.scope = new ZdsScope();
        zdsRequest.scope.object = new ZdsScopeObject();
        zdsRequest.scope.object.entiteittype = "ZAK";
        zdsRequest.scope.object.heeftRelevant = new ZdsScopeHeeftRelevant();
        zdsRequest.scope.object.heeftRelevant.entiteittype = "ZAKEDC";
        zdsRequest.scope.object.heeftRelevant.gerelateerde = new ZdsScopeGerelateerde();
        zdsRequest.scope.object.heeftRelevant.gerelateerde.entiteittype = "EDC";

        var zdsResponse = this.zdsClient.post(this.converter.getSession().getReferentienummer(), zdsUrl, zdsSoapAction, zdsRequest);
        var zakZakLa01 = (ZdsZakLa01LijstZaakdocumenten) XmlUtils.getStUFObject(zdsResponse.getBody().toString(),ZdsZakLa01LijstZaakdocumenten.class);
        if(zakZakLa01.antwoord == null || zakZakLa01.antwoord.object == null || zakZakLa01.antwoord.object.heeftRelevant == null) {
        	// no documents
        	return new ArrayList<ZdsHeeftRelevant>();
        }
        return zakZakLa01.antwoord.object.heeftRelevant;
    }

    private void replicateDocumenten(String zaakidentificatie, String rsin, List<ZdsHeeftRelevant> relevanteDocumenten) {
    	debug.infopoint("replicatie", "Aantal gekoppelde zaakdocumenten is: " + relevanteDocumenten.size() + "(zaakid: " + zaakidentificatie + ")");
    	var zgwZaak = this.converter.getZaakService().zgwClient.getZaakByIdentificatie(zaakidentificatie);
    	var zgwZaakDocumenten = this.converter.getZaakService().zgwClient.getZaakInformatieObjectenByZaak(zgwZaak.url);
        for (ZdsHeeftRelevant relevant : relevanteDocumenten) {
            var zaakdocumentidentificatie = relevant.gerelateerde.identificatie;
            debug.infopoint("replicatie", "Start repliceren van zaakdocument met  identificatie:" + zaakdocumentidentificatie + "(zaakid: " + zaakidentificatie + ")");

            ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.converter.getZaakService().zgwClient.getZgwEnkelvoudigInformatieObjectByIdentiticatie(zaakdocumentidentificatie);
            if (zgwEnkelvoudigInformatieObject == null) {
            	debug.infopoint("replicatie", "document not found, copying document with identificatie #" + zaakdocumentidentificatie);
            	try {
            		copyDocument(zaakdocumentidentificatie, rsin);
                	var adg = this.converter.getSession().getAantalDocumentenGerepliceerd();
            		this.converter.getSession().setAantalDocumentenGerepliceerd(adg + 1);
            	}
            	catch(ConverterException ex) {
            		debug.infopoint("converter exception", "document with identificatie #" + zaakdocumentidentificatie + " has error:" + ex.toString());
            		if(ex.details != null) {
            			debug.infopoint("converter exception-details", ex.details);
            		}
            		var sw = new java.io.StringWriter();
            		var pw = new java.io.PrintWriter(sw);
            		ex.printStackTrace(pw);
            		debug.infopoint("converter exception-stacktrace", sw.toString());
            		
            		var IGNORE_ERROR1 = "Fout tijdens het selecteren van een enkelvouding document.";
            		if(ex.details != null && ex.details.indexOf(IGNORE_ERROR1) !=-1) {
            			debug.infopoint("converter exception: ignore exception", "ignoring:" + IGNORE_ERROR1);
            		}
            		else {
            			throw ex;
            		}
            	}
            }
            else {
            	// maybe it needs to be attached to the zaak
            	var found = false;
            	for(ZgwZaakInformatieObject zio : zgwZaakDocumenten) {
            		if(zio.informatieobject.equals(zgwEnkelvoudigInformatieObject.url)) {
            			found = true;
            			break;
            		}
            	}
            	if(!found) {
                	debug.infopoint("replicatie", "document already but not attached to the zaak, document with identificatie #" + zaakdocumentidentificatie + " has to be punt in zaak with identificatie #" + zaakidentificatie);
            		ZgwZaakInformatieObject zgwZaakInformatieObject = this.converter.getZaakService().addZaakInformatieObject(zgwEnkelvoudigInformatieObject, zgwZaak.url);
            	}
            	else {
                	debug.infopoint("replicatie", "document already found and attached to the zaak, no action needed for document with identificatie #" + zaakdocumentidentificatie);
            	}
            }
        }
    }

	private void copyDocument(String zaakdocumentidentificatie, String rsin) {
		var zdsUrl = this.converter.getZaakService().configService.getConfiguration().getReplication()
				.getGeefZaakdocumentLezen().getUrl();
		var zdsSoapAction = this.converter.getZaakService().configService.getConfiguration().getReplication()
				.getGeefZaakdocumentLezen().getSoapaction();
		var zdsRequest = new ZdsReplicateGeefZaakdocumentLezenLv01();
		zdsRequest.stuurgegevens = this.converter.getZdsDocument().stuurgegevens;
		zdsRequest.stuurgegevens.berichtcode = "Lv01";
		zdsRequest.stuurgegevens.entiteittype = "EDC";
		zdsRequest.parameters = new ZdsParametersMetSortering();
		zdsRequest.parameters.setSortering("0");
		zdsRequest.parameters.setIndicatorVervolgvraag("false");
		zdsRequest.gelijk = new ZdsZaakDocument();
		zdsRequest.gelijk.identificatie = zaakdocumentidentificatie;
		zdsRequest.scope = new ZdsScope();
		zdsRequest.scope.object = new ZdsScopeObject();
		zdsRequest.scope.object.setEntiteittype("EDC");
		zdsRequest.scope.object.setScope("alles");

		var zdsResponse = this.zdsClient.post(this.converter.getSession().getReferentienummer(), zdsUrl, zdsSoapAction, zdsRequest);
		// fetch the document details
		log.debug("getGeefZaakdocumentLezen response:" + zdsResponse.getBody().toString());
		var zdsEdcLa01 = (ZdsEdcLa01GeefZaakdocumentLezen) XmlUtils.getStUFObject(zdsResponse.getBody().toString(),
				ZdsEdcLa01GeefZaakdocumentLezen.class);

		if (zdsEdcLa01.antwoord == null || zdsEdcLa01.antwoord.document == null || zdsEdcLa01.antwoord.document.get(0) == null) {
			//throw new RuntimeException("Document not found for identificatie: " + zaakdocumentidentificatie);
			debug.infopoint("Warning", "zaakdocumentidentificatie #" + zaakdocumentidentificatie + " not found, this document will not be replicated");
			return;
		}
		var zdsDocument = zdsEdcLa01.antwoord.document.get(0);
		debug.infopoint("replicatie", "received document-data from zds-zaaksysteem for zaakdocument:"
				+ zaakdocumentidentificatie + ", now storing in zgw-zaaksysteem");
		this.converter.getZaakService().voegZaakDocumentToe(rsin, zdsDocument);
	}

    public ResponseEntity<?> proxy() {
		var url = this.converter.getTranslation().getLegacyservice();
		var soapaction = this.converter.getTranslation().getSoapaction();
		var request = this.converter.getSession().getClientRequestBody();
		debug.infopoint("proxy", "relaying request to url: " + url + " with soapaction: " + soapaction + " request-size:" + request.length());

		var legacyresponse = this.zdsClient.post(this.converter.getSession().getReferentienummer(), url, soapaction, request);
		if (legacyresponse.getStatusCode() != HttpStatus.OK) {
			throw new ConverterException("HttpStatus:" + legacyresponse.getStatusCode().toString() + " Url:" + url + " SoapAction: " + soapaction +  " Service:" + this.converter.getTranslation().getLegacyservice(), legacyresponse.getBody().toString());
		}
		return legacyresponse;
	}
}