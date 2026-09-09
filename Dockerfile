FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25

COPY build/install/*/lib /app/lib

ENV JDK_JAVA_OPTIONS="--enable-preview -XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

EXPOSE 9028

CMD ["-cp", "/app/lib/*", "no.nav.arbeidsplassen.importapi.ApplicationKt"]
