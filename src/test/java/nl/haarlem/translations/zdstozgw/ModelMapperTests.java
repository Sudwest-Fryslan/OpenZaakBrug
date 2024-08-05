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
package nl.haarlem.translations.zdstozgw;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import nl.haarlem.translations.zdstozgw.config.ModelMapperConfig;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsHeeft;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocument;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObjectPost;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ModelMapperConfig.class, BuildProperties.class})
public class ModelMapperTests {

    @Autowired
    ModelMapper modelMapper;

    @Test
    public void zgwEnkelvoudigInformatieObjectToZdsZaakDocument_shouldMapCorrectly(){
        //assign
    	System.setProperty("user.timezone", "CET");
    	ModelMapperConfig.singleton.timeoffset = "0";
        ZgwEnkelvoudigInformatieObjectPost zgwEnkelvoudigInformatieObject = new ZgwEnkelvoudigInformatieObjectPost()
                .setBestandsnaam("bestandsnaam")
                .setInhoud("inhoud")
                .setAuteur("auteur")
                .setBeschrijving("beschrijving")
                .setBronorganisatie("bronorganisatie")
                .setCreatiedatum("2020-02-30")
                .setFormaat("formaat")
                .setIdentificatie("identificatie")
                .setInformatieobjecttype("informatieobjecttype")
                .setOntvangstdatum("2020-06-20")
                .setStatus("status")
                .setTaal("taal")
                .setTitel("titel")
                .setUrl("url")
                .setVersie("versie")
                .setVertrouwelijkheidaanduiding("vertrouwelijkheidaanduiding")
                .setVerzenddatum("2020-05-09");
        // String expectedCreatieDatum = "20200230";
        // TODO: use gooed expectd values
        String expectedCreatieDatum = "20200229";

        //act
        ZdsZaakDocument zdsZaakDocument = modelMapper.map(zgwEnkelvoudigInformatieObject, ZdsZaakDocument.class);

        //assert
        assertEquals(expectedCreatieDatum, zdsZaakDocument.getCreatiedatum());
    }

    @Test
    public void convertStufDateTimeToZgwDateTime_shouldAddTwoHoursInUTCWhenDayInSummer(){
        //assign
    	System.setProperty("user.timezone", "CET");
    	ModelMapperConfig.singleton.timeoffset = "0";
        ZdsHeeft zdsHeeft = new ZdsHeeft().setDatumStatusGezet("20200904103404929");
        String expectedDatum = "2020-09-04T08:34:04.920000Z";

        //act
        ZgwStatus zgwStatus =  modelMapper.map(zdsHeeft, ZgwStatus.class);

        //assert
        assertEquals(expectedDatum, zgwStatus.getDatumStatusGezet());
    }

    @Test
    public void convertStufDateTimeToZgwDateTime_shouldAddOneHourInUTCWhenDayInWinter(){
        //assign
    	System.setProperty("user.timezone", "CET");
    	ModelMapperConfig.singleton.timeoffset = "0";
        ZdsHeeft zdsHeeft = new ZdsHeeft().setDatumStatusGezet("20200101103404929");
        String expectedDatum = "2020-01-01T09:34:04.920000Z";

        //act
        ZgwStatus zgwStatus =  modelMapper.map(zdsHeeft, ZgwStatus.class);

        //assert
        assertEquals(expectedDatum, zgwStatus.getDatumStatusGezet());
    }
}
