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

import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZakLk01ActualiseerZaakstatus;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

@SpringBootTest
public class XmlUnitTests {

    @Test
    public void contextLoads() {
    }

    @Ignore
    @Test
    public void getStUFObject_whenParsingActualiseerZaakstatus_convertsRequiredNodes() {
        try {
            //assign
            String content = IOUtils.toString(getClass()
                    .getClassLoader()
                    .getResourceAsStream("zds1.1/ActualiseerZaakstatus"), "UTF-8");

            //actheb
            ZdsZakLk01ActualiseerZaakstatus zdsZakLk01ActualiseerZaakstatus = (ZdsZakLk01ActualiseerZaakstatus) XmlUtils.getStUFObject(content, ZdsZakLk01ActualiseerZaakstatus.class);
            var object = zdsZakLk01ActualiseerZaakstatus.objects.get(1);

            //assert
            assumeTrue(object.identificatie != null);
            assumeTrue(object.heeft.get(0).datumStatusGezet != null);
            assumeTrue(object.heeft.get(0).gerelateerde.volgnummer != null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
