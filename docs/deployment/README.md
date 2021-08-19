# Open ZaakBrug deployment Instructions 


## Commit/PR-CI

In the Open ZaakBrug repository we use github actions for the ci/cd. 
First of all the CI tool will build the application and run the unit tests.
Afterwards, it will start the application in docker container (this step might be redundant however 
we had some problems in the past that the application was not working properly in docker container)
Just to see that the OZB performs well in docker container, ci tool (using curl) will ask OZB to generate zaak identification.
Afterwards, if the event type is 'push' to master or release-* branches the docker image will be pushed to docker hub.
(https://hub.docker.com/repository/docker/openzaakbrug/openzaakbrug)

## Versioning

master and the release-* branches have a branch protection rule and require a pull request review to be able to merge.
Pull request merge action will result in a 'push' event.
 - Any push event to master will update the 'latest' tagged version in docker hub.
 - Any push event to release-{version} branch will create/update {version}-latest tagged image in docker hub.

Creating releases will result in a new version in the repository. (For creating releases in github see https://docs.github.com/en/github/administering-a-repository/releasing-projects-on-github/managing-releases-in-a-repository).

### Making Releases

To have a new version in docker hub registry it is required to create a release. A release can be created in both master and release-* branches.
'create release' event will trigger the release workflow. Basically, release workflow will push an image to docker hub by using the given name to the tag while creating a release as the version number. Tag name can be in the following format MAJOR.MINOR.PATCH. (See https://semver.org/ for more information).

### Examples

| Branch                                    | Github Event     | Release Tag   | Docker image Tag | 
| ----------------------------------------- | ---------------- | ------------- | ---------------- |
| master                                    | push 			   | Not effective | latest  	      | 
| master                                    | release          | 1.2.1         | 1.2.1            | 
| release-1.2                               | push             | Not effective | 1.2-latest       | 
| release-1.2                               | release 		   | 1.2.5         | 1.2.5  		  | 

Basically to have an independent version creating release is mandatory. Other actions will only overwrite the existing versions.(Containing 'latest' naming convention.)

## Configuration update
In OZB, there are two configuration files which may require changes for the deployment. Namely, _application.properties_ and _config.json_. These files are located in _/root/config_ folder and mounted to the docker container.
Analyze PR to check whether the deployment requires changes in one of these files or not. Then, do the change.

## Deployment
Time to update the docker image. There is a script called update_ozb.sh in the _/root_ folder of the **root** user. An example of the file is also present in github.
Run 

	./update_ozb.sh <version>
to update the docker image. If something goes wrong during the update it will be visible in the terminal. Then the log file of the deployment needs to be examined. 
There is a folder called _update_ozb_history_ in the _/root_ folder which keeps the log files of the deployments with the name in the following format _```update_ozb_<timestamp>.log```_
Depending on the error message troubleshooting may differ. 

## Post deployment
It is always a good practice to check the basic consequences of the deployment. 
I always run 

	docker logs OpenZaakBrug 

command to display the startup log of OZB (just to verify that no exception is occurred while starting the application)

Example output after a successfull deployment:

	2021-03-30 12:56:42.322  INFO 1 --- [           main] n.h.translations.zdstozgw.Application    : Starting Application v0.0.2 on SERVERNAME with PID 1 (/home/zds-to-zgw.jar started by root in /home)
	2021-03-30 12:56:42.325  INFO 1 --- [           main] n.h.translations.zdstozgw.Application    : No active profile set, falling back to default profiles: default
	2021-03-30 12:56:43.927  INFO 1 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
	2021-03-30 12:56:44.054  INFO 1 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 115ms. Found 4 JPA repository interfaces.
	2021-03-30 12:56:44.608  INFO 1 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration' of type [org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
	2021-03-30 12:56:45.013  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
	2021-03-30 12:56:45.028  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
	2021-03-30 12:56:45.029  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.30]
	2021-03-30 12:56:45.121  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
	2021-03-30 12:56:45.122  INFO 1 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 2712 ms
	2021-03-30 12:56:45.535  INFO 1 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
	2021-03-30 12:56:45.649  INFO 1 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate Core {5.4.10.Final}
	2021-03-30 12:56:45.832  INFO 1 --- [           main] o.hibernate.annotations.common.Version   : HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
	2021-03-30 12:56:45.991  INFO 1 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
	2021-03-30 12:56:46.151  INFO 1 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
	2021-03-30 12:56:46.182  INFO 1 --- [           main] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.dialect.PostgreSQLDialect
	2021-03-30 12:56:47.333  INFO 1 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
	2021-03-30 12:56:47.344  INFO 1 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
	2021-03-30 12:56:47.799  INFO 1 --- [           main] n.h.t.zdstozgw.config.ModelMapperConfig  : nl.haarlem.translations.zdstozgw.timeoffset.minutes = -5
	2021-03-30 12:56:50.971  WARN 1 --- [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
	2021-03-30 12:56:51.206  INFO 1 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
	2021-03-30 12:56:51.682  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
	2021-03-30 12:56:51.685  INFO 1 --- [           main] n.h.translations.zdstozgw.Application    : Started Application in 10.315 seconds (JVM running for 10.959)

In case the deployment is expected to make changes in the database also check the database to confirm that the expected changes are in place.

You are all settled :)

## Logging
By default docker will log the stdout of the application and store it in _`/var/lib/docker/containers/<container-id>`_
The log file is a json file and is rotating with 10 files 10MB each. These settings can be adjusted by editing the file _/etc/docker/deamon.json_
After editing the file do not forget to restart the deamon (```systemctl restart dockerd```)

## Nginx 
In case nginx requires some adjustments the config file can be found in _/etc/nginx/conf.d/_.
Do not forget to restart nginx after making changes (```systemctl restart nginx```)

## Asked questions

#### Docker volumes

	In het update script zie ik: -v /root/config:/home/config -v data-debug:/home/data. Waar kan ik de data-debug folder vinden? Zit niet onder /root zo te zien, maar waar dan wel? 

	The former will mount the folder in the host machine to the docker container and the latter(data-debug) is the name of the volume. It will keep the persisted data in /home/data folder in the docker container. Available volumes can be seen by executing the command:  docker volume ls





