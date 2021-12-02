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
package nl.haarlem.translations.zdstozgw.converter.impl.fastdrc;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.STUF;
import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.ZKN;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.debug.Debugger;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsAntwoordLijstZaakdocument;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsHeeftRelevant;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsObjectLijstZaakDocument;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsParameters;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsStuurgegevens;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocument;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZakLa01LijstZaakdocumenten;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZakLv01;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwInformatieObjectType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaak;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakInformatieObject;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

public class GeefLijstZaakdocumentenTranslator extends Converter {
	private String datasourceDriverClassName;
	private String datasourceUrl;
	private String datasourceUsername;
	private String datasourcePassword;
	private String datasourceSql;

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
	
	public GeefLijstZaakdocumentenTranslator(RequestResponseCycle context, Translation translation,
			ZaakService zaakService) {
		super(context, translation, zaakService);

		datasourceDriverClassName = retrieveParameter("datasource.driverClassName");
		datasourceUrl = retrieveParameter("datasource.url");
		datasourceUsername = retrieveParameter("datasource.username");
		datasourcePassword = retrieveParameter("datasource.password");
		datasourceSql = retrieveParameter("datasource.sql");
		
		 try {
			Class.forName(datasourceDriverClassName);
		} catch (ClassNotFoundException e) {
			throw new ConverterException("error loading database driver:" + datasourceDriverClassName, e.getMessage(), e);
		}
	}
	public String retrieveParameter(String name) {
		if(this.translation.getParameterValue(name) == null) throw new ConverterException("required parameter '" + name + "' is not defined"); 	
		return this.translation.getParameterValue(name);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsZakLv01) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(), ZdsZakLv01.class);
	}

	@Override
	public ResponseEntity<?> execute() throws ResponseStatusException {
		ZdsZakLv01 zdsZakLv01 = (ZdsZakLv01) this.getZdsDocument();
		var zaakidentificatie = zdsZakLv01.gelijk.identificatie;

		this.getSession().setFunctie("GeefLijstZaakdocumenten");
		this.getSession().setKenmerk("zaakidentificatie:" + zaakidentificatie);
		log.debug("geefLijstZaakdocumenten:" + zaakidentificatie);
		ZgwZaak zgwZaak = this.getZaakService().zgwClient.getZaakByIdentificatie(zaakidentificatie);
		var gerelateerdeDocumenten = new ArrayList<ZdsHeeftRelevant>();
		
		var sql = datasourceSql.replace("${UUID}", zgwZaak.uuid);
		try {
			Connection conn = DriverManager.getConnection(datasourceUrl, datasourceUsername, datasourcePassword);
			Statement stmt = conn.createStatement();
		    ResultSet rs = stmt.executeQuery(datasourceSql);
			 while (rs.next()) {
				/*
				for (ZgwZaakInformatieObject zgwZaakInformatieObject : zgwZaakInformatieObjecten) {
					ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient
							.getZaakDocumentByUrl(zgwZaakInformatieObject.informatieobject);
					if (zgwEnkelvoudigInformatieObject == null || zgwEnkelvoudigInformatieObject.informatieobjecttype == null) {
						throw new ConverterException("could not get the zaakdocument: "
								+ zgwZaakInformatieObject.informatieobject + " for zaak:" + zaakidentificatie);
					}			
					ZgwInformatieObjectType documenttype = this.zgwClient
							.getZgwInformatieObjectTypeByUrl(zgwEnkelvoudigInformatieObject.informatieobjecttype);
					if (documenttype == null) {
						throw new ConverterException("getZgwInformatieObjectType #"
								+ zgwEnkelvoudigInformatieObject.informatieobjecttype + " could not be found");
					}
					ZdsZaakDocument zdsZaakDocument = this.modelMapper.map(zgwEnkelvoudigInformatieObject,
							ZdsZaakDocument.class);
					zdsZaakDocument.omschrijving = documenttype.omschrijving;
					ZdsHeeftRelevant heeftRelevant = this.modelMapper.map(zgwZaakInformatieObject, ZdsHeeftRelevant.class);
					heeftRelevant.gerelateerde = zdsZaakDocument;
					relevanteDocumenten.add(heeftRelevant);
		
				}
				return relevanteDocumenten;
		 		*/

			 	// Retrieve by column name
				// System.out.print("ID: " + rs.getInt("id"));
				// System.out.print(", Age: " + rs.getInt("age"));
				// System.out.print(", First: " + rs.getString("first"));
				// System.out.println(", Last: " + rs.getString("last"));
					
				// ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject = this.zgwClient.getZaakDocumentByUrl(zgwZaakInformatieObject.informatieobject);
				// if (zgwEnkelvoudigInformatieObject == null || zgwEnkelvoudigInformatieObject.informatieobjecttype == null) {
				// 	throw new ConverterException("could not get the zaakdocument: "
				// 			+ zgwZaakInformatieObject.informatieobject + " for zaak:" + zaakidentificatie);
				// }			

				var zdsZaakDocument = new ZdsZaakDocument();
				zdsZaakDocument.identificatie = rs.getString("informatieobjecttype");
				zdsZaakDocument.omschrijving = rs.getString("omschrijving");				
				zdsZaakDocument.creatiedatum = rs.getString("creatiedatum");
				zdsZaakDocument.ontvangstdatum = rs.getString("ontvangstdatum");
				zdsZaakDocument.titel = rs.getString("titel");
				zdsZaakDocument.formaat = rs.getString("formaat");
				zdsZaakDocument.taal = rs.getString("taal");
				zdsZaakDocument.versie = rs.getString("versie");
				zdsZaakDocument.status = rs.getString("status");
				zdsZaakDocument.verzenddatum = rs.getString("verzenddatum");
				zdsZaakDocument.vertrouwelijkAanduiding = rs.getString("vertrouwelijkAanduiding");
				zdsZaakDocument.auteur = rs.getString("auteur");
				zdsZaakDocument.link = rs.getString("link");
				var informatieobjecttype = rs.getString("informatieobjecttype");
				ZgwInformatieObjectType documenttype = this.getZaakService().zgwClient.getZgwInformatieObjectTypeByUrl(informatieobjecttype);
				if (documenttype == null) {
					throw new ConverterException("getZgwInformatieObjectType #" + informatieobjecttype + " could not be found");
				}				
				zdsZaakDocument.omschrijving = documenttype.omschrijving;	
				
				ZdsHeeftRelevant heeftRelevant =  new ZdsHeeftRelevant();
				heeftRelevant.titel = rs.getString("titel");
				heeftRelevant.beschrijving = rs.getString("beschrijving");
				heeftRelevant.registratiedatum = rs.getString("registratiedatum");
				heeftRelevant.gerelateerde = zdsZaakDocument;
				gerelateerdeDocumenten.add(heeftRelevant);
				 
			 }
			 rs.close();
			 conn.close();
		}
		catch(SQLException sqlexception) {
			log.warn("could not execute sql:\n" + sql);
			throw new ConverterException("error while executing direct sqlquery", sqlexception.getMessage()  + "\n" + sql, sqlexception);
		}

		ZdsZakLa01LijstZaakdocumenten zdsZakLa01LijstZaakdocumenten = new ZdsZakLa01LijstZaakdocumenten(
				zdsZakLv01.stuurgegevens, this.getSession().getReferentienummer());
		zdsZakLa01LijstZaakdocumenten.antwoord = new ZdsAntwoordLijstZaakdocument();
		zdsZakLa01LijstZaakdocumenten.stuurgegevens = new ZdsStuurgegevens(zdsZakLv01.stuurgegevens,
				this.getSession().getReferentienummer());
		zdsZakLa01LijstZaakdocumenten.stuurgegevens.berichtcode = "La01";
		zdsZakLa01LijstZaakdocumenten.stuurgegevens.entiteittype = "ZAK";
		zdsZakLa01LijstZaakdocumenten.parameters = new ZdsParameters(zdsZakLv01.parameters);
		zdsZakLa01LijstZaakdocumenten.antwoord = new ZdsAntwoordLijstZaakdocument();
		zdsZakLa01LijstZaakdocumenten.antwoord.object = new ZdsObjectLijstZaakDocument();
		zdsZakLa01LijstZaakdocumenten.antwoord.object.heeftRelevant = gerelateerdeDocumenten;

		var response = XmlUtils.getSOAPMessageFromObject(zdsZakLa01LijstZaakdocumenten);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
