FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:6cfc2c5e3791cc50d11fd4404844a276bec2dc970493db1118ff8170b7c00259

COPY build/install/*/lib /app/lib

ENV JDK_JAVA_OPTIONS="--enable-preview -XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

EXPOSE 9028

CMD ["-cp", "/app/lib/*", "no.nav.arbeidsplassen.importapi.ApplicationKt"]
