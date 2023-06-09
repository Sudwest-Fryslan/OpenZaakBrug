#!/bin/sh

mv ./src/main/resources/application.properties_example ./src/main/resources/application.properties
mv ./src/main/resources/config.json_example ./src/main/resources/config.json

mvn install -Dmaven.javadoc.skip=true -B -V -DskipTests

sudo docker-compose -f docker-compose.yml up --build -d

# start the counter from 5 seconds
timeCounter=5

# wait timeCounter seconds for application to start up
sleep $timeCounter
# get status of the endpoint
status_code=$(curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/debug)
# check if the endpoint is available 
while [ "${status_code}" -ne 200 -a $timeCounter -le $MAX_APP_START_TIME ]
do
	status_code=$(curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/debug)
	timeCounter=`expr $timeCounter + $WAIT_INTERVAL`
	sleep $WAIT_INTERVAL
done

if [ "${status_code}" -eq 200 ]; then
	echo "=== Debug is available. Tests should be triggered here. ==="
	echo $(curl --data "<SOAP-ENV:Envelope xsi:schemaLocation=\"http://www.egem.nl/StUF/sector/zkn/0310 zkn0310-p28\zs-dms\zkn0310_msg_zs-dms_resolved.xsd\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:StUF=\"http://www.egem.nl/StUF/StUF0301\" xmlns:ZKN=\"http://www.egem.nl/StUF/sector/zkn/0310\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><SOAP-ENV:Header/><SOAP-ENV:Body><ZKN:genereerZaakIdentificatie_Di02><ZKN:stuurgegevens><StUF:berichtcode>Di02</StUF:berichtcode><StUF:zender><StUF:organisatie>1900</StUF:organisatie><StUF:applicatie>GWS4all</StUF:applicatie><StUF:gebruiker>Gebruiker</StUF:gebruiker></StUF:zender><StUF:ontvanger><StUF:organisatie>1900</StUF:organisatie><StUF:applicatie>CDR</StUF:applicatie></StUF:ontvanger><StUF:referentienummer>a25e6e13-4786-4bf0-9afc-1ef75b56463f</StUF:referentienummer><StUF:tijdstipBericht>20201207150645509</StUF:tijdstipBericht><StUF:functie>genereerZaakidentificatie</StUF:functie></ZKN:stuurgegevens></ZKN:genereerZaakIdentificatie_Di02></SOAP-ENV:Body></SOAP-ENV:Envelope>" -X POST -H "Content-Type: text/xml" -H "SOAPAction: \"http://www.egem.nl/StUF/sector/zkn/0310/genereerZaakIdentificatie_Di02\"" http://localhost:8080/translate/generic/zds/VrijBericht)
else
	echo "=== HTTP_CODE: ${status_code} ==="
	echo "=== Something went wrong while starting the application check log files. ==="
	exit 1
fi
sudo docker-compose logs
