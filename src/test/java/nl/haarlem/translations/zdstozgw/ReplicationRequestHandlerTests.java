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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import nl.haarlem.translations.zdstozgw.config.ConfigService;
//import nl.haarlem.translations.zdstozgw.config.model.Replication;
//import nl.haarlem.translations.zdstozgw.config.model.ResponseType;
import nl.haarlem.translations.zdstozgw.requesthandler.impl.LoggingRequestHandler;

@Ignore
@RunWith(SpringRunner.class)
public class ReplicationRequestHandlerTests {

    LoggingRequestHandler replicationRequestHandler;

    @Mock
    ConfigService configService;

    @Before
    public void setup() {
        this.replicationRequestHandler = new LoggingRequestHandler(null, configService);
    }


//    @Test(expected = RuntimeException.class)
//    public void execute_whenModusDisabled_shouldThrowError() {
//        //assign
//        Configuratie configuratie = new Configuratie()
//                .setReplication(new Replication()
//                        .setEnableZDS(false)
//                        .setEnableZGW(false));
//
//        doReturn(configuratie).when(configService).getConfiguratie();
//
//        //act
//        this.replicationRequestHandler.execute("test", null, null);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void execute_whenResponseTypeZGWDoesNotMatchModus_shouldThrowError() {
//        //assign
//        Configuratie configuratie = new Configuratie()
//                .setReplication(new Replication()
//                        .setEnableZDS(true)
//                        .setEnableZGW(false)
//                        .setResponseType(ResponseType.ZGW));
//
//        doReturn(configuratie).when(configService).getConfiguratie();
//
//        //act
//        this.replicationRequestHandler.execute("test", null, null);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void execute_whenResponseTypeZDSDoesNotMatchModus_shouldThrowError() {
//        //assign
//        Configuratie configuratie = new Configuratie()
//                .setReplication(new Replication()
//                        .setEnableZDS(false)
//                        .setEnableZGW(true)
//                        .setResponseType(ResponseType.ZDS));
//
//        doReturn(configuratie).when(configService).getConfiguratie();
//
//        //act
//        this.replicationRequestHandler.execute("test", null, null);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void execute_whenResponseTypeIsNull_shouldThrowError() {
//        //assign
//        Configuratie configuratie = new Configuratie()
//                .setReplication(new Replication()
//                        .setEnableZDS(true)
//                        .setEnableZGW(true)
//                        .setResponseType(null));
//
//        doReturn(configuratie).when(configService).getConfiguratie();
//
//        //act
//        this.replicationRequestHandler.execute("test", null, null);
//    }

}
