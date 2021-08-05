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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import nl.haarlem.translations.zdstozgw.config.ConfigService;
//import nl.haarlem.translations.zdstozgw.config.model.Replication;
//import nl.haarlem.translations.zdstozgw.config.model.ResponseType;
import nl.haarlem.translations.zdstozgw.config.model.Translation;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConfigServiceTests {

    @Autowired
    ConfigService configService;

    @Ignore
    @Test
    public void getTranslationBySoapActionAndApplicatie_whenApplicatieAvailable_shouldReturnApplicationSpecificTranslation() {
        //assign
        String soapAction = "http://www.egem.nl/StUF/sector/zkn/0310/genereerDocumentIdentificatie_Di02";
        String applicatie = "GWS4all";

        //act
        Translation result = configService.getTranslationByPathAndSoapAction(soapAction, applicatie);

        //assert
        Assert.assertTrue(result != null);

    }

    @Ignore
    @Test
    public void getTranslationBySoapActionAndApplicatie_whenApplicatieNull_shouldReturnDefaultTranslation() {
        //assign
        String soapAction = "http://www.egem.nl/StUF/sector/zkn/0310/voegZaakdocumentToe_Lk01";

        //act
        Translation result = configService.getTranslationByPathAndSoapAction(soapAction, null);

        //assert
        Assert.assertTrue(result != null);

    }

//    @Test
//    public void getReplication_shouldReturnCorrectObject() {
//        //assign
//        Replication expectedResult = new Replication()
//                .setEnableZDS(true)
//                .setEnableZGW(true)
//                .setResponseType(ResponseType.ZDS);
//
//        //act
//        Replication result = configService.getConfiguratie().getReplication();
//
//        //assert
//        Assert.assertEquals(expectedResult, result);
//
//    }

}
