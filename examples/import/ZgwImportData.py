#!/usr/bin/python3.6
# -*- coding: utf-8 -*-
# Copyright 2019 - Gemeente Súdwest-Fryslân
#
# Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
# Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
# except in compliance with the Licence.
# 
# You may obtain a copy of the Licence at:
# http://ec.europa.eu/idabc/eupl.html
# 
# Unless required by applicable law or agreed to in writing, software distributed under 
# the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF 
# ANY KIND, either express or implied. See the Licence for the specific language 
# governing permissions and limitations under the Licence.
"""
Dit script is bedoeld om zaak-informatie van een locatie te importeren in een zaaksysteem. 
Van een locatie wordt de betreffende informatie geladen, waarna deze via zgw wordt aangeboden aan het zaaksysteem.

Dit script kan men aanroepen met de volgende parameters:

> `python ZgwImportData.py https://github.com/Sudwest-Fryslan/OpenZaakBrug/raw/master/examples/openzaak-export-catalogus-zaaktypes.zip`

In het bestand Config.py staat de configuraties van de locatie van het zaaksysteem, met de credentials
"""
__version__ = '0.1'
__author__ = 'Eduard Witteveen'
__copyright__ = 'Copyright (C) 2023 Gemeente Súdwest-Fryslân'
__license__ = 'EUPL'

# Generic part for all python scripts
from asyncio.windows_events import NULL
from email.policy import default
import io
import logging
import os
import datetime
import sys
import pkg_resources
#logging.basicConfig(level=logging.INFO, format='(%(threadName)-9s) %(message)s',)
loggingstream = io.StringIO()

logging.basicConfig(
    format='%(asctime)s %(levelname)-8s %(message)s',
#    level=logging.DEBUG,
    level=logging.INFO,
    datefmt='%Y-%m-%d %H:%M:%S',
    handlers=[
        #logging.FileHandler("debug.log"),
        logging.StreamHandler(),
        logging.StreamHandler(loggingstream)
    ]    
    )


logging.info("Start time:        " + str(datetime.datetime.now()))
logging.info("Script path:       " + os.path.realpath(__file__))
logging.info("Script version:    " + __version__)
logging.info("Working directory: " + os.path.realpath(os.getcwd()))
logging.info("Python version:    " + sys.version)
logging.info("Python modules:    " +  " ".join(sorted(["%s==%s" % (i.key, i.version) for i in pkg_resources.working_set])))
###############################################################
# Python version checks
###############################################################
# Python < 3
if sys.version_info[0] < 3:
    logging.error("Wrong version of Python, required verion: 3.X.X")
    sys.exit(2)


# Active configuration
import Config
import tempfile
import requests
import zipfile
import json
import yaml
import re
import urllib

config = Config.ReferenceZgwConfig

def getOpenApiSpecification(url):
    response = requests.get(url, allow_redirects=True)
    content = response.content.decode("utf-8")
    specification = yaml.safe_load(content)
    return specification

header = None
def getHeaders(jwt_token_url = config.JWT_TOKEN_URL, jwt_client_id = config.JWT_CLIENT_ID, jwt_secret = config.JWT_SECRET):
    global header

    if not header is None:
        return header

    requestjson = {
        "clientIds": [
            jwt_client_id
        ],
        "secret": jwt_secret,
        "label": "user",
        "heeftAlleAutorisaties": 'true',
        "autorisaties": []
    }

    response = requests.post(jwt_token_url, json=requestjson)
    if response.status_code != 200:
        logging.warn(f"Error posting to the url: {jwt_token_url}\nObject:{requestjson}\n\tStatus Code: {response.status_code}\n\tResponse: {response.content}")
        return None
    responsejson = response.json()
    logging.debug(f"JwtTokenUrl {jwt_token_url} request: {requestjson} response: {responsejson}")
    bearer_token = responsejson['authorization']    
    # return {'Content-Type': 'application/json','Authorization': bearer_token, 'Accept-Crs': 'EPSG:4326', 'Content-Crs': 'EPSG:4326'}
    header = {'Content-Type': 'application/json','Authorization': bearer_token}
    return header

def httpGet(url):
    headers=getHeaders()
    logging.debug(f"httpGet to url: {url} with headers: {headers}" )
    response = requests.get(url, headers=headers)
    return response


def httpPost(url, json):
    headers=getHeaders()
    logging.debug(f"httpPost to url: {url} with headers: {headers} and json: {json}" )
    response = requests.post(url, json, headers=headers)
    return response

