FROM maven:3-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml ./pom.xml
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package

FROM jetty:9.4.51-jre17-alpine-eclipse-temurin
COPY --from=build /app/target/*.war $JETTY_BASE/webapps/api.war
CMD ["java", "-jar", "/usr/local/jetty/start.jar"]