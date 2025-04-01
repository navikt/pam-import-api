FROM gcr.io/distroless/java21-debian12

COPY build/libs/pam-import-api-*-all.jar ./app.jar
ENV JAVA_OPTS="-XX:-OmitStackTraceInFastThrow -Xms256m -Xmx1536m"
ENV LANG='C.UTF-8' LC_ALL='C.UTF-8' TZ="Europe/Oslo"
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
