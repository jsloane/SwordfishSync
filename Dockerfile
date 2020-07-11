FROM alpine/git as clone
WORKDIR /app
RUN git clone --single-branch --branch master https://github.com/jsloane/swordfishsync.git

FROM maven:3.5.3-jdk-8 as download-dependencies
WORKDIR /app
COPY --from=clone /app/swordfishsync /app
RUN mvn -s /usr/share/maven/ref/settings-docker.xml dependency:resolve

FROM maven:3.5.3-jdk-8 as build
WORKDIR /app
COPY --from=download-dependencies /app /app
COPY --from=download-dependencies /usr/share/maven/ref/ /root/.m2
RUN mvn clean package

FROM tomcat:9.0.20-jre8-alpine
COPY --from=build /app/sfs-client/target/sfs-client.war /usr/local/tomcat/webapps/
COPY --from=build /app/sfs-server/target/sfs-server.war /usr/local/tomcat/webapps/

EXPOSE 8080
CMD ["catalina.sh", "run"]