def importObject(zgwobjecturl, importdata, destinationpaths, destinationschemas, mapping, callstack):
    logging.info(f"Importing Object: {zgwobjecturl}")
    callstack.append(zgwobjecturl)
    
    # is this an url that we are importing?
    if zgwobjecturl not in importdata:
        logging.info(f"Skipping reference: {zgwobjecturl} , not in the importdata")
        return zgwobjecturl

    # did we already find/create the object?
    if zgwobjecturl in mapping:
        mappedurl = mapping[zgwobjecturl]
        logging.info(f"Already imported the object: {zgwobjecturl} at the new location: {mappedurl}")
        return mappedurl

    path = zgwobjecturl.split('/')[-2]

    # When we are a different organisation:
    if path == 'catalogussen' and len(config.OVERRIDE_CATALOGUS) > 0 :
        importdata[zgwobjecturl]['domein'] = config.OVERRIDE_CATALOGUS['domein']
        importdata[zgwobjecturl]['rsin'] = config.OVERRIDE_CATALOGUS['rsin']

    destination = destinationpaths[path]
    postdefinition = destination['post']
    if '$ref' in postdefinition['requestBody']['content']['application/json']['schema']:
        schema = postdefinition['requestBody']['content']['application/json']['schema']['$ref']
        destinationschema = destinationschemas[schema.split('/')[-1]]
    else:
        if path == 'informatieobjecttypen' : 
            destinationschema = destinationschemas['InformatieObjectType']
        

    destinationjson = {}
    searchfilter = {}
    for property in destinationschema['properties']:
        zwgobject = importdata[zgwobjecturl]
        readonly = destinationschema['properties'][property]['readOnly'] if 'readOnly' in destinationschema['properties'][property] else False
        datatype = destinationschema['properties'][property]['type'] if 'type' in destinationschema['properties'][property] else None
        typeformat = destinationschema['properties'][property]['format'] if 'format' in destinationschema['properties'][property] else None

        if path =='zaaktypen' and property == 'selectielijstProcestype' :
            zwgobject = zwgobject

        fieldname = re.sub( '(?<!^)(?=[A-Z])', '_', property).lower()

        if fieldname in zwgobject and not readonly and zwgobject[fieldname] is not None:
            value = zwgobject[fieldname]

            if typeformat == 'uri' or (path=='zaaktype-informatieobjecttypen' and fieldname == 'informatieobjecttype') :
# MELDEN: informatieobjecttype moet uri zijn
                # convert the uri if neccesary
                value = importObject(value, importdata, destinationpaths, destinationschemas, mapping, callstack)

            #if datatype == 'string':
            #    value = str(value)
            destinationjson[property] = value
        elif path =='informatieobjecttypen' and fieldname == 'informatieobjectcategorie' :
            # MELDEN: Verplicht nieuw veld
            destinationjson[property] = 'Onbekend'
        elif path =='zaaktypen' and fieldname == 'verantwoordelijke' :
            # MELDEN: Verplicht nieuw veld
            destinationjson[property] = 'Onbekend'

        else:
            logging.debug(f"Ignoring property: {property} readonly: {readonly} in-zgwobject: {fieldname in zwgobject}" )

# MELDEN: 
#   'brondatumArchiefprocedure' = {'afleidingswijze': 'ander_datumkenmerk', 'datumkenmerk': 'Vervaldatum besluit', 'einddatum_bekend': False, 'objecttype': 'overige', 'registratie': 'Besluit', 'procestermijn': 'P1D'}
#   {"type":"https://catalogi-api.vng.cloud/ref/fouten/ValidationError/","code":"invalid","title":"Invalid input.","status":400,"detail":"","instance":"urn:uuid:ff42657e-df93-4399-8564-01f1661e6b85","invalidParams":[{"name":"brondatumArchiefprocedure.procestermijn","code":"must-be-empty","reason":"This field must be empty for afleidingswijze `ander_datumkenmerk`"}]}
    if path == 'resultaattypen':
        if destinationjson['brondatumArchiefprocedure']['afleidingswijze'] == 'ander_datumkenmerk' :
            destinationjson['brondatumArchiefprocedure']['procestermijn'] = None


# MELDEN: Geen manier om secondary key te bevragen
    if path == 'catalogussen' :
        searchfilter['domein']= destinationjson['domein']
        searchfilter['rsin']= destinationjson['rsin']
    elif path == 'informatieobjecttypen' :
        searchfilter['catalogus']= destinationjson['catalogus']
        searchfilter['omschrijving']= destinationjson['omschrijving']
        searchfilter['status']= 'alles'
    elif path == 'zaaktypen' :
        searchfilter['catalogus']= destinationjson['catalogus']
        searchfilter['identificatie']= destinationjson['identificatie']
        searchfilter['status']= 'alles'
    elif path == 'resultaattypen' :
        searchfilter = searchfilter
# MELDEN:   Get filter op'omschrijving'
#           Combi is wel uniek
        searchfilter['zaaktype']= destinationjson['zaaktype']
#        searchfilter['omschrijving']= destinationjson['omschrijving']
        searchfilter['status']= 'alles'
    elif path == 'roltypen' :
# MELDEN:   Get filter op'omschrijving'
#           Combi is wel uniek
        searchfilter['zaaktype']= destinationjson['zaaktype']
#        searchfilter['omschrijving']= destinationjson['omschrijving']
        searchfilter['omschrijvingGeneriek']= destinationjson['omschrijvingGeneriek']
        searchfilter['status']= 'alles'
    elif path == 'statustypen' :
