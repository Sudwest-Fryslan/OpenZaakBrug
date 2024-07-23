#!/usr/bin/python3.6
# -*- coding: utf-8 -*-
# Copyright 2024 - Gemeente Súdwest-Fryslân
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
__version__ = '0.2'
__author__ = 'Eduard Witteveen'
__copyright__ = 'Copyright (C) 2024 Gemeente Súdwest-Fryslân'
__license__ = 'EUPL'

# Generic part for all python scripts
import io
import logging
import os
import datetime
import sys
import pkg_resources
import tempfile
import requests
import zipfile
import json
import yaml
import re
import urllib
import jwt

#from cgitb import text

# Setup logging
loggingstream = io.StringIO()

logging.basicConfig(
    format='%(asctime)s %(levelname)-8s %(message)s',
    level=logging.INFO,
    datefmt='%Y-%m-%d %H:%M:%S',
    handlers=[
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

# Python version checks
if sys.version_info[0] < 3:
    logging.error("Wrong version of Python, required version: 3.X.X")
    sys.exit(2)

# Active configuration
import Config

config = Config.CurrentConfig

def getOpenApiSpecification(url):
    response = requests.get(url, allow_redirects=True)
    content = response.content.decode("utf-8")
    specification = yaml.safe_load(content)
    return specification

header = None

def generate_jwt(client_id, secret):
    payload = {
        'iss': client_id,
        'iat': datetime.datetime.utcnow(),
        'exp': datetime.datetime.utcnow() + datetime.timedelta(minutes=10),
        'client_id': client_id,
        'user_id': client_id,
        'user_representation': client_id
    }
    token = jwt.encode(payload, secret, algorithm='HS256')
    return token

def getHeaders(jwt_token_url=config.JWT_TOKEN_URL, jwt_client_id=config.JWT_CLIENT_ID, jwt_secret=config.JWT_SECRET):
	global header

	if header is not None:
		return header

	if jwt_token_url is not None:
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
			logging.warn(f"Error posting to the url: {jwt_token_url}\nObject: {requestjson}\n\tStatus Code: {response.status_code}\n\tResponse: {response.content}")
			return None
		responsejson = response.json()
		logging.debug(f"JwtTokenUrl {jwt_token_url} request: {requestjson} response: {responsejson}")
		bearer_token = responsejson['authorization']
		header = {'Content-Type': 'application/json', 'Authorization': bearer_token}
	else:
		token = generate_jwt(jwt_client_id, jwt_secret)
		logging.debug(f"For url: {jwt_token_url} using: 'Bearer {token}' without using a jwtUrl")
		header = {'Content-Type': 'application/json', 'Authorization': f'Bearer {token}'}

		return header

def httpGet(url):
    headers = getHeaders()
    logging.debug(f"httpGet to url: {url} with headers: {headers}")
    response = requests.get(url, headers=headers)
    return response

def httpPost(url, jsonstring):
    headers = getHeaders()
    logging.debug(f"httpPost to url: {url} with headers: {headers} and jsonstring: {jsonstring}")
    try:
        jsondata = json.loads(jsonstring)
    except json.JSONDecodeError as e:
        logging.error(f"Failed to decode JSON string: {jsonstring}. Error: {e}")
        return None
    response = requests.post(url, json=jsondata, headers=headers)
    return response

def importObject(zgwobjecturl, importdata, destinationpaths, destinationschemas, mapping, callstack):
    logging.debug(f"Importing Object: {zgwobjecturl}")
    callstack.append(zgwobjecturl)

    if zgwobjecturl not in importdata:
        logging.debug(f"Skipping reference: {zgwobjecturl}, not in the importdata")
        return zgwobjecturl

    if zgwobjecturl in mapping:
        mappedurl = mapping[zgwobjecturl]
        logging.debug(f"Already imported the object: {zgwobjecturl} at the new location: {mappedurl}")
        return mappedurl

    path = zgwobjecturl.split('/')[-2]

    if path == 'catalogussen' and len(config.OVERRIDE_CATALOGUS) > 0:
        importdata[zgwobjecturl]['domein'] = config.OVERRIDE_CATALOGUS['domein']
        importdata[zgwobjecturl]['rsin'] = config.OVERRIDE_CATALOGUS['rsin']

    destination = destinationpaths[path]
    postdefinition = destination['post']
    if '$ref' in postdefinition['requestBody']['content']['application/json']['schema']:
        schema = postdefinition['requestBody']['content']['application/json']['schema']['$ref']
        destinationschema = destinationschemas[schema.split('/')[-1]]
    else:
        if path == 'informatieobjecttypen':
            destinationschema = destinationschemas['InformatieObjectType']

    destinationjson = {}
    searchfilter = {}
    for property in destinationschema['properties']:
        zwgobject = importdata[zgwobjecturl]
        readonly = destinationschema['properties'][property].get('readOnly', False)
        datatype = destinationschema['properties'][property].get('type', None)
        typeformat = destinationschema['properties'][property].get('format', None)

        if path == 'zaaktypen' and property == 'selectielijstProcestype':
            zwgobject = zwgobject

        fieldname = re.sub(r'(?<!^)(?=[A-Z])', '_', property).lower()

        if fieldname in zwgobject and not readonly and zwgobject[fieldname] is not None:
            value = zwgobject[fieldname]

            if typeformat == 'uri' or (path == 'zaaktype-informatieobjecttypen' and fieldname == 'informatieobjecttype'):
                value = importObject(value, importdata, destinationpaths, destinationschemas, mapping, callstack)

            destinationjson[property] = value
        elif path == 'informatieobjecttypen' and fieldname == 'informatieobjectcategorie':
            destinationjson[property] = 'Onbekend'
        elif path == 'zaaktypen' and fieldname == 'verantwoordelijke':
            destinationjson[property] = 'Onbekend'
        else:
            logging.debug(f"Ignoring property: {property} readonly: {readonly} in-zgwobject: {fieldname in zwgobject}")

    if path == 'resultaattypen':
        if destinationjson['brondatumArchiefprocedure']['afleidingswijze'] == 'ander_datumkenmerk':
            destinationjson['brondatumArchiefprocedure']['procestermijn'] = None

    if path == 'catalogussen':
        searchfilter['domein'] = destinationjson['domein']
        searchfilter['rsin'] = destinationjson['rsin']
    elif path == 'informatieobjecttypen':
        searchfilter['catalogus'] = destinationjson['catalogus']
        searchfilter['omschrijving'] = destinationjson['omschrijving']
        searchfilter['status'] = 'alles'
    elif path == 'zaaktypen':
        searchfilter['catalogus'] = destinationjson['catalogus']
        searchfilter['identificatie'] = destinationjson['identificatie']
        searchfilter['status'] = 'alles'
    elif path == 'resultaattypen':
        searchfilter['zaaktype'] = destinationjson['zaaktype']
        searchfilter['status'] = 'alles'
    elif path == 'roltypen':
        searchfilter['zaaktype'] = destinationjson['zaaktype']
        searchfilter['omschrijvingGeneriek'] = destinationjson['omschrijvingGeneriek']
        searchfilter['status'] = 'alles'
    elif path == 'statustypen':
        searchfilter['zaaktype'] = destinationjson['zaaktype']
        searchfilter['status'] = 'alles'
    elif path == 'zaaktype-informatieobjecttypen':
        searchfilter['zaaktype'] = destinationjson['zaaktype']
        searchfilter['informatieobjecttype'] = destinationjson['informatieobjecttype']

    if len(searchfilter) > 0:
        url = destinationpaths[path]['url'] + 'api/v1/' + path + "?" + urllib.parse.urlencode(searchfilter, doseq=True)
        response = httpGet(url)
        if response.status_code != 200:
            logging.warning(f"Error getting the url: {url}\n\tStatus Code: {response.status_code}\n\tResponse: {response.content}")
            return None
        else:
            jsonresponse = response.json()
            if jsonresponse['count'] > 0:
                for resultaat in jsonresponse['results']:
                    if path in ('resultaattypen', 'statustypen') and resultaat['omschrijving'] != destinationjson['omschrijving']:
                        continue
                    jsonresponse = jsonresponse['results'][0]
                    mapping[zgwobjecturl] = jsonresponse['url']
                    logging.debug(f"Object: {zgwobjecturl} already existed as: " + jsonresponse['url'])
                    return jsonresponse['url']

    requestbody = json.dumps(destinationjson, indent=4)
    url = destinationpaths[path]['url'] + 'api/v1/' + path

    response = httpPost(url, requestbody)
    if response.status_code not in (200, 201):
        logging.warning(f"Error posting to the url: {url}\nObject: {requestbody}\n\tStatus Code: {response.status_code}\n\tResponse: {response.content}")
        return None

    jsonresponse = response.json()
    mapping[zgwobjecturl] = jsonresponse['url']
    logging.info(f"Stored object: {zgwobjecturl} as: " + jsonresponse['url'])

    callstack.remove(zgwobjecturl)
    return jsonresponse['url']

def publiceerObject(zgwobjecturl, mapping):
    path = zgwobjecturl.split('/')[-2]
    if path in ('besluittypen', 'informatieobjecttypen', 'zaaktypen'):
        url = mapping[zgwobjecturl] + '/publish'
        response = httpPost(url, '')
        if response.status_code not in (200, 201):
            logging.warning(f"Error posting to the url: {url}\n\tStatus Code: {response.status_code}\n\tResponse: {response.content}")
            return None

if __name__ == '__main__':
    logging.info("=== Start ===")
    if len(sys.argv) <= 1:
        logging.fatal('Invalid environment name, first parameter should be the url/location of the data to import')
        sys.exit(1)
        
    url = sys.argv[1]
    logging.info("Loading data from url: %s", url)
    filename = url.split('/')[-1]

    importdata = {}
    with tempfile.TemporaryDirectory() as directory:
        logging.info("And saving it to the directory: %s", directory)
        req = requests.get(url)
        with zipfile.ZipFile(io.BytesIO(req.content)) as zip_ref:
            zip_ref.extractall(directory)
        logging.info("Files stored to: %s", directory)
        
        for rootdir, dirs, files in os.walk(directory):
            for file in files:
                file_path = os.path.join(rootdir, file)
                logging.info("Loading data from: %s", file_path)
                
                with open(file_path) as fp:
                    data = json.load(fp)

                    for zgwobject in data:
                        url = zgwobject['url']
                        logging.debug("\tCaching zgwobject: %s", url)
                        importdata[url] = zgwobject
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
                'url': endpoint,
                'openapi-path': paths[path],
                'post': paths[path].get('post', None)
            }
            logging.debug("Loaded destinationpath: " + path[1:])

        components = specification['components']
        schemas = components['schemas']
        for schema in schemas:
            destinationschemas[schema] = schemas[schema]
            logging.debug("Loaded destinationschema: " + schema)

    mapping = {}
    for zgwobjecturl in importdata:
        importObject(zgwobjecturl, importdata, destinationpaths, destinationschemas, mapping, [])

    for zgwobjecturl in importdata:
        publiceerObject(zgwobjecturl, mapping)

    sys.exit(0)
