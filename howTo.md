- To generate project from archetype:
 mvn archetype:generate -Dappengine-version=1.8 -Djava8=true -DCloudSDK_Tooling=false -Dapplication-id=my-application-id -Dfilter=com.google.appengine.archetypes:

(setup guide: https://cloud.google.com/appengine/docs/standard/java/tools/maven)

	- Put application Java classes in src/main/java/
	- Configure application in src/main/webapp/WEB-INFO/appengine-web.xml
	- Configure deployment in src/main/webapp/WEB-INF/web.xml
	- Documentation: https://cloud.google.com/appengine/docs/standard/java/config/appref


AppEngine Server
- To build:
mvn clean package

- To test:
mvn appengine:devserver

(specify testing port in pom.xml plugin appengine-maven-plugin)

- To deploy:
mvn appengine:update
Then go to:
http://your-appengine-property-id.appspot.com


Client
- To build:
mvn clean compile assembly:single

- To run:
java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar
