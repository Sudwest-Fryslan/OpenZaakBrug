FROM openjdk:11-jdk
ENV TZ=Europe/Amsterdam
WORKDIR /home
RUN mkdir lib && mkdir data
RUN curl -H "Accept: application/zip" -k https://nexus.ibissource.org/repository/OpenZaakBrugExtensions/nl/sudwestfryslan/translations/ozb-extensions/1.0.1-SNAPSHOT/ozb-extensions-1.0.1-20220822.084521-8.jar > lib/extension.jar
RUN curl -H "Accept: application/zip" -k https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/21.1.0.0/ojdbc11-21.1.0.0.jar > lib/oracle.jar
COPY ./target/zds-to-zgw.jar zds-to-zgw.jar
ENTRYPOINT ["java","-Dloader.path=lib", "-jar", "zds-to-zgw.jar"]
