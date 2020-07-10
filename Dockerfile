FROM alpine/git as clone
WORKDIR /app
RUN git clone https://github.com/jsloane/SwordfishSync.git

FROM maven:3.5-jdk-8-alpine as build
WORKDIR /app
COPY --from=clone /app/spring-petclinic /app
RUN mvn package

FROM tomcat:9.0.20-jre8-alpine
COPY --from=build /app/SwordfishSync/sfs-client/target/sfs-client.war /usr/local/tomcat/webapps/
#COPY --from=build /app/SwordfishSync/sfs-server/target/sfs-server.war /usr/local/tomcat/webapps/

EXPOSE 8080
ENTRYPOINT ["sh", "-c"]
CMD ["catalina.sh", "run"]