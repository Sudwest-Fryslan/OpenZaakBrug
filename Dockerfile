FROM openjdk:11-jdk
ENV TZ=Europe/Amsterdam
WORKDIR /home
RUN mkdir lib
COPY ./target/zds-to-zgw.jar zds-to-zgw.jar
ENTRYPOINT ["java","-Dloader.path=lib", "-jar", "zds-to-zgw.jar"]
