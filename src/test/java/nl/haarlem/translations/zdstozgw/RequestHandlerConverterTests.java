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


import nl.haarlem.translations.zdstozgw.config.ConfigService;
import nl.haarlem.translations.zdstozgw.config.model.Configuration;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestHandler;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestHandlerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
public class RequestHandlerConverterTests {
    RequestHandlerFactory requestHandlerFactory;

    @Mock
    ConfigService configService;

    @Before
    public void setup() {
        this.requestHandlerFactory = new RequestHandlerFactory(configService);
    }

    @Test
    public void getRequestHandler_shouldReturnCorrectRequestHandler() throws Exception {
        //assign
        String expectedClass = "nl.haarlem.translations.zdstozgw.requesthandler.impl.BasicRequestHandler";
        Configuration configuration = new Configuration()
                .setRequestHandlerImplementation(expectedClass);
        doReturn(configuration).when(configService).getConfiguration();

        //act
        RequestHandler requestHandler = requestHandlerFactory.getRequestHandler(null);

        //assert
        Assert.assertEquals(expectedClass, requestHandler.getClass().getCanonicalName());
    }
}