# MELDEN:   Get filter op'omschrijving'
#           Combi is wel uniek
        searchfilter['zaaktype']= destinationjson['zaaktype']
        searchfilter['status']= 'alles'
#   Hier geen omschrijvingGeneriek
#        searchfilter['omschrijvingGeneriek']= destinationjson['omschrijvingGeneriek']
    elif path == 'zaaktype-informatieobjecttypen' :
        searchfilter['zaaktype']= destinationjson['zaaktype']
        searchfilter['informatieobjecttype']= destinationjson['informatieobjecttype']

    if len(searchfilter) > 0:
        url = destinationpaths[path]['url'] + 'api/v1/' + path + "?" + urllib.parse.urlencode(searchfilter, doseq=True)       
        response = httpGet(url)        
        if response.status_code != 200:
            logging.warn(f"Error getting the url: {url}\n\tStatus Code: {response.status_code}\n\tResponse: {response.content}")
            return None
        else:
            jsonresponse = response.json()
            if jsonresponse['count'] > 0:
                for resultaat in jsonresponse['results']:
                    # additionalfilter 
                    if path == 'resultaattypen' or path == 'resultaattypen' or path =='statustypen':
                        if resultaat['omschrijving'] != destinationjson['omschrijving']:
                            continue
#                    if jsonresponse['count'] > 1:
#                    logging.warn(f"Unexpected result from url: {url}\n\tMore than 1 result, we use the first item\n\tResponse: {response.content}") 
                    jsonresponse = jsonresponse['results'][0]
                    mapping[zgwobjecturl] = jsonresponse['url']
                    logging.info(f"Object: {zgwobjecturl} already existed as: " + jsonresponse['url'])
                    return jsonresponse['url']

    # Serializing json  
    requestbody = json.dumps(destinationjson, indent = 4)
    url = destinationpaths[path]['url'] + 'api/v1/' + path

    # Create the new object
    response = httpPost(url, requestbody)
    if response.status_code not in (200, 201):
        logging.warn(f"Error posting to the url: {url}\nObject:{requestbody}\n\tStatus Code: {response.status_code}\n\tResponse: {response.content}")
        return None

    jsonresponse = response.json()
    mapping[zgwobjecturl] = jsonresponse['url']
    logging.info(f"Stored object: {zgwobjecturl} as: " + jsonresponse['url'])

    callstack.remove(zgwobjecturl)
    return jsonresponse['url']

def publiceerObject(zgwobjecturl, mapping):
    path = zgwobjecturl.split('/')[-2]
    if path in ('besluittypen', 'informatieobjecttypen', 'zaaktypen') :
        # zgwobject = httpGet(storedurl)
        url = mapping[zgwobjecturl] + '/publish' 
        response = httpPost(url, '')
        if response.status_code not in (200, 201):
            logging.warn(f"Error posting to the url: {url}\n\tStatus Code: {response.status_code}\n\tResponse: {response.content}")
            return None

if __name__ == '__main__':
    logging.info("=== Start ===")
    if len(sys.argv) <= 1 :
        logging.fatal('Invalid environment name, first parameter should be the url/location of the data to import')
        sys.exit(1)
        
    url = sys.argv[1]
    logging.info("Loading data from url: %s", url)
    filename = url.split('/')[-1]

    importdata = {}
    with tempfile.TemporaryDirectory() as directory:
        #filename = directory + filename
        logging.info("And saving it to the directory: %s", directory)
        req = requests.get(url)
        zipfile= zipfile.ZipFile(io.BytesIO(req.content))
        zipfile.extractall(directory)
        logging.info("Files stored to: %s", directory)
        
        for rootdir, dirs, files in os.walk(directory):
            for file in files:
                file = os.path.join(rootdir, file)
                logging.info("Loading data from: %s", file)
                
                fp = open(file)
                data = json.load(fp)

                for zgwobject in data:
                    url = zgwobject['url']
                    logging.debug("\tCaching zgwobject: %s", url)
                    importdata[url] = zgwobject
                fp.close()
        logging.info("All data loaded, removing directory: %s", directory)

    destinationpaths = {}
    destinationschemas = {}
    for endpoint in config.ZGW_URLS.keys():
        openapi = config.ZGW_URLS[endpoint]
        logging.info("Fetching definitions from: " + openapi)
        specification = getOpenApiSpecification(openapi)
        
        paths = specification['paths']
        for path in paths:                
            destinationpaths[path[1:]] = {
                'url' : endpoint,
                'openapi-path' : paths[path],
                'post' : paths[path].get('post', None)
            }
            logging.info("Loaded destinationpath:" + path[1:])

        components = specification['components']
        schemas = components['schemas']
        for schema in schemas:                
            destinationschemas[schema] = schemas[schema]
            logging.info("Loaded destinationschema:" + schema)
        

    mapping = {}
    for zgwobjecturl in importdata:
        importObject(zgwobjecturl, importdata, destinationpaths, destinationschemas, mapping, [])

    for zgwobjecturl in importdata:
        publiceerObject(zgwobjecturl, mapping)


    sys.exit(0)
