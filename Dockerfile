FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache bash
RUN adduser -u 1000 apprunner -D
USER apprunner

COPY build/libs/pam-import-api-*-all.jar ./app.jar
ENV JAVA_OPTS="-XX:-OmitStackTraceInFastThrow -Xms256m -Xmx1536m"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
