FROM ghcr.io/navikt/baseimages/temurin:21
COPY scripts/init-env.sh /init-scripts/init-env.sh
COPY build/libs/pam-import-api-*-all.jar ./app.jar
ENV JAVA_OPTS="-Xms256m -Xmx1536m"
