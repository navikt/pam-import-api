FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:7c23edc701ceea40893d376025c7dbe982cc01ead4f47f7cb2ff1349d6ece3ed

COPY build/install/*/lib /app/lib

ENV JDK_JAVA_OPTIONS="--enable-preview -XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

EXPOSE 9028

CMD ["-cp", "/app/lib/*", "no.nav.arbeidsplassen.importapi.ApplicationKt"]
