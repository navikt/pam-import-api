FROM gcr.io/distroless/java21

COPY scripts/init-env.sh /init-scripts/init-env.sh
COPY build/libs/pam-import-api-all.jar ./app.jar
EXPOSE 9028

ENV JAVA_OPTS="-Xms256m -Xmx1536m"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"

ENTRYPOINT ["java","-jar","/app.jar"]
