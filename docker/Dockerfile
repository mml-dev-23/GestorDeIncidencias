FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY GestorDeIncidencias.jar app.jar
COPY lib/ lib/

ENTRYPOINT ["java", "-jar", "app.jar"]
