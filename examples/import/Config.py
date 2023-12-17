# Config.py
class Config:
    # 
    IMPLEMENTATION_CONFIG = ReferenceZgwConfig
    JWT_CLIENT_ID = 'id'
    JWT_SECRET = 'secret'

    ORDER_OF_PERSISTENCE = ["/api/v1/catalogussen/", "/api/v1/zaaktypen"]

    # OVERRIDE_CATALOGUS = {}
    OVERRIDE_CATALOGUS = {
        'domein' : 'SWF',
        'rsin' : '823288444'
        }

class ReferenceZgwConfig(Config):
    JWT_TOKEN_URL = 'https://zaken-auth.vng.cloud/api/v1/register'
	
    ZGW_URLS =  {
        'https://zaken-api.vng.cloud/': 'https://raw.githubusercontent.com/vng-Realisatie/zaken-api/1.5.0/src/openapi.yaml',
        'https://documenten-api.vng.cloud/': 'https://raw.githubusercontent.com/VNG-Realisatie/gemma-documentregistratiecomponent/1.4.1/src/openapi.yaml',
        'https://catalogi-api.vng.cloud/' : 'https://raw.githubusercontent.com/VNG-Realisatie/catalogi-api/1.3.0/src/openapi.yaml',
        'https://besluiten-api.vng.cloud/' : 'https://raw.githubusercontent.com/VNG-Realisatie/besluiten-api/1.0.2/src/openapi.yaml'
        }

class OpenZaakConfig(Config):	
    ZGW_URLS =  {
        'https://test.openzaak.nl/zaken/': 'https://test.openzaak.nl/zaken/api/v1/schema/openapi.yaml',
        'https://test.openzaak.nl/documenten/': 'https://test.openzaak.nl/documenten/api/v1/schema/openapi.yaml',
        'https://test.openzaak.nl/catalogi/': 'https://test.openzaak.nl/catalogi/api/v1/schema/openapi.yaml',
        'https://test.openzaak.nl/besluiten/': 'https://test.openzaak.nl/besluiten/api/v1/schema/openapi.yaml'
        }